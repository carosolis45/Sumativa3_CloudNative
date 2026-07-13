package cl.duoc.ejemplo.gestorguias.repository;

import cl.duoc.ejemplo.gestorguias.entity.GuiaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GuiaRepository extends JpaRepository<GuiaDespacho, Long> {
    List<GuiaDespacho> findByTransportista(String transportista);
    List<GuiaDespacho> findByTransportistaAndFechaCreacionBetween(String transportista, LocalDateTime inicio, LocalDateTime fin);
}