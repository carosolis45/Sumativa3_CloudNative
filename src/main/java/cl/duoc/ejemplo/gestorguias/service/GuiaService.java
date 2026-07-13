package cl.duoc.ejemplo.gestorguias.service;

import cl.duoc.ejemplo.gestorguias.entity.GuiaDespacho;
import cl.duoc.ejemplo.gestorguias.repository.GuiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class GuiaService {

    @Autowired
    private GuiaRepository guiaRepository;

    @Autowired
    private S3Service s3Service;  // ✅ REACTIVADO

    @Autowired
    private ProductorService productorService;

    @Autowired
    private EfsStorageService efsStorageService;  // ✅ NUEVO: Para generar PDFs

    @Value("${efs.mount.path}")
    private String efsPath;

    /**
     * 1. Crear guía de despacho
     * Envía la guía a la cola de RabbitMQ y genera PDF
     */
    public GuiaDespacho crearGuia(GuiaDespacho guia) {
        guia.setNumeroGuia(generarNumeroGuia());
        guia.setFechaCreacion(LocalDateTime.now());
        guia.setEstado("PENDIENTE");
        
        // Enviamos la guía a la cola de RabbitMQ
        productorService.enviarGuia(guia);
        
        // ✅ Generar PDF usando EfsStorageService
        Path pdfPath = efsStorageService.generarPdf(guia);
        guia.setRutaArchivo(pdfPath.toString());
        
        // Guardar en base de datos
        guiaRepository.save(guia);
        
        return guia;
    }

    /**
     * 2. Subir guías generadas a S3
     */
    public String subirAS3(Long id) {
        GuiaDespacho guia = guiaRepository.findById(id).orElse(null);
        if (guia == null) return "Guía no encontrada";
        
        String fecha = guia.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        try {
            // Leer el PDF desde el EFS
            Path pdfPath = Paths.get(guia.getRutaArchivo());
            byte[] contenido = Files.readAllBytes(pdfPath);
            
            // Subir a S3
            s3Service.subirArchivo(fecha + "/" + guia.getTransportista(), guia.getNumeroGuia() + ".pdf", contenido);
            guia.setEstado("SUBIDA");
            guiaRepository.save(guia);
            return "Guía subida a S3: " + fecha + "/" + guia.getTransportista() + "/" + guia.getNumeroGuia() + ".pdf";
        } catch (IOException e) {
            return "Error al subir: " + e.getMessage();
        }
    }

    /**
     * 3. Descargar guía desde S3
     */
    public String descargarGuia(Long id) {
        GuiaDespacho guia = guiaRepository.findById(id).orElse(null);
        if (guia == null) {
            throw new RuntimeException("Guía no encontrada con ID: " + id);
        }
        
        String fecha = guia.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String carpeta = fecha + "/" + guia.getTransportista();
        String nombreArchivo = guia.getNumeroGuia() + ".pdf";
        
        return s3Service.descargarArchivo(carpeta, nombreArchivo);
    }

    /**
     * 4. Modificar o actualizar guías
     */
    public GuiaDespacho actualizarGuia(GuiaDespacho guiaActualizada) {
        GuiaDespacho guiaExistente = guiaRepository.findById(guiaActualizada.getId())
            .orElseThrow(() -> new RuntimeException("Guía no encontrada con ID: " + guiaActualizada.getId()));
        
        guiaExistente.setTransportista(guiaActualizada.getTransportista());
        guiaExistente.setDestinatario(guiaActualizada.getDestinatario());
        guiaExistente.setOrigen(guiaActualizada.getOrigen());
        guiaExistente.setDestino(guiaActualizada.getDestino());
        guiaExistente.setPeso(guiaActualizada.getPeso());
        guiaExistente.setValorDeclarado(guiaActualizada.getValorDeclarado());
        guiaExistente.setEstado("ACTUALIZADA");
        
        // Regenerar PDF con datos actualizados
        Path pdfPath = efsStorageService.generarPdf(guiaExistente);
        guiaExistente.setRutaArchivo(pdfPath.toString());
        
        GuiaDespacho saved = guiaRepository.save(guiaExistente);
        return saved;
    }

    /**
     * 5. Eliminar guías (de S3, EFS y BD)
     */
    public String eliminarGuia(Long id) {
        GuiaDespacho guia = guiaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Guía no encontrada con ID: " + id));
        
        // Eliminar de S3
        String fecha = guia.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String carpeta = fecha + "/" + guia.getTransportista();
        String nombreArchivo = guia.getNumeroGuia() + ".pdf";
        
        try {
            if (s3Service.archivoExiste(carpeta, nombreArchivo)) {
                s3Service.eliminarArchivo(carpeta, nombreArchivo);
            }
        } catch (Exception e) {
            System.err.println("Error al eliminar de S3: " + e.getMessage());
        }
        
        // Eliminar archivo PDF del EFS
        try {
            Path pdfPath = Paths.get(guia.getRutaArchivo());
            Files.deleteIfExists(pdfPath);
        } catch (IOException e) {
            System.err.println("Error al eliminar archivo EFS: " + e.getMessage());
        }
        
        guiaRepository.delete(guia);
        return "Guía eliminada correctamente";
    }

    /**
     * 6. Consultar guías por transportista
     */
    public List<GuiaDespacho> consultarPorTransportista(String transportista) {
        return guiaRepository.findByTransportista(transportista);
    }

    /**
     * 7. Consultar guías por transportista y fecha
     */
    public List<GuiaDespacho> consultarPorTransportistaYFecha(String transportista, LocalDateTime inicio, LocalDateTime fin) {
        return guiaRepository.findByTransportistaAndFechaCreacionBetween(transportista, inicio, fin);
    }

    /**
     * 8. Buscar guía por ID
     */
    public GuiaDespacho buscarPorId(Long id) {
        return guiaRepository.findById(id).orElse(null);
    }

    /**
     * 9. Obtener todas las guías
     */
    public List<GuiaDespacho> obtenerTodas() {
        return guiaRepository.findAll();
    }

    // ===== MÉTODOS PRIVADOS =====

    private String generarNumeroGuia() {
        return "GIA-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }
}