package org.example;

// 1. Datos del libro
class Libro {
    private String titulo;
    private String autor;

    public Libro(String titulo, String autor) {
        this.titulo = titulo;
        this.autor = autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }
}

// 2. Maneja la persistencia
class LibroRepositorio {
    public void guardarEnArchivo(Libro libro) {
        // Lógica para guardar en archivo
        System.out.println("Guardando: " + libro.getTitulo() + " - " + libro.getAutor() + " en archivo...");
    }
}

// 3. Maneja la impresión en consola
class LibroImpresora {
    public void imprimirEnConsola(Libro libro) {
        System.out.println(libro.getTitulo() + " - " + libro.getAutor());
    }
}

// 4. Maneja el envio de emails
class LibroEmailService {
    public void enviarPorEmail(Libro libro, String destinatario) {
        System.out.println("Enviando por email: " + libro.getTitulo() + " - " + libro.getAutor() + " a " + destinatario);
    }
}

public class SRP {

    public static void main(String[] args) {
        Libro libro = new Libro("Orgullo y prejuicio", "Jane Austen");

        LibroRepositorio repositorio = new LibroRepositorio();
        repositorio.guardarEnArchivo(libro);

        LibroImpresora impresora = new LibroImpresora();
        impresora.imprimirEnConsola(libro);

        LibroEmailService emailService = new LibroEmailService();
        emailService.enviarPorEmail(libro, "persona@gmail.com");
    }
}
