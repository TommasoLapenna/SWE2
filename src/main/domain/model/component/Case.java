package main.domain.model.component;

import main.domain.model.attribute.ComponentType;
import main.domain.model.attribute.FormFactor;
import main.domain.model.attribute.PsuFormFactor;

import java.util.Set;

public record Case(
        int id, String brand, String model, double price, int powerDrawWatts,
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