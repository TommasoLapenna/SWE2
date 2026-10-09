package model.component;

import model.attribute.ComponentType;
import model.attribute.ConstantPowerDraw;
import model.attribute.StorageTech;
import model.attribute.StorageType;

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
