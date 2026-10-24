package main.model.component;

import main.model.attribute.ComponentType;
import main.model.attribute.ConstantPowerDraw;
import main.model.attribute.StorageTech;
import main.model.attribute.StorageType;

public record Storage(
        int id, String brand, String model, double price,
        StorageType storageType,
        StorageTech storageTech,
        int CapacityGb

    ) implements Component {

    @Override
    public ComponentType type() { return ComponentType.STORAGE; }

    @Override
    public int powerDrawWatts() { return ConstantPowerDraw.STORAGE_DEFAULT_POWER_DRAW; }
}
