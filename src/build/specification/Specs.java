package build.specification;

import pattern.factory.Factory;
import model.attribute.ComponentType;
import model.attribute.CoolerType;
import model.attribute.EfficencyRating;
import model.attribute.StorageTech;
import model.attribute.StorageType;
import model.component.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class Specs implements Factory {
    private Specs() {}

    // Thresholds used by the power specifications (declared first: a static initializer cannot forward-reference them)
    public static final double PSU_HEADROOM_FACTOR = 1.2;
    public static final int HIGH_LOAD_WATTS = 650;
    public static final EfficencyRating HIGH_LOAD_MIN_EFFICIENCY = EfficencyRating.GOLD;

    public static BuildSpecification atMost(ComponentType t, int n)  { return new ComponentCountSpecification(t, 0, n); }
    public static BuildSpecification atLeast(ComponentType t, int n) { return new ComponentCountSpecification(t, n, Integer.MAX_VALUE); }
    public static BuildSpecification exactly(ComponentType t, int n) { return new ComponentCountSpecification(t, n, n); }

    public static BuildSpecification mutipleExcatclyOne(ComponentType... t) {
        BuildSpecification res = new ComponentCountSpecification(t[0], 1, 1);
        for (int i = 1; i < t.length; i++){
            res = res.and(new ComponentCountSpecification(t[i], 1, 1));
        }
        return res;
    }

    // ==================== Completeness predicates on a single CPU ====================

    public static BuildSpecification cpuHasBundledCoolerSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CPU, Cpu.class).coolerIncluded(),
            Set.of(ComponentType.CPU)
    );

    public static BuildSpecification cpuHasIntegratedGpuSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CPU, Cpu.class).integratedGpu(),
            Set.of(ComponentType.CPU)
    );

    // ==================== Platform: CPU / motherboard / memory / cooler ====================

    public static BuildSpecification socketMatchSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CPU, Cpu.class).socket().equals(b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).socket()),
            Set.of(ComponentType.CPU, ComponentType.MOTHERBOARD)
    );

    public static BuildSpecification cpuSupportsBoardMemoryGenSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CPU, Cpu.class).memoryGen().contains(b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).supportedMemoryGen()),
            Set.of(ComponentType.CPU, ComponentType.MOTHERBOARD)
    );

    public static BuildSpecification ramMatchesBoardGenSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream().allMatch(r -> r.memoryGen() == b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).supportedMemoryGen()),
            Set.of(ComponentType.RAM, ComponentType.MOTHERBOARD)
    );

    public static BuildSpecification ramMatchesCpuGenSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream().allMatch(r -> b.getSingleComponent(ComponentType.CPU, Cpu.class).memoryGen().contains(r.memoryGen())),
            Set.of(ComponentType.RAM, ComponentType.CPU)
    );

    public static BuildSpecification ramSlotsSufficientSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream().mapToInt(Memory::sticksQty).sum() <= b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).ramSlots(),
            Set.of(ComponentType.RAM, ComponentType.MOTHERBOARD)
    );

    public static BuildSpecification ramCapacityWithinBoardLimitSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream().mapToInt(Memory::totalCapacityGb).sum() <= b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).maxMemoryGb(),
            Set.of(ComponentType.RAM, ComponentType.MOTHERBOARD)
    );

    public static BuildSpecification ramSpeedWithinBoardLimitSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream().allMatch(r -> r.frequencyMhz() <= b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).maxMemorySpeedMhz()),
            Set.of(ComponentType.RAM, ComponentType.MOTHERBOARD)
    );

    public static BuildSpecification ramKitsHomogeneousSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream().map(r -> List.of(r.frequencyMhz(), r.stickCapacityGb())).distinct().count() <= 1,
            Set.of(ComponentType.RAM)
    );

    public static BuildSpecification ramDualChannelSpecification = new PredicateSpecification(
            b -> {
                int sticks = b.getComponents(ComponentType.RAM, Memory.class).stream().mapToInt(Memory::sticksQty).sum();
                return sticks >= 2 && sticks % 2 == 0;
            },
            Set.of(ComponentType.RAM)
    );

    public static BuildSpecification coolerSocketSupportSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.COOLING, Cooler.class).supportedSocktes().contains(b.getSingleComponent(ComponentType.CPU, Cpu.class).socket()),
            Set.of(ComponentType.CPU, ComponentType.COOLING)
    );

    public static BuildSpecification coolerTdpSufficientSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.COOLING, Cooler.class).maxTdp() >= b.getSingleComponent(ComponentType.CPU, Cpu.class).tdpWatts(),
            Set.of(ComponentType.CPU, ComponentType.COOLING)
    );

    // ==================== Physical fit and expansion: case / motherboard / GPU / PSU / cooler ====================

    public static BuildSpecification formFactorMatchSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CASE, Case.class).supportedForms().contains(b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).formFactor()),
            Set.of(ComponentType.CASE, ComponentType.MOTHERBOARD)
    );

    public static BuildSpecification psuFormFactorFitsCaseSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CASE, Case.class).supportedPsuForms().contains(b.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).formFactor()),
            Set.of(ComponentType.CASE, ComponentType.POWER_SUPPLY)
    );

    public static BuildSpecification gpuLengthFitsCaseSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.GPU, Gpu.class).stream().allMatch(g -> g.lenghtMm() <= b.getSingleComponent(ComponentType.CASE, Case.class).maxGpuLengthMm()),
            Set.of(ComponentType.GPU, ComponentType.CASE)
    );

    // A card occupies ceil(slotWidth) expansion slots (a 2.5-slot cooler blocks 3 brackets)
    public static BuildSpecification gpuSlotsFitCaseSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.GPU, Gpu.class).stream().mapToInt(g -> (int) Math.ceil(g.slotWidth())).sum() <= b.getSingleComponent(ComponentType.CASE, Case.class).expansionSlots(),
            Set.of(ComponentType.GPU, ComponentType.CASE)
    );

    public static BuildSpecification gpuCountFitsBoardSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.GPU, Gpu.class).size() <= b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).pcieX16Slots(),
            Set.of(ComponentType.GPU, ComponentType.MOTHERBOARD)
    );

    public static BuildSpecification coolerIsAirSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.COOLING, Cooler.class).coolingType() == CoolerType.AIR,
            Set.of(ComponentType.COOLING)
    );

    public static BuildSpecification coolerIsLiquidSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.COOLING, Cooler.class).coolingType() == CoolerType.LIQUID,
            Set.of(ComponentType.COOLING)
    );

    public static BuildSpecification airCoolerHeightFitsCaseSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.COOLING, Cooler.class).heightMm() <= b.getSingleComponent(ComponentType.CASE, Case.class).maxCoolerHeightMm(),
            Set.of(ComponentType.COOLING, ComponentType.CASE)
    );

    public static BuildSpecification radiatorFitsCaseSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CASE, Case.class).supportedRadiatorsMm().contains(b.getSingleComponent(ComponentType.COOLING, Cooler.class).radiatorSizeMm()),
            Set.of(ComponentType.COOLING, ComponentType.CASE)
    );

    // ==================== Storage ====================

    public static BuildSpecification m2SlotsSufficientSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.STORAGE, Storage.class).stream().filter(d -> d.storageTech() == StorageTech.M2_SATA || d.storageTech() == StorageTech.M2_NVME).count() <= b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).m2Slots(),
            Set.of(ComponentType.STORAGE, ComponentType.MOTHERBOARD)
    );

    // M.2 SATA drives use an M.2 slot, not a SATA port: only StorageTech.SATA counts here
    public static BuildSpecification sataPortsSufficientSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.STORAGE, Storage.class).stream().filter(d -> d.storageTech() == StorageTech.SATA).count() <= b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).sataPorts(),
            Set.of(ComponentType.STORAGE, ComponentType.MOTHERBOARD)
    );

    public static BuildSpecification driveBaysSufficientSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.STORAGE, Storage.class).stream().filter(d -> d.storageTech() == StorageTech.SATA).count() <= b.getSingleComponent(ComponentType.CASE, Case.class).driveBays(),
            Set.of(ComponentType.STORAGE, ComponentType.CASE)
    );

    public static BuildSpecification hasSolidStateDriveSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.STORAGE, Storage.class).stream().anyMatch(d -> d.storageType() == StorageType.SSD),
            Set.of(ComponentType.STORAGE)
    );

    // ==================== Power budget (the ones reading the total wattage depend on every component type) ====================

    public static BuildSpecification psuSufficientWattsSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).wattage() >= b.totalWattage(),
            EnumSet.allOf(ComponentType.class)
    );

    public static BuildSpecification psuHeadroomSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).wattage() >= b.totalWattage() * PSU_HEADROOM_FACTOR,
            EnumSet.allOf(ComponentType.class)
    );

    public static BuildSpecification psuEfficiencyAdequateSpecification = new PredicateSpecification(
            b -> b.totalWattage() <= HIGH_LOAD_WATTS
                    || b.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).efficencyRating().compareTo(HIGH_LOAD_MIN_EFFICIENCY) >= 0,
            EnumSet.allOf(ComponentType.class)
    );

    public static BuildSpecification psuCoversGpuRecommendationSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.GPU, Gpu.class).stream().allMatch(g -> b.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).wattage() >= g.recommendedPsuWatts()),
            Set.of(ComponentType.POWER_SUPPLY, ComponentType.GPU)
    );

    public static BuildSpecification psuConnectorsSufficientSpecification = new PredicateSpecification(
            b -> b.getComponents(ComponentType.GPU, Gpu.class).stream().mapToInt(Gpu::powerConnectors).sum() <= b.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).pcieConnectors(),
            Set.of(ComponentType.POWER_SUPPLY, ComponentType.GPU)
    );

}