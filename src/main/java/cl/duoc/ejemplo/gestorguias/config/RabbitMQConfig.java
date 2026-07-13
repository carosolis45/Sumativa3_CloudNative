package cl.duoc.ejemplo.gestorguias.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nombre de las colas
    public static final String COLA_EXITOSA = "cola.guias.exitosas";
    public static final String COLA_ERRORES = "cola.guias.errores";

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:admin}")
    private String username;

    @Value("${spring.rabbitmq.password:admin123}")
    private String password;

    // Configuración de conexión
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    // Configuración de RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    // Convertidor de mensajes a JSON
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Crear cola 1: Guias exitosas
    @Bean
    public Queue colaExitosa() {
        return new Queue(COLA_EXITOSA, true); // true = durable (persiste)
    }

    // Crear cola 2: Guias con error
    @Bean
    public Queue colaErrores() {
        return new Queue(COLA_ERRORES, true); // true = durable (persiste)
    }
}