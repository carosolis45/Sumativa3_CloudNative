package cl.duoc.ejemplo.gestorguias.service;

import cl.duoc.ejemplo.gestorguias.entity.GuiaDespacho;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class EfsStorageService {

    private static final Logger log = LoggerFactory.getLogger(EfsStorageService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Path efsRoot;

    public EfsStorageService(@Value("${efs.mount.path}") String efsPath) {
        this.efsRoot = Paths.get(efsPath).toAbsolutePath();
    }

    public Path generarPdf(GuiaDespacho guia) {
        Path destino = efsRoot.resolve("guia" + guia.getNumeroGuia() + ".pdf");
        try {
            Files.createDirectories(efsRoot);
            try (OutputStream out = Files.newOutputStream(destino)) {
                escribirPdf(guia, out);
            }
            log.info("Guía {} generada en EFS: {}", guia.getNumeroGuia(), destino);
            return destino;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo escribir la guía en el EFS: " + destino, ex);
        }
    }

    public byte[] leerPdf(Path ruta) {
        try {
            return Files.readAllBytes(ruta);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo leer la guía desde el EFS: " + ruta, ex);
        }
    }

    public void eliminarPdf(Path ruta) {
        if (ruta == null) return;
        try {
            boolean borrado = Files.deleteIfExists(ruta);
            if (borrado) log.info("Guía eliminada del EFS: {}", ruta);
        } catch (IOException ex) {
            log.warn("No se pudo eliminar la guía del EFS: {}", ex.getMessage());
        }
    }

    public String nombreArchivo(GuiaDespacho guia) {
        return "guia" + guia.getNumeroGuia() + ".pdf";
    }

    public Path getEfsRoot() {
        return efsRoot;
    }

    private void escribirPdf(GuiaDespacho guia, OutputStream out) {
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font label = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font valor = FontFactory.getFont(FontFactory.HELVETICA, 11);

            Paragraph header = new Paragraph("GUÍA DE DESPACHO", titulo);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(16f);
            doc.add(header);

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{1.2f, 2.8f});

            fila(tabla, "N° Guía:", guia.getNumeroGuia(), label, valor);
            fila(tabla, "Transportista:", guia.getTransportista(), label, valor);
            fila(tabla, "Destinatario:", guia.getDestinatario(), label, valor);
            fila(tabla, "Origen:", guia.getOrigen(), label, valor);
            fila(tabla, "Destino:", guia.getDestino(), label, valor);
            fila(tabla, "Peso:", guia.getPeso() + " kg", label, valor);
            fila(tabla, "Valor Declarado:", "$" + guia.getValorDeclarado(), label, valor);
            fila(tabla, "Fecha:", guia.getFechaCreacion().format(FECHA), label, valor);
            fila(tabla, "Estado:", guia.getEstado(), label, valor);

            doc.add(tabla);

            Paragraph pie = new Paragraph(
                    "\nDocumento generado automáticamente por el sistema de gestión de guías.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, java.awt.Color.GRAY));
            pie.setSpacingBefore(20f);
            doc.add(pie);

        } catch (Exception ex) {
            throw new RuntimeException("Error al construir el PDF de la guía " + guia.getNumeroGuia(), ex);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    private void fila(PdfPTable tabla, String etiqueta, String contenido, Font label, Font valor) {
        PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, label));
        PdfPCell c2 = new PdfPCell(new Phrase(contenido, valor));
        c1.setPadding(6f);
        c2.setPadding(6f);
        tabla.addCell(c1);
        tabla.addCell(c2);
    }
}