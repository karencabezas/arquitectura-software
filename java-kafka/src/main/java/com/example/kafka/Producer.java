// Producer.java
package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Scanner;

public class Producer {
    private static final String BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String TOPICO_ALUMNOS = "topico-alumnos";
    private static final String TOPICO_PROFESORES = "topico-profesores";
    private static final String TOPICO_DIRECTORES = "topico-directores";

    public static void main(String[] args) {
        // Configurar propiedades del productor
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Crear el productor
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // Enviar mensajes
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                System.out.println("\n---------- Productor Kafka - Menu ----------");
                System.out.println("Elige el tópico al que desea enviar mensaje: ");
                System.out.println("1. " + TOPICO_ALUMNOS);
                System.out.println("2. " + TOPICO_PROFESORES);
                System.out.println("3. " + TOPICO_DIRECTORES);
                System.out.println("4. para salir");
                System.out.print("Digite la opción> ");
                String opcion = scanner.nextLine();
                String topicSeleccionado;
                switch (opcion) {
                    case "1":
                        topicSeleccionado = TOPICO_ALUMNOS;
                        System.out.println("Tópico seleccionado: " + TOPICO_ALUMNOS);
                        break;
                    case "2":
                        topicSeleccionado = TOPICO_PROFESORES;
                        System.out.println("Tópico seleccionado: " + TOPICO_PROFESORES);
                        break;
                    case "3":
                        topicSeleccionado = TOPICO_DIRECTORES;
                        System.out.println("Tópico seleccionado: " + TOPICO_DIRECTORES);
                        break;
                    case "4":
                        System.out.println("Saliendo...");
                        return;
                    default:
                        System.out.println("Opción no válida. Se seleccionará TOPICO_ALUMNOS por defecto.");
                        topicSeleccionado = TOPICO_ALUMNOS;
                }

                while (true) {
                    System.out.print("Escribe el mensaje ([exit] para salir) >");
                    String mensaje = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(mensaje)) {
                        break;
                    }
                    // Crear el registro a enviar
                    ProducerRecord<String, String> record = new ProducerRecord<>(topicSeleccionado, mensaje);

                    // Enviar el mensaje
                    producer.send(record, (metadata, exception) -> {
                        if (exception == null) {
                            System.out.println("Mensaje enviado: " +
                                    "Topic: " + metadata.topic() + ", " +
                                    "Partition: " + metadata.partition() + ", " +
                                    "Offset: " + metadata.offset() + ", " +
                                    "Timestamp: " + metadata.timestamp());
                        } else {
                            System.err.println("Error al enviar mensaje: " + exception.getMessage());
                        }
                    });
                    // Forzar el envío
                    producer.flush();
                }

            }
        } finally {
            // Cerrar el productor
            System.out.println("Cerrando recursos...");
            producer.close();
            scanner.close();
            System.out.println("Productor cerrado correctamente.");
        }
    }
}