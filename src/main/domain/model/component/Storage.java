package main.domain.model.component;

import main.domain.model.attribute.ComponentType;
import main.domain.model.attribute.ConstantPowerDraw;
import main.domain.model.attribute.StorageTech;
import main.domain.model.attribute.StorageType;

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
