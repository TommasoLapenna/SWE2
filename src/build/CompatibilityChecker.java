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
                registry.get("TOO-MANY-CPUS"),
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
                "TOO-MANY-MOBOS",
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
                "TOO-MANY-PSUS",
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
                "TOO-MANY-CASES",
                registry.specification("MISSING-CASE"),
                Specs.atMost(ComponentType.CASE, 1),
                Severity.ERROR,
                "Only one case allowed, found: {CASE}"
        ));

        registry.add(RulesFactory.create(
                7,
                "PSU-SUFFICIENT-WATTS",
                Specs.exactly(ComponentType.POWER_SUPPLY, 1),
                Specs.psuSufficientWattsSpecification,
                Severity.ERROR,
                "Power supply must provide sufficient watts"
        ));

        registry.add(RulesFactory.create(
                5,
                "SOCKET-MATCH",
                registry.specification("MISSING-CPU").implies(
                registry.specification("SINGLE-CPU").implies(
                        registry.specification("MISSING-MOBO").implies(
                                registry.specification("SINGLE-MOBO").implies(
                                        Specs.socketMatchSpecification)))),
                Severity.ERROR,
                "CPU {CPU} and Motherboard {MOTHERBOARD} socket must match"
        ));

/*
        registry.add(RulesFactory.create(
                5,
                "SOCKET-MATCH",
                Severity.ERROR,
                "CPU {CPU} and Motherboard {MOTHERBOARD} socket must match",
                Specs.socketMatchSpecification,
                registry.specification("SINGLE-CPU"),
                registry.specification("SINGLE-MOBO")
        ));
         */
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
