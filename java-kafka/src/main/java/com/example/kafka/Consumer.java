// Consumer.java
package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class Consumer {
    private static final String BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String TOPICO_ALUMNOS = "topico-alumnos";
    private static final String TOPICO_PROFESORES = "topico-profesores";
    private static final String TOPICO_DIRECTORES = "topico-directores";
    private static final String GROUP_ID = "kafka-demo-group";

    public static void main(String[] args) {
        // Configurar propiedades del consumidor
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Crear el consumidor
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // Suscribirse al topic
        consumer.subscribe(Arrays.asList(TOPICO_ALUMNOS, TOPICO_PROFESORES, TOPICO_DIRECTORES));

        System.out.println("===== KAFKA CONSUMER - 3 TÓPICOS =====");
        System.out.println("Escuchando mensajes de los siguientes tópicos: ");
        System.out.println("  1. " + TOPICO_ALUMNOS);
        System.out.println("  2. " + TOPICO_PROFESORES);
        System.out.println("  3. " + TOPICO_DIRECTORES);
        System.out.println("Presiona Ctrl+C para salir...");

        try {
            // Bucle para escuchar mensajes continuamente
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Mensaje recibido: " +
                            "Topic: " + record.topic() + ", " +
                            "Partition: " + record.partition() + ", " +
                            "Offset: " + record.offset() + ", " +
                            "Timestamp: " + record.timestamp() + ", " +
                            "Value: " + record.value());
                }
            }
        } catch (Exception e) {
            System.err.println("Error en el consumidor: " + e.getMessage());
        } finally {
            // Cerrar el consumidor
            consumer.close();
            System.out.println("Consumidor cerrado correctamente.");
        }
    }
}