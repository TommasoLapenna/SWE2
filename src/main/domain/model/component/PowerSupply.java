package main.domain.model.component;

import main.domain.model.attribute.ComponentType;
import main.domain.model.attribute.EfficencyRating;
import main.domain.model.attribute.PsuFormFactor;

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
