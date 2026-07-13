package cl.duoc.ejemplo.gestorguias.service;

import cl.duoc.ejemplo.gestorguias.config.RabbitMQConfig;
import cl.duoc.ejemplo.gestorguias.entity.GuiaDespacho;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductorService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Envia una guia a la cola exitosa.
     * Si falla, la envia a la cola de errores.
     */
    public void enviarGuia(GuiaDespacho guia) {
        try {
            // Intenta enviar a cola exitosa
            rabbitTemplate.convertAndSend(RabbitMQConfig.COLA_EXITOSA, guia);
            System.out.println("Guia enviada a cola exitosa: " + guia.getId());
        } catch (Exception e) {
            // Si falla, envia a cola de errores
            try {
                rabbitTemplate.convertAndSend(RabbitMQConfig.COLA_ERRORES, guia);
                System.err.println("Guia enviada a cola de errores: " + guia.getId());
            } catch (Exception ex) {
                System.err.println(" Error critico: No se pudo enviar a ninguna cola. " + ex.getMessage());
            }
        }
    }
}