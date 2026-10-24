package main.model.component;

import main.model.attribute.ComponentType;

public record Peripherals (
        int id, String brand, String model, double price, int powerDrawWatts
    ) implements Component {

    @Override
    public ComponentType type() { return ComponentType.PERIPHERAL; }
}
