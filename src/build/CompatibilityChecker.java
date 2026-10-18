package build;

import build.specification.Specs;
import build.rule.*;
import model.attribute.ComponentType;
import model.component.Component;
import pattern.observer.Observer;


import java.util.*;

public class CompatibilityChecker implements Observer<BuildEvent> {
    private final PcBuilder build;
    private final RulesRegistry registry = new RulesRegistry();
    private final ViolationList violations = new ViolationList();

    public CompatibilityChecker(PcBuilder build, List<BuildRule> rules) {
        this.build = build;
        build.attach(this);
        // This lambda is the equivalent of r -> registry.add(r), it was suggested by the IDE
        rules.forEach(registry::add);
        evaluateAll();
    }

    @Override
    public void update(BuildEvent event) {
        if (event.type() == BuildEventType.RESTORED)
            evaluateAll();
        else {
            for (BuildRule rule : registry.lookup(Set.of(event.componentType()))) {
                reevaluate(rule);
            }
        }
    }

    private void evaluateAll() {
        for (BuildRule rule : registry.rules())
            reevaluate(rule);
    }

    private void reevaluate(BuildRule rule) {
        if (!rule.isSatisfiedBy(build)) {
            Map<ComponentType, List<Component>> involved = new LinkedHashMap<>();
            for (ComponentType type : rule.involvedComponents()) {
                involved.put(type, List.copyOf(build.getComponents(type)));
            }
            violations.add(new Violation(rule.name(), rule.severity(), rule.message(), involved));
        } else
            violations.clear(rule.name());
    }

    public List<Violation> violations() {
        return violations.violations();
    }
}
