package application;

import build.rule.BuildRule;
import build.rule.Severity;
import build.specification.Specs;
import model.attribute.ComponentType;

import java.util.List;

public final class DefaultRules {
    public static List<BuildRule> create() {
        return List.of(
                // ==================== CARDINALITY / COMPLETENESS ====================
                new BuildRule(
                        1,
                        "MISSING-CPU",
                        Specs.atLeast(ComponentType.CPU, 1),
                        Severity.ERROR,
                        "At least one CPU is required"
                ),
                new BuildRule(
                        2,
                        "TOO-MANY-CPUS",
                        Specs.atMost(ComponentType.CPU, 1),
                        Severity.ERROR,
                        "Only one CPU is allowed, found: {CPU}"
                ),
                new BuildRule(
                        3,
                        "MISSING-MOBO",
                        Specs.atLeast(ComponentType.MOTHERBOARD, 1),
                        Severity.WARNING,
                        "At least one Motherboard is required"
                ),
                new BuildRule(
                        4,
                        "TOO-MANY-MOBOS",
                        Specs.atMost(ComponentType.MOTHERBOARD, 1),
                        Severity.ERROR,
                        "Only one motherboard is allowed, found: {MOTHERBOARD}"
                ),
                new BuildRule(
                        5,
                        "MISSING-PSU",
                        Specs.atLeast(ComponentType.POWER_SUPPLY, 1),
                        Severity.WARNING,
                        "At least one Power Supply is required"
                ),
                new BuildRule(
                        6,
                        "TOO-MANY-PSUS",
                        Specs.atMost(ComponentType.POWER_SUPPLY, 1),
                        Severity.ERROR,
                        "Only one Power Supply is allowed, found: {POWER_SUPPLY}"
                ),
                new BuildRule(
                        7,
                        "MISSING-RAM",
                        Specs.atLeast(ComponentType.RAM, 1),
                        Severity.ERROR,
                        "Ram is missing"
                ),
                new BuildRule(
                        8,
                        "MISSING-STORAGE",
                        Specs.atLeast(ComponentType.STORAGE, 1),
                        Severity.WARNING,
                        "Storage is missing"
                ),
                new BuildRule(
                        9,
                        "MISSING-CASE",
                        Specs.atLeast(ComponentType.CASE, 1),
                        Severity.ERROR,
                        "Case is missing"
                ),
                new BuildRule(
                        10,
                        "TOO-MANY-CASES",
                        Specs.atMost(ComponentType.CASE, 1),
                        Severity.ERROR,
                        "Only one case allowed, found: {CASE}"
                ),
                new BuildRule(
                        11,
                        "TOO-MANY-COOLERS",
                        Specs.atMost(ComponentType.COOLING, 1),
                        Severity.ERROR,
                        "Only one CPU cooler is allowed, found: {COOLING}"
                ),
                new BuildRule(
                        12,
                        "MISSING-COOLER",
                        Specs.mutipleExcatclyOne(ComponentType.CPU).implies(Specs.cpuHasBundledCoolerSpecification.or(Specs.atLeast(ComponentType.COOLING, 1))),
                        Severity.WARNING,
                        "CPU {CPU} ships without a cooler and none was selected"
                ),
                new BuildRule(
                        13,
                        "MISSING-DISPLAY-OUTPUT",
                        Specs.mutipleExcatclyOne(ComponentType.CPU).implies(Specs.atLeast(ComponentType.GPU, 1).or(Specs.cpuHasIntegratedGpuSpecification)),
                        Severity.WARNING,
                        "CPU {CPU} has no integrated graphics and no GPU was selected"
                ),
                // ==================== PLATFORM: CPU / MOTHERBOARD / MEMORY / COOLER ====================
                new BuildRule(
                        14,
                        "SOCKET-MISMATCH",
                        Specs.mutipleExcatclyOne(ComponentType.CPU, ComponentType.MOTHERBOARD).implies(Specs.socketMatchSpecification),
                        Severity.ERROR,
                        "CPU {CPU} and Motherboard {MOTHERBOARD} socket must match"
                ),
                new BuildRule(
                        15,
                        "CPU-MEMORY-GEN-MISMATCH",
                        Specs.mutipleExcatclyOne(ComponentType.CPU, ComponentType.MOTHERBOARD).implies(Specs.cpuSupportsBoardMemoryGenSpecification),
                        Severity.ERROR,
                        "CPU {CPU} does not support the memory generation of motherboard {MOTHERBOARD}"
                ),
                new BuildRule(
                        16,
                        "RAM-BOARD-GEN-MISMATCH",
                        Specs.mutipleExcatclyOne(ComponentType.MOTHERBOARD).implies(Specs.ramMatchesBoardGenSpecification),
                        Severity.ERROR,
                        "RAM {RAM} is not the memory generation supported by motherboard {MOTHERBOARD}"
                ),
                new BuildRule(
                        17,
                        "RAM-CPU-GEN-MISMATCH",
                        Specs.mutipleExcatclyOne(ComponentType.CPU).implies(Specs.ramMatchesCpuGenSpecification),
                        Severity.ERROR,
                        "RAM {RAM} is not a memory generation supported by CPU {CPU}"
                ),
                new BuildRule(
                        18,
                        "RAM-SLOTS-EXCEEDED",
                        Specs.mutipleExcatclyOne(ComponentType.MOTHERBOARD).implies(Specs.ramSlotsSufficientSpecification),
                        Severity.ERROR,
                        "RAM {RAM} needs more slots than motherboard {MOTHERBOARD} provides"
                ),
                new BuildRule(
                        19,
                        "RAM-CAPACITY-EXCEEDED",
                        Specs.mutipleExcatclyOne(ComponentType.MOTHERBOARD).implies(Specs.ramCapacityWithinBoardLimitSpecification),
                        Severity.ERROR,
                        "RAM {RAM} exceeds the maximum capacity of motherboard {MOTHERBOARD}"
                ),
                new BuildRule(
                        20,
                        "RAM-SPEED-ABOVE-BOARD-LIMIT",
                        Specs.mutipleExcatclyOne(ComponentType.MOTHERBOARD).implies(Specs.ramSpeedWithinBoardLimitSpecification),
                        Severity.WARNING,
                        "RAM {RAM} is rated above motherboard {MOTHERBOARD} and will be downclocked"
                ),
                new BuildRule(
                        21,
                        "RAM-KITS-NOT-HOMOGENEOUS",
                        Specs.ramKitsHomogeneousSpecification,
                        Severity.MESSAGE,
                        "Memory kits differ in speed or module size and will run at the lowest common setting: {RAM}"
                ),
                new BuildRule(
                        22,
                        "RAM-NOT-DUAL-CHANNEL",
                        Specs.atLeast(ComponentType.RAM, 1).implies(Specs.ramDualChannelSpecification),
                        Severity.MESSAGE,
                        "RAM {RAM} does not form a dual-channel configuration"
                ),
                new BuildRule(
                        23,
                        "COOLER-SOCKET-MISMATCH",
                        Specs.mutipleExcatclyOne(ComponentType.CPU, ComponentType.COOLING).implies(Specs.coolerSocketSupportSpecification),
                        Severity.ERROR,
                        "Cooler {COOLING} does not support the socket of CPU {CPU}"
                ),
                new BuildRule(
                        24,
                        "COOLER-TDP-INSUFFICIENT",
                        Specs.mutipleExcatclyOne(ComponentType.CPU, ComponentType.COOLING).implies(Specs.coolerTdpSufficientSpecification),
                        Severity.WARNING,
                        "Cooler {COOLING} is rated below the TDP of CPU {CPU}"
                ),
                // ==================== PHYSICAL FIT AND EXPANSION ====================
                new BuildRule(
                        25,
                        "FORM-FACTOR-MISMATCH",
                        Specs.mutipleExcatclyOne(ComponentType.MOTHERBOARD, ComponentType.CASE).implies(Specs.formFactorMatchSpecification),
                        Severity.ERROR,
                        "Motherboard {MOTHERBOARD} form factor must match case {CASE}"
                ),
                new BuildRule(
                        26,
                        "PSU-FORM-FACTOR-MISMATCH",
                        Specs.mutipleExcatclyOne(ComponentType.POWER_SUPPLY, ComponentType.CASE).implies(Specs.psuFormFactorFitsCaseSpecification),
                        Severity.ERROR,
                        "Power supply {POWER_SUPPLY} form factor is not supported by case {CASE}"
                ),
                new BuildRule(
                        27,
                        "GPU-TOO-LONG",
                        Specs.mutipleExcatclyOne(ComponentType.CASE).implies(Specs.gpuLengthFitsCaseSpecification),
                        Severity.ERROR,
                        "GPU {GPU} is too long for case {CASE}"
                ),
                new BuildRule(
                        28,
                        "GPU-SLOTS-EXCEED-CASE",
                        Specs.mutipleExcatclyOne(ComponentType.CASE).implies(Specs.gpuSlotsFitCaseSpecification),
                        Severity.ERROR,
                        "GPU {GPU} occupies more expansion slots than case {CASE} offers"
                ),
                new BuildRule(
                        29,
                        "GPU-COUNT-EXCEEDS-PCIE-SLOTS",
                        Specs.mutipleExcatclyOne(ComponentType.MOTHERBOARD).implies(Specs.gpuCountFitsBoardSpecification),
                        Severity.ERROR,
                        "GPU {GPU} needs more PCIe x16 slots than motherboard {MOTHERBOARD} provides"
                ),
                new BuildRule(
                        30,
                        "COOLER-TOO-TALL",
                        Specs.mutipleExcatclyOne(ComponentType.COOLING, ComponentType.CASE).implies(Specs.coolerIsAirSpecification.implies(Specs.airCoolerHeightFitsCaseSpecification)),
                        Severity.ERROR,
                        "Air cooler {COOLING} is taller than case {CASE} allows"
                ),
                new BuildRule(
                        31,
                        "RADIATOR-UNSUPPORTED",
                        Specs.mutipleExcatclyOne(ComponentType.COOLING, ComponentType.CASE).implies(Specs.coolerIsLiquidSpecification.implies(Specs.radiatorFitsCaseSpecification)),
                        Severity.ERROR,
                        "Radiator of cooler {COOLING} is not supported by case {CASE}"
                ),
                // ==================== STORAGE ====================
                new BuildRule(
                        32,
                        "M2-SLOTS-EXCEEDED",
                        Specs.mutipleExcatclyOne(ComponentType.MOTHERBOARD).implies(Specs.m2SlotsSufficientSpecification),
                        Severity.ERROR,
                        "Storage {STORAGE} needs more M.2 slots than motherboard {MOTHERBOARD} offers"
                ),
                new BuildRule(
                        33,
                        "SATA-PORTS-EXCEEDED",
                        Specs.mutipleExcatclyOne(ComponentType.MOTHERBOARD).implies(Specs.sataPortsSufficientSpecification),
                        Severity.ERROR,
                        "Storage {STORAGE} needs more SATA ports than motherboard {MOTHERBOARD} offers"
                ),
                new BuildRule(
                        34,
                        "DRIVE-BAYS-EXCEEDED",
                        Specs.mutipleExcatclyOne(ComponentType.CASE).implies(Specs.driveBaysSufficientSpecification),
                        Severity.ERROR,
                        "Storage {STORAGE} needs more drive bays than case {CASE} offers"
                ),
                new BuildRule(
                        35,
                        "NO-SOLID-STATE-DRIVE",
                        Specs.atLeast(ComponentType.STORAGE, 1).implies(Specs.hasSolidStateDriveSpecification),
                        Severity.MESSAGE,
                        "Storage {STORAGE} contains no SSD: a solid-state boot drive is recommended"
                ),
                // ==================== POWER BUDGET ====================
                new BuildRule(
                        36,
                        "PSU-SUFFICIENT-WATTS",
                        Specs.exactly(ComponentType.POWER_SUPPLY, 1).implies(Specs.psuSufficientWattsSpecification),
                        Severity.ERROR,
                        "Power supply must provide sufficient watts"
                ),
                new BuildRule(
                        37,
                        "PSU-LOW-HEADROOM",
                        Specs.mutipleExcatclyOne(ComponentType.POWER_SUPPLY).implies(Specs.psuSufficientWattsSpecification.implies(Specs.psuHeadroomSpecification)),
                        Severity.WARNING,
                        "Power supply {POWER_SUPPLY} leaves less than the recommended headroom over the total draw"
                ),
                new BuildRule(
                        38,
                        "PSU-BELOW-GPU-RECOMMENDATION",
                        Specs.mutipleExcatclyOne(ComponentType.POWER_SUPPLY).implies(Specs.psuCoversGpuRecommendationSpecification),
                        Severity.WARNING,
                        "Power supply {POWER_SUPPLY} is below the recommended wattage of GPU {GPU}"
                ),
                new BuildRule(
                        39,
                        "PSU-PCIE-CONNECTORS-INSUFFICIENT",
                        Specs.mutipleExcatclyOne(ComponentType.POWER_SUPPLY).implies(Specs.psuConnectorsSufficientSpecification),
                        Severity.ERROR,
                        "Power supply {POWER_SUPPLY} has fewer PCIe power connectors than GPU {GPU} requires"
                ),
                new BuildRule(
                        40,
                        "PSU-EFFICIENCY-LOW-FOR-LOAD",
                        Specs.mutipleExcatclyOne(ComponentType.POWER_SUPPLY).implies(Specs.psuEfficiencyAdequateSpecification),
                        Severity.MESSAGE,
                        "Power supply {POWER_SUPPLY} has a low efficiency rating for a build of this wattage"
                )
        );
    }
}