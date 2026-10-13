package build;

import model.attribute.ComponentType;
import model.component.Component;
import observer.Observer;


import java.util.*;

public class CompatibilityChecker implements Observer<BuildEvent> {
    private final PcBuilder build;
    private RulesRegistry registry = new RulesRegistry();
    private ViolationList violations = new ViolationList();

    public CompatibilityChecker(PcBuilder build) {
        this.build = build;
        build.attach(this);
        // The SINGLE-* rules are NOT to be used as guards, for the way the logic is evaluated
        // UPDATE: now they can. A guarded rule is "guard -> check", vacuously true when the guard fails
        // (SINGLE-CPU holds with 0 CPUs), which is why it broke as a guard. Rules now also carry a guard form,
        // "guard AND check" (SINGLE-CPU -> exactly one CPU), used when passing a BuildRule as guard
        // (registry.get(...)) or via registry.guard(...); registry.specification(...) is still the plain implication

        registry.add(RulesFactory.create(
                1,
                "MISSING-CPU",
                Specs.atLeast(ComponentType.CPU, 1),
                Severity.WARNING,
                "At least one CPU is required"
        ));

        registry.add(RulesFactory.create(
                1,
                "SINGLE-CPU",
                registry.get("MISSING-CPU"),
                Specs.atMost(ComponentType.CPU, 1),
                Severity.ERROR,
                "Only one CPU is allowed, found: {CPU}"
        ));

        registry.add(RulesFactory.create(
                1,
                "MISSING-MOBO",
                Specs.atLeast(ComponentType.MOTHERBOARD, 1),
                Severity.WARNING,
                "At least one Motherboard is required"
        ));

        registry.add(RulesFactory.create(
                2,
                "SINGLE-MOBO",
                registry.specification("MISSING-MOBO"),
                Specs.atMost(ComponentType.MOTHERBOARD, 1),
                Severity.ERROR,
                "Only one motherboard is allowed, found: {MOTHERBOARD}"
        ));

        registry.add(RulesFactory.create(
                1,
                "MISSING-PSU",
                Specs.atLeast(ComponentType.POWER_SUPPLY, 1),
                Severity.WARNING,
                "At least one Power Supply is required"
        ));

        registry.add(RulesFactory.create(
                6,
                "SINGLE-PSU",
                registry.specification("MISSING-PSU"),
                Specs.atMost(ComponentType.POWER_SUPPLY, 1),
                Severity.ERROR,
                "Only one Power Supply is allowed, found: {POWER_SUPPLY}"
        ));

        registry.add(RulesFactory.create(
                6,
                "MISSING-RAM",
                Specs.atLeast(ComponentType.RAM, 1),
                Severity.ERROR,
                "Ram is missing"
        ));

        registry.add(RulesFactory.create(
                7,
                "MISSING-STORAGE",
                Specs.atLeast(ComponentType.STORAGE, 1),
                Severity.ERROR,
                "Storage is missing"
        ));

        registry.add(RulesFactory.create(
                8,
                "MISSING-CASE",
                Specs.atLeast(ComponentType.CASE, 1),
                Severity.ERROR,
                "Case is missing"
        ));

        registry.add(RulesFactory.create(
                8,
                "SINGLE-CASE",
                registry.specification("MISSING-CASE"),
                Specs.atMost(ComponentType.CASE, 1),
                Severity.ERROR,
                "Only one case allowed, found: {CASE}"
        ));

        registry.add(RulesFactory.create(
                7,
                "PSU-SUFFICIENT-WATTS",
                // previously: Specs.exactly(ComponentType.POWER_SUPPLY, 1), now guarded by the previous rule
                registry.get("SINGLE-PSU"),
                Specs.psuSufficientWattsSpecification,
                Severity.ERROR,
                "Power supply must provide sufficient watts"
        ));

        registry.add(RulesFactory.create(
                5,
                "SOCKET-MATCH",
                // previously: Specs.exactly(ComponentType.CPU, 1).and(Specs.exactly(ComponentType.MOTHERBOARD, 1)),
                // more than one guard rule -> combine their guard forms with and()
                registry.guard("SINGLE-CPU").and(registry.guard("SINGLE-MOBO")),
                Specs.socketMatchSpecification,
                Severity.ERROR,
                "CPU {CPU} and Motherboard {MOTHERBOARD} socket must match"
        ));

        // ============================================================================================================
        // ADDITIONAL COMPATIBILITY RULES (for testing)
        // All guarded by the previous rules: a check that needs a single CPU / MOTHERBOARD / CASE / POWER_SUPPLY is
        // only evaluated when exactly one is present (otherwise the SINGLE-* / MISSING-* rules already report it).
        // GPU / RAM / STORAGE / COOLING can be many: the specs check all of them, or their sums / counts
        // ============================================================================================================

        // --- Memory ---

        registry.add(RulesFactory.create(
                13,
                "MOBO-RAM-GEN",
                registry.get("SINGLE-MOBO"),
                Specs.moboRamGenSpecification,
                Severity.ERROR,
                "RAM {RAM} generation is not supported by motherboard {MOTHERBOARD}"
        ));

        registry.add(RulesFactory.create(
                14,
                "CPU-RAM-GEN",
                registry.get("SINGLE-CPU"),
                Specs.cpuRamGenSpecification,
                Severity.ERROR,
                "RAM {RAM} generation is not supported by CPU {CPU}"
        ));

        registry.add(RulesFactory.create(
                15,
                "RAM-SLOTS",
                registry.get("SINGLE-MOBO"),
                Specs.ramSlotsSpecification,
                Severity.ERROR,
                "Not enough RAM slots on {MOTHERBOARD} for all the sticks of: {RAM}"
        ));

        registry.add(RulesFactory.create(
                16,
                "RAM-MAX-CAPACITY",
                registry.get("SINGLE-MOBO"),
                Specs.ramMaxCapacitySpecification,
                Severity.ERROR,
                "Total RAM capacity of {RAM} exceeds the maximum supported by {MOTHERBOARD}"
        ));

        // WARNING: the RAM still works, just downclocked
        registry.add(RulesFactory.create(
                17,
                "RAM-SPEED",
                registry.get("SINGLE-MOBO"),
                Specs.ramSpeedSpecification,
                Severity.WARNING,
                "RAM {RAM} will run slower than its rated frequency on {MOTHERBOARD}"
        ));

        // --- Case ---

        registry.add(RulesFactory.create(
                18,
                "CASE-MOBO-FORM-FACTOR",
                registry.guard("SINGLE-CASE").and(registry.guard("SINGLE-MOBO")),
                Specs.caseMoboFormFactorSpecification,
                Severity.ERROR,
                "Case {CASE} does not fit motherboard {MOTHERBOARD}"
        ));

        registry.add(RulesFactory.create(
                19,
                "CASE-PSU-FORM-FACTOR",
                registry.guard("SINGLE-CASE").and(registry.guard("SINGLE-PSU")),
                Specs.casePsuFormFactorSpecification,
                Severity.ERROR,
                "Case {CASE} does not fit power supply {POWER_SUPPLY}"
        ));

        // --- GPU ---

        registry.add(RulesFactory.create(
                20,
                "GPU-LENGTH",
                registry.get("SINGLE-CASE"),
                Specs.gpuLengthSpecification,
                Severity.ERROR,
                "GPU {GPU} too long for case {CASE}"
        ));

        registry.add(RulesFactory.create(
                21,
                "GPU-PCIE-SLOTS",
                registry.get("SINGLE-MOBO"),
                Specs.gpuPcieSlotsSpecification,
                Severity.ERROR,
                "Not enough PCIe x16 slots on {MOTHERBOARD} for GPUs: {GPU}"
        ));

        registry.add(RulesFactory.create(
                22,
                "GPU-PSU-CONNECTORS",
                registry.get("SINGLE-PSU"),
                Specs.gpuPsuConnectorsSpecification,
                Severity.ERROR,
                "Power supply {POWER_SUPPLY} has not enough PCIe connectors for GPUs: {GPU}"
        ));

        // --- Cooling ---

        registry.add(RulesFactory.create(
                23,
                "COOLER-SOCKET",
                registry.get("SINGLE-CPU"),
                Specs.coolerSocketSpecification,
                Severity.ERROR,
                "Cooler {COOLING} does not support the socket of CPU {CPU}"
        ));

        registry.add(RulesFactory.create(
                24,
                "COOLER-HEIGHT",
                registry.get("SINGLE-CASE"),
                Specs.coolerHeightSpecification,
                Severity.ERROR,
                "Air cooler {COOLING} too tall for case {CASE}"
        ));

        // WARNING, like the MISSING-* rules
        registry.add(RulesFactory.create(
                25,
                "CPU-NOT-COOLED",
                registry.get("SINGLE-CPU"),
                Specs.cpuCooledSpecification,
                Severity.WARNING,
                "CPU {CPU} has no bundled cooler and no cooler was added"
        ));

        // --- Storage ---

        registry.add(RulesFactory.create(
                26,
                "M2-SLOTS",
                registry.get("SINGLE-MOBO"),
                Specs.m2SlotsSpecification,
                Severity.ERROR,
                "Not enough M.2 slots on {MOTHERBOARD} for drives: {STORAGE}"
        ));

        // ============================================================================================================
        // END OF ADDITIONAL RULES
        // ============================================================================================================

        evaluateAll();

    }

    @Override
    public void update(BuildEvent event) {
        for (BuildRule rule : registry.lookup(Set.of(event.componentType()))) {
            reevaluate(rule);
        }
    }

    public void evaluateAll(){
        for(BuildRule rule : registry.rules())
            reevaluate(rule);
    }

    public void reevaluate(BuildRule rule){
        if(!rule.isSatisfiedBy(build)) {
            Map<ComponentType, List<Component>> involved = new LinkedHashMap<>();
            for (ComponentType type : rule.involvedComponents()) {
                involved.put(type, List.copyOf(build.getComponents(type)));
            }
            violations.add(new Violation(rule.name(), rule.severity(), rule.message(), involved));
        }
        else
            violations.clear(rule.name());
    }

    public List<Violation> violations() {
        return violations.violations();
    }
}
