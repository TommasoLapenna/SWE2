package model.component;

import model.attribute.ComponentType;

public record Peripherals (
        String id, String brand, String model, double price, int powerDrawWatts
    ) implements Component {

    @Override
    public ComponentType type() { return ComponentType.PERIPHERAL; }
}
