package model.component;

import model.attribute.ComponentType;

public record Gpu(
        int id, String brand, String model, double price, int tdpWatts,
        int vramCapacityGb,
        int lenghtMm,
        double slotWidth,
        int recommendedPsuWatts,
        int powerConnectors
    ) implements Component {

    @Override
    public ComponentType type() { return ComponentType.GPU; }

    @Override
    public int powerDrawWatts() { return tdpWatts; }
}
