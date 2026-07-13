package cl.duoc.ejemplo.gestorguias.service;

import cl.duoc.ejemplo.gestorguias.config.RabbitMQConfig;
import cl.duoc.ejemplo.gestorguias.entity.GuiaDespacho;
import cl.duoc.ejemplo.gestorguias.repository.GuiaRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsumidorService {

    @Autowired
    private GuiaRepository guiaRepository;

    /**
     * Escucha la cola exitosa y guarda la guia en Oracle Cloud.
     */
    @RabbitListener(queues = RabbitMQConfig.COLA_EXITOSA)
    public void consumirGuiaExitosa(GuiaDespacho guia) {
        try {
            guiaRepository.save(guia);
            System.out.println("Guia guardada en Oracle Cloud: " + guia.getId());
        } catch (Exception e) {
            System.err.println("Error al guardar en Oracle: " + e.getMessage());
        }
    }
}