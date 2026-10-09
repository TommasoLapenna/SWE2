package model.component;

import model.attribute.ComponentType;
import model.attribute.ConstantPowerDraw;
import model.attribute.MemoryGen;

public record Memory (
        int id, String brand, String model, double price,
        MemoryGen memoryGen,
        int sticksQty,
        int stickCapacityGb,
        int frequencyMhz

    ) implements Component {

    @Override
    public ComponentType type() { return ComponentType.RAM; }

    @Override
    public int powerDrawWatts() { return ConstantPowerDraw.RAM_DEFAULT_POWER_DRAW*sticksQty; }

    public int totalCapacityGb() { return sticksQty*stickCapacityGb; }
}
