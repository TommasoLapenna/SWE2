package model.component;

import model.attribute.ComponentType;
import model.attribute.EfficencyRating;
import model.attribute.PsuFormFactor;

public record PowerSupply(
        int id, String brand, String model, double price,
        int wattage,
        EfficencyRating efficencyRating,
        PsuFormFactor formFactor,
        int pcieConnectors

    ) implements Component {

    @Override
    public ComponentType type() { return ComponentType.POWER_SUPPLY; }

    @Override
    public int powerDrawWatts() { return 0; }
}
