package org.example;

// 1. Interfaces
interface Vehiculo {
    void arrancar();
    void detener();
}

interface VehiculoTerrestre {
    void conducir();
}

interface VehiculoAereo {
    void volar();
    void despegar();
    void aterrizar();
}

interface VehiculoAcuatico {
    void navegar();
    void anclar();
}

class Automovil implements Vehiculo, VehiculoTerrestre {
    @Override
    public void arrancar() {
        System.out.println("Automóvil arrancando...");
    }

    @Override
    public void detener() {
        System.out.println("Automóvil deteniendose...");
    }

    @Override
    public void conducir() {
        System.out.println("Automóvil conduciendo");
    }
}

class Avion implements Vehiculo, VehiculoAereo {
    @Override
    public void arrancar() {
        System.out.println("Avión con motores encendidos...");
    }

    @Override
    public void detener() {
        System.out.println("Avión deteniendose...");
    }

    @Override
    public void volar() {
        System.out.println("Avión volando...");
    }

    @Override
    public void despegar() {
        System.out.println("Avión despegando de la pista...");
    }

    @Override
    public void aterrizar() {
        System.out.println("Avión aterrizando...");
    }
}

class Barco implements Vehiculo, VehiculoAcuatico {
    @Override
    public void arrancar() {
        System.out.println("Barco con motor encendido...");
    }

    @Override
    public void detener() {
        System.out.println("Barco con motor apagado...");
    }

    @Override
    public void navegar() {
        System.out.println("Barco navegando...");
    }

    @Override
    public void anclar() {
        System.out.println("Barco anclado...");
    }
}


public class ISP {
    public static void main(String[] args) {
        // Vehículos Terrestres
        Automovil auto = new Automovil();
        auto.arrancar();
        auto.conducir();
        auto.detener();

        // Vehículos Aéreos
        Avion avion = new Avion();
        avion.arrancar();
        avion.despegar();
        avion.volar();
        avion.aterrizar();
        avion.detener();

        // Vehículo sin motor
        Barco barco = new Barco();
        barco.arrancar();
        barco.detener();
        barco.navegar();
        barco.anclar();
    }
}
