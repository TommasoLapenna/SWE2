package model.component;

import model.attribute.ComponentType;

public sealed interface Component
        permits Case, Gpu, Peripherals, Cooler, Cpu, Memory, Motherboard, PowerSupply, Storage {

    int id();
    String brand();
    String model();
    double price();

    int powerDrawWatts();

    public ComponentType type();
    default String name() { return brand() + " " + model(); }

}
