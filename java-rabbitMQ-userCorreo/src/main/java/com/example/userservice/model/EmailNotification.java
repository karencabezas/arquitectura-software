package com.example.userservice.model;

import java.io.Serializable;
import java.util.Objects;

public class EmailNotification implements Serializable {

    private String to;
    private String subject;
    private String body;

    // Constructors
    public EmailNotification() {
    }

    public EmailNotification(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    // Create notification for new user registration
    public static EmailNotification forNewUserRegistration(User user) {
        String subject = "Bienvenido a nuestro servicio!";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Tu cuenta ha sido creado exitosamente.\n\n" +
                        "Saludos,\n" +
                        "Codigo",
                user.getName());

        return new EmailNotification(user.getEmail(), subject, body);
    }

    // Create notification for new product registration
    public static EmailNotification forNewProductRegistration(Product product) {
        String subject = "¡Producto registrado exitosamente!";
        String body = String.format(
                "Hola,\n\n" +
                        "El siguiente producto ha sido registrado en nuestro sistema:\n\n" +
                        "Nombre:  %s\n" +
                        "Codigo:  %s\n" +
                        "Precio:  $%.2f\n" +
                        "Stock:   %d unidades\n\n" +
                        "Si tienes alguna consulta, no dudes en contactarnos.\n\n" +
                        "Saludos,\n" +
                        "El equipo de soporte",
                product.getName(),
                product.getCode(),
                product.getPrice(),
                product.getStock());

        return new EmailNotification(product.getEmail(), subject, body);
    }

    // Getters and Setters
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // equals, hashCode and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailNotification that = (EmailNotification) o;
        return Objects.equals(to, that.to) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(to, subject, body);
    }

    @Override
    public String toString() {
        return "EmailNotification{" +
                "to='" + to + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}