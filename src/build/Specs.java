package build;

import build.build_specification.ComponentCountSpecification;
import build.build_specification.PredicateSpecification;
import build.build_specification.PsuSufficientWattsSpecification;
import build.build_specification.SocketMatchSpecification;
import factory.Factory;
import model.attribute.ComponentType;
import build.build_specification.BuildSpecification;
import model.attribute.CoolerType;
import model.attribute.StorageTech;
import model.component.*;

import java.util.Set;
import java.util.function.Predicate;

public final class Specs implements Factory {
    private Specs() {}

    static BuildSpecification atMost(ComponentType t, int n)  { return new ComponentCountSpecification(t, 0, n); }
    static BuildSpecification atLeast(ComponentType t, int n) { return new ComponentCountSpecification(t, n, Integer.MAX_VALUE); }
    static BuildSpecification exactly(ComponentType t, int n) { return new ComponentCountSpecification(t, n, n); }

    static BuildSpecification socketMatchSpecification = new SocketMatchSpecification();
    static BuildSpecification psuSufficientWattsSpecification = new PsuSufficientWattsSpecification();

    // ------------------------------------------------------------------------------------------------------------
    // Additional compatibility specifications (used by the test rules in CompatibilityChecker).
    // They assume exactly one CPU / MOTHERBOARD / CASE / POWER_SUPPLY (getSingleComponent throws otherwise),
    // so their rules MUST be guarded by the matching SINGLE-* rule. GPU / RAM / STORAGE / COOLING can be many,
    // so they are checked with allMatch / sums / counts (an empty list is trivially compatible)
    // ------------------------------------------------------------------------------------------------------------

    // Factory for lambda specs: the predicate plus the component types it reads
    static BuildSpecification of(Predicate<PcBuilder> predicate, ComponentType... dependsOn) {
        return new PredicateSpecification(predicate, Set.of(dependsOn));
    }

    // Shortcuts for the single components
    private static Cpu cpu(PcBuilder b)           { return b.getSingleComponent(ComponentType.CPU, Cpu.class); }
    private static Motherboard mobo(PcBuilder b)  { return b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class); }
    private static Case pcCase(PcBuilder b)       { return b.getSingleComponent(ComponentType.CASE, Case.class); }
    private static PowerSupply psu(PcBuilder b)   { return b.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class); }

    // Every RAM kit has the memory generation supported by the motherboard
    static BuildSpecification moboRamGenSpecification = of(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream()
                    .allMatch(m -> m.memoryGen() == mobo(b).supportedMemoryGen()),
            ComponentType.RAM, ComponentType.MOTHERBOARD);

    // Every RAM kit has a memory generation supported by the CPU
    static BuildSpecification cpuRamGenSpecification = of(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream()
                    .allMatch(m -> cpu(b).memoryGen().contains(m.memoryGen())),
            ComponentType.RAM, ComponentType.CPU);

    // Total sticks across all the RAM kits fit in the motherboard slots
    static BuildSpecification ramSlotsSpecification = of(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream()
                    .mapToInt(Memory::sticksQty).sum() <= mobo(b).ramSlots(),
            ComponentType.RAM, ComponentType.MOTHERBOARD);

    // Total RAM capacity does not exceed what the motherboard supports
    static BuildSpecification ramMaxCapacitySpecification = of(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream()
                    .mapToInt(Memory::totalCapacityGb).sum() <= mobo(b).maxMemoryGb(),
            ComponentType.RAM, ComponentType.MOTHERBOARD);

    // Every RAM kit runs at its rated frequency on the motherboard
    static BuildSpecification ramSpeedSpecification = of(
            b -> b.getComponents(ComponentType.RAM, Memory.class).stream()
                    .allMatch(m -> m.frequencyMhz() <= mobo(b).maxMemorySpeedMhz()),
            ComponentType.RAM, ComponentType.MOTHERBOARD);

    // The case supports the motherboard form factor
    static BuildSpecification caseMoboFormFactorSpecification = of(
            b -> pcCase(b).supportedForms().contains(mobo(b).formFactor()),
            ComponentType.CASE, ComponentType.MOTHERBOARD);

    // The case supports the PSU form factor
    static BuildSpecification casePsuFormFactorSpecification = of(
            b -> pcCase(b).supportedPsuForms().contains(psu(b).formFactor()),
            ComponentType.CASE, ComponentType.POWER_SUPPLY);

    // Every GPU fits in the case
    static BuildSpecification gpuLengthSpecification = of(
            b -> b.getComponents(ComponentType.GPU, Gpu.class).stream()
                    .allMatch(g -> g.lenghtMm() <= pcCase(b).maxGpuLengthMm()),
            ComponentType.GPU, ComponentType.CASE);

    // Count based: there are enough PCIe x16 slots for all the GPUs
    static BuildSpecification gpuPcieSlotsSpecification = of(
            b -> b.getComponentNumber(ComponentType.GPU) <= mobo(b).pcieX16Slots(),
            ComponentType.GPU, ComponentType.MOTHERBOARD);

    // The PSU has enough PCIe power connectors for all the GPUs
    static BuildSpecification gpuPsuConnectorsSpecification = of(
            b -> b.getComponents(ComponentType.GPU, Gpu.class).stream()
                    .mapToInt(Gpu::powerConnectors).sum() <= psu(b).pcieConnectors(),
            ComponentType.GPU, ComponentType.POWER_SUPPLY);

    // Every cooler supports the CPU socket
    static BuildSpecification coolerSocketSpecification = of(
            b -> b.getComponents(ComponentType.COOLING, Cooler.class).stream()
                    .allMatch(c -> c.supportedSocktes().contains(cpu(b).socket())),
            ComponentType.COOLING, ComponentType.CPU);

    // Every AIR cooler fits under the case side panel (LIQUID ones are not checked by height)
    static BuildSpecification coolerHeightSpecification = of(
            b -> b.getComponents(ComponentType.COOLING, Cooler.class).stream()
                    .filter(c -> c.coolingType() == CoolerType.AIR)
                    .allMatch(c -> c.heightMm() <= pcCase(b).maxCoolerHeightMm()),
            ComponentType.COOLING, ComponentType.CASE);

    // Count based: the CPU has a bundled cooler, or at least one cooler was added
    static BuildSpecification cpuCooledSpecification = of(
            b -> cpu(b).coolerIncluded() || b.hasComponent(ComponentType.COOLING),
            ComponentType.CPU, ComponentType.COOLING);

    // Count based: M.2 drives (SATA or NVMe) do not exceed the motherboard M.2 slots
    static BuildSpecification m2SlotsSpecification = of(
            b -> b.getComponents(ComponentType.STORAGE, Storage.class).stream()
                    .filter(s -> s.storageTech() != StorageTech.SATA)
                    .count() <= mobo(b).m2Slots(),
            ComponentType.STORAGE, ComponentType.MOTHERBOARD);
}
