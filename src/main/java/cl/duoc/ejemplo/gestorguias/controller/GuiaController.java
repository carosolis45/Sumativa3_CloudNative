package cl.duoc.ejemplo.gestorguias.controller;

import cl.duoc.ejemplo.gestorguias.entity.GuiaDespacho;
import cl.duoc.ejemplo.gestorguias.service.GuiaService;
import cl.duoc.ejemplo.gestorguias.service.ProductorService;
import cl.duoc.ejemplo.gestorguias.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/guias")
public class GuiaController {

    @Autowired
    private GuiaService guiaService;

    // @Autowired
    // private S3Service s3Service;  // COMENTADO

    @Autowired
    private ProductorService productorService;

    /**
     * 1. Crear guías de despacho
     */
    @PostMapping("/crear")
    public ResponseEntity<String> crearGuia(@RequestBody GuiaDespacho guia) {
        GuiaDespacho creada = guiaService.crearGuia(guia);
        return ResponseEntity.ok("✅ Guía creada y enviada a la cola: " + creada.getNumeroGuia());
    }

    /**
     * 2. Subir guías generadas a S3 (COMENTADO)
     */
    /*
    @PostMapping("/subir/{id}")
    public ResponseEntity<String> subirGuiaS3(@PathVariable Long id) {
        return ResponseEntity.ok(guiaService.subirAS3(id));
    }
    */

    /**
     * 3. Descargar guías (COMENTADO)
     */
    /*
    @GetMapping("/descargar/{id}")
    public ResponseEntity<String> descargarGuia(@PathVariable Long id) {
        String contenido = guiaService.descargarGuia(id);
        return ResponseEntity.ok(contenido);
    }
    */

    /**
     * 4. Modificar o actualizar guías
     */
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<GuiaDespacho> actualizarGuia(@PathVariable Long id, @RequestBody GuiaDespacho guia) {
        guia.setId(id);
        return ResponseEntity.ok(guiaService.actualizarGuia(guia));
    }

    /**
     * 5. Eliminar guías
     */
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarGuia(@PathVariable Long id) {
        return ResponseEntity.ok(guiaService.eliminarGuia(id));
    }

    /**
     * 6. Consultar guías por transportista
     */
    @GetMapping("/consultar/transportista")
    public ResponseEntity<List<GuiaDespacho>> consultarPorTransportista(@RequestParam String transportista) {
        return ResponseEntity.ok(guiaService.consultarPorTransportista(transportista));
    }

    /**
     * 7. Consultar guías por transportista y fecha
     */
    @GetMapping("/consultar")
    public ResponseEntity<List<GuiaDespacho>> consultarPorTransportistaYFecha(
            @RequestParam String transportista,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(guiaService.consultarPorTransportistaYFecha(transportista, inicio, fin));
    }

    /**
     * 8. Obtener todas las guías
     */
    @GetMapping("/todas")
    public ResponseEntity<List<GuiaDespacho>> obtenerTodas() {
        return ResponseEntity.ok(guiaService.obtenerTodas());
    }

    /**
     * 9. Procesar colas
     */
    @PostMapping("/procesar-colas")
    public ResponseEntity<String> procesarColaExitosa() {
        return ResponseEntity.ok("✅ La cola está siendo escuchada automáticamente por el consumidor");
    }
}