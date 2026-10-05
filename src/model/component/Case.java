package model.component;

import model.attribute.ComponentType;
import model.attribute.FormFactor;
import model.attribute.PsuFormFactor;

import java.util.Set;

public record Case(
        String id, String brand, String model, double price, int powerDrawWatts,
        Set<FormFactor> supportedForms,
        Set<PsuFormFactor> supportedPsuForms,
        int maxGpuLengthMm,
        int maxCoolerHeightMm,
        int expansionSlots,
        int driveBays,                      // bays for SATA drives (2.5" / 3.5")
        Set<Integer> supportedRadiatorsMm
    ) implements Component {
    @Override
    public ComponentType type() { return ComponentType.CASE; }
}
