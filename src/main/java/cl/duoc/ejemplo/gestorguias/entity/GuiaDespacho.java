package cl.duoc.ejemplo.gestorguias.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "guias_despacho")
public class GuiaDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroGuia;
    private String transportista;
    private String destinatario;
    private String origen;
    private String destino;
    private Double peso;
    private Double valorDeclarado;
    private LocalDateTime fechaCreacion;
    private String rutaArchivo;
    private String estado;

    public GuiaDespacho() {}

    // Getters
    public Long getId() { return id; }
    public String getNumeroGuia() { return numeroGuia; }
    public String getTransportista() { return transportista; }
    public String getDestinatario() { return destinatario; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public Double getPeso() { return peso; }
    public Double getValorDeclarado() { return valorDeclarado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public String getRutaArchivo() { return rutaArchivo; }
    public String getEstado() { return estado; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }
    public void setTransportista(String transportista) { this.transportista = transportista; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public void setOrigen(String origen) { this.origen = origen; }
    public void setDestino(String destino) { this.destino = destino; }
    public void setPeso(Double peso) { this.peso = peso; }
    public void setValorDeclarado(Double valorDeclarado) { this.valorDeclarado = valorDeclarado; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public void setEstado(String estado) { this.estado = estado; }
}