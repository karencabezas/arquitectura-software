package org.example;

abstract class Ave {
    protected String nombre;

    public Ave(String nombre) {
        this.nombre = nombre;
    }

    public void comer() {
        System.out.println(nombre + " está comiendo");
    }

    public void respirar() {
        System.out.println(nombre + " está respirando");
    }
}

// Aves que pueden volar
class AveVoladora extends Ave {
    public AveVoladora(String nombre) {
        super(nombre);
    }

    public void volar() {
        System.out.println(nombre + " está volando");
    }
}

// Aves que no pueden volar
class AveNoVoladora extends Ave {
    public AveNoVoladora(String nombre) {
        super(nombre);
    }

    public void caminar() {
        System.out.println(nombre + " está caminando");
    }
}

// Implementaciones
class Paloma extends AveVoladora {
    public Paloma() {
        super("Paloma");
    }
}

class Pinguino extends AveNoVoladora {
    public Pinguino() {
        super("Pingüino");
    }
}

public class LSP {
    public static void main(String[] args) {
        // Puedo sustituir Ave por cualquier subclase
        Ave ave1 = new Paloma();
        Ave ave2 = new Pinguino();

        //Acciones en comun
        ave1.comer();
        ave2.comer();
        ave1.respirar();
        ave2.respirar();

        //Acciones específicas
        AveVoladora voladora = new Paloma();
        voladora.volar();
        AveNoVoladora noVoladora = new Pinguino();
        noVoladora.caminar();

    }

}
