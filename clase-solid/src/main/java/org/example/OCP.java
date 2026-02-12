package org.example;

// 1. Interface
interface Notificacion {
    void enviar(String mensaje);
}

// 2. Implementaciones del ejemplo
class NotificacionSMS implements Notificacion {
    @Override
    public void enviar(String mensaje) {
        System.out.println("Enviando notificación vía SMS: " + mensaje);
    }
}

class NotificacionEmail implements Notificacion {
    @Override
    public void enviar(String mensaje) {
        System.out.println("Enviando notificación vía Email: " + mensaje);
    }
}

// 3. Nueva implementación - NotificacionWhatsApp
class NotificacionWhatsApp implements Notificacion {
    @Override
    public void enviar(String mensaje) {
        System.out.println("Enviando notificación vía WhatsApp: " + mensaje);
    }
}

// 4. Nueva implementación - NotificacionPush
class NotificacionPush implements Notificacion {
    @Override
    public void enviar(String mensaje) {
        System.out.println("Enviando notificación vía Push: " + mensaje);
    }
}

// 4. Servicio para notificaciones
class ServicioNotificaciones {
    public void notificar(Notificacion notificacion, String mensaje) {
        notificacion.enviar(mensaje);
    }
}

public class OCP {
    public static void main(String[] args) {
        ServicioNotificaciones servicio = new ServicioNotificaciones();
        String mensaje = "Compra realizada exitosamente!";

        // Usar diferentes tipos de notificaciones
        servicio.notificar(new NotificacionSMS(), mensaje);
        servicio.notificar(new NotificacionEmail(), mensaje);
        servicio.notificar(new NotificacionWhatsApp(), mensaje);
        servicio.notificar(new NotificacionPush(), mensaje);
    }
}
