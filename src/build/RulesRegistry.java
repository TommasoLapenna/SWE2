package build;

import build.rule.Severity;
import factory.Factory;
import model.attribute.ComponentType;
import specification.Specification;

import java.util.*;

public class RulesRegistry implements Factory {
    private Map<String, BuildRule> rules = new LinkedHashMap<>();

    public BuildRule create(int id, String name, Specification<PcBuilder> specification, Severity severity, String message, ComponentType... types) {
            return new BuildRule(id, name, specification, severity, message, Set.of(types));
    }

    /*
    public BuildRule createGuarded(int id, String name, Specification<PcBuilder> specification, Severity severity, String message, ComponentType... types) {
            return new BuildRule(id, name, specification, severity, message, Set.of(types));
    }
    */

    public BuildRule create(int id, String name, BuildRule guard, Specification<PcBuilder> specification, Severity severity, String message, ComponentType... types) {
        Set<ComponentType> totalComponents = new HashSet<>(Set.of(types));
        totalComponents.addAll(guard.involvedComponents());
        Specification<PcBuilder> totalSpecification = guard.specification().implies(specification);

        return new BuildRule(id, name, totalSpecification, severity, message, totalComponents);
    }

    public BuildRule create(int id, String name, Specification<PcBuilder> guard, Specification<PcBuilder> specification, Severity severity, String message, ComponentType... types) {
        Specification<PcBuilder> totalSpecification = guard.implies(specification);

        return new BuildRule(id, name, totalSpecification, severity, message, Set.of(types));
    }

    public BuildRule get(String name) {
        BuildRule rule = rules.get(name);
        if(rule == null)
            throw new IllegalArgumentException("No rule with name " + name);
        return rule;
    }

    public Specification<PcBuilder> specification(String name) {
        BuildRule rule = rules.get(name);
        if(rule == null)
            throw new IllegalArgumentException("No rule with name " + name);
        return rule.specification();
    }
    public void addRule(BuildRule rule) {
        rules.put(rule.name(), rule);
    }

    public void createAndAdd(int id, String name, Specification<PcBuilder> specification, Severity severity, String message, ComponentType... types) {
        addRule(create(id, name, specification, severity, message, types));
    }

    public void createAndAdd(int id, String name, BuildRule guard, Specification<PcBuilder> specification, Severity severity, String message, ComponentType... types) {
        addRule(create(id, name, guard, specification, severity, message, types));
    }

    public void createAndAdd(int id, String name, Specification<PcBuilder> guard, Specification<PcBuilder> specification, Severity severity, String message, ComponentType... types){
        addRule(create(id, name, guard, specification, severity, message, types));
    }

    public List<BuildRule> rules() {
        return new ArrayList<>(rules.values());
    }
}
