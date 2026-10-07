package build;

import build.rule.*;
import model.attribute.ComponentType;
import model.component.Component;
import observer.Observer;


import java.util.*;

public class CompatibilityChecker implements Observer<BuildEvent> {
    private final PcBuilder build;
    private RulesRegistry registry = new RulesRegistry();
    private List<Violation> violations = new ArrayList<>();

    public CompatibilityChecker(PcBuilder build, List<BuildRule> rules) {
        this.build = build;
        //this.rules = rules;

        registry.createAndAdd(
                1,
                "SINGLE-CPU",
                Specs.exactly(ComponentType.CPU, 1),
                Severity.ERROR,
                "Only one CPU is allowed",
                ComponentType.CPU
        );

        registry.createAndAdd(
                2,
                "SINGLE-MOBO",
                Specs.exactly(ComponentType.MOTHERBOARD, 1),
                Severity.ERROR,
                "Only one motherboard is allowed",
                ComponentType.MOTHERBOARD
        );

        registry.createAndAdd(
                3,
                "SINGLE-PSU",
                Specs.exactly(ComponentType.POWER_SUPPLY, 1),
                Severity.ERROR,
                "Only one power supply is allowed",
                ComponentType.POWER_SUPPLY
        );

        registry.createAndAdd(
                4,
                "PSU-SUFFICIENT-WATTS",
                registry.get("SINGLE-PSU"),
                Specs.psuSufficientWattsSpecification,
                Severity.ERROR,
                "Power supply must provide sufficient watts",
                ComponentType.POWER_SUPPLY
        );

        registry.createAndAdd(
                5,
                "SOCKET-MATCH",
                registry.specification("SINGLE-CPU").and(registry.specification("SINGLE-MOBO")),
                Specs.socketMatchSpecification,
                Severity.ERROR,
                "CPU and motherboard socket must match",
                ComponentType.CPU,
                ComponentType.MOTHERBOARD
        );

    }

    @Override
    public void update(BuildEvent event) {
            //TODO Base version, specific checks based on events to be implemented
    }

    public void evaluate(){
        Map<ComponentType, List<Component>> components = new LinkedHashMap<>();
        violations.clear();
        for(BuildRule rule : registry.rules()) {
            components.clear();
            if(!rule.isSatisfiedBy(build)) {
                for(ComponentType type : rule.involvedComponents()) {
                    components.put(type, build.getComponents(type));
                }
                violations.add(new Violation(rule.name(), rule.message(), components));
            }
        }
    }

    public List<Violation> violations() {
        return violations;
    }
}
