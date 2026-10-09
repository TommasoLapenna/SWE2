package build;

import model.attribute.ComponentType;
import model.component.Component;
import observer.Observer;


import java.util.*;

public class CompatibilityChecker implements Observer<BuildEvent> {
    private final PcBuilder build;
    private RulesRegistry registry = new RulesRegistry();
    private List<Violation> violations = new ArrayList<>();

    public CompatibilityChecker(PcBuilder build) {
        this.build = build;
        build.attach(this);
        // The SINGLE-* rules are NOT to be used as guards, for the way the logic is evaluated

        registry.createAndAdd(
                1,
                "MISSING-CPU",
                Specs.atLeast(ComponentType.CPU, 1),
                Severity.WARNING,
                "At least one CPU is required"
        );

        registry.createAndAdd(
                1,
                "SINGLE-CPU",
                registry.get("MISSING-CPU"),
                Specs.atMost(ComponentType.CPU, 1),
                Severity.ERROR,
                "Only one CPU is allowed, found: {CPU}"
        );

        registry.createAndAdd(
                1,
                "MISSING-MOBO",
                Specs.atLeast(ComponentType.MOTHERBOARD, 1),
                Severity.WARNING,
                "At least one Motherboard is required"
        );

        registry.createAndAdd(
                2,
                "SINGLE-MOBO",
                registry.specification("MISSING-MOBO"),
                Specs.atMost(ComponentType.MOTHERBOARD, 1),
                Severity.ERROR,
                "Only one motherboard is allowed, found: {MOTHERBOARD}"
        );

        registry.createAndAdd(
                1,
                "MISSING-PSU",
                Specs.atLeast(ComponentType.POWER_SUPPLY, 1),
                Severity.WARNING,
                "At least one Power Supply is required"
        );

        registry.createAndAdd(
                6,
                "SINGLE-PSU",
                registry.specification("MISSING-PSU"),
                Specs.atMost(ComponentType.POWER_SUPPLY, 1),
                Severity.ERROR,
                "Only one Power Supply is allowed, found: {POWER_SUPPLY}"
        );

        registry.createAndAdd(
                6,
                "MISSING-RAM",
                Specs.atLeast(ComponentType.RAM, 1),
                Severity.ERROR,
                "Ram is missing"
        );

        registry.createAndAdd(
                7,
                "MISSING-STORAGE",
                Specs.atLeast(ComponentType.STORAGE, 1),
                Severity.ERROR,
                "Storage is missing"
        );

        registry.createAndAdd(
                8,
                "MISSING-CASE",
                Specs.atLeast(ComponentType.CASE, 1),
                Severity.ERROR,
                "Case is missing"
        );

        registry.createAndAdd(
                8,
                "SINGLE-CASE",
                registry.specification("MISSING-CASE"),
                Specs.atMost(ComponentType.CASE, 1),
                Severity.ERROR,
                "Only one case allowed, found: {CASE}"
        );

        registry.createAndAdd(
                7,
                "PSU-SUFFICIENT-WATTS",
                Specs.exactly(ComponentType.POWER_SUPPLY, 1),
                Specs.psuSufficientWattsSpecification,
                Severity.ERROR,
                "Power supply must provide sufficient watts"
        );

        registry.createAndAdd(
                5,
                "SOCKET-MATCH",
                Specs.exactly(ComponentType.CPU, 1).and(Specs.exactly(ComponentType.MOTHERBOARD, 1)),
                Specs.socketMatchSpecification,
                Severity.ERROR,
                "CPU {CPU} and Motherboard {MOTHERBOARD} socket must match"
        );

    }

    @Override
    public void update(BuildEvent event) {
        System.out.println("===========================================================================================");
        violations.clear();
            //TODO Base version, specific checks based on events to be implemented
        evaluate();
    }

    public void evaluate(){
        for (BuildRule rule : registry.rules()) {
            if (!rule.isSatisfiedBy(build)) {
                Map<ComponentType, List<Component>> involved = new LinkedHashMap<>();
                for (ComponentType type : rule.involvedComponents()) {
                    involved.put(type, List.copyOf(build.getComponents(type)));
                }
                violations.add(new Violation(rule.name(), rule.message(), involved));
            }
        }
    }

    public List<Violation> violations() {
        return violations;
    }
}
