package org.example;

// 1. Interface
interface GeneradorReporte {
    void crear(String contenido);
}

// 2. Implementaciones
class PDFGenerator implements GeneradorReporte {
    @Override
    public void crear(String contenido) {
        System.out.println("Generando PDF...");
        System.out.println("Contenido: " + contenido);
        System.out.println("PDF creado exitosamente");
    }

}

class ExcelGenerator implements GeneradorReporte {
    @Override
    public void crear(String contenido) {
        System.out.println("Generando Excel...");
        System.out.println("Contenido: " + contenido);
        System.out.println("Excel creado exitosamente");
    }

}

class WordGenerator implements GeneradorReporte {
    @Override
    public void crear(String contenido) {
        System.out.println("Generando Word...");
        System.out.println("Contenido: " + contenido);
        System.out.println("Word creado exitosamente");
    }

}

class HTMLGenerator implements GeneradorReporte {
    @Override
    public void crear(String contenido) {
        System.out.println("Generando HTML...");
        System.out.println("Contenido:" + contenido );
        System.out.println("HTML creado exitosamente");
    }

}

// 3. Service
class ReporteService {
    private GeneradorReporte generador;

    // Inyección
    public ReporteService(GeneradorReporte generador) {
        this.generador = generador;
    }

    public void generar(String contenido) {
        generador.crear("Contenido del reporte: " + contenido);
    }
}


public class DIP {
    public static void main(String[] args) {
        String contenido = "Archivo de cuadre enero 2026";

        // PDF
        ReporteService servicioPDF = new ReporteService(new PDFGenerator());
        servicioPDF.generar(contenido);

        // Excel
        ReporteService servicioExcel = new ReporteService(new ExcelGenerator());
        servicioExcel.generar(contenido);

        // Word
        ReporteService servicioWord = new ReporteService(new WordGenerator());
        servicioWord.generar(contenido);

        // HTML
        ReporteService servicioHTML = new ReporteService(new WordGenerator());
        servicioHTML.generar(contenido);

    }
}
