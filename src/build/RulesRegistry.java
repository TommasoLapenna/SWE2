package build;

import build.build_specification.BuildSpecification;
import factory.Factory;
import model.attribute.ComponentType;
import specification.Specification;

import java.util.*;

public class RulesRegistry implements Factory {
    private Map<String, BuildRule> rules = new LinkedHashMap<>();

    public BuildRule create(int id, String name, BuildSpecification specification, Severity severity, String message) {
            return new BuildRule(id, name, specification, severity, message);
    }

    /*
    public BuildRule createGuarded(int id, String name, Specification<PcBuilder> specification, Severity severity, String message, ComponentType... types) {
            return new BuildRule(id, name, specification, severity, message, Set.of(types));
    }
    */

    public BuildRule create(int id, String name, BuildRule guard, BuildSpecification specification, Severity severity, String message) {
            BuildSpecification totalSpecification = guard.specification().implies(specification);

        return new BuildRule(id, name, totalSpecification, severity, message);
    }

    public BuildRule create(int id, String name, BuildSpecification guard, BuildSpecification specification, Severity severity, String message) {
        BuildSpecification totalSpecification = guard.implies(specification);

        return new BuildRule(id, name, totalSpecification, severity, message);
    }

    public BuildRule get(String name) {
        BuildRule rule = rules.get(name);
        if(rule == null)
            throw new IllegalArgumentException("No rule with name " + name);
        return rule;
    }

    public BuildSpecification specification(String name) {
        BuildRule rule = rules.get(name);
        if(rule == null)
            throw new IllegalArgumentException("No rule with name " + name);
        return rule.specification();
    }
    public void addRule(BuildRule rule) {
        rules.put(rule.name(), rule);
    }

    public void createAndAdd(int id, String name, BuildSpecification specification, Severity severity, String message) {
        addRule(create(id, name, specification, severity, message));
    }

    public void createAndAdd(int id, String name, BuildRule guard, BuildSpecification specification, Severity severity, String message) {
        addRule(create(id, name, guard, specification, severity, message));
    }

    public void createAndAdd(int id, String name, BuildSpecification guard, BuildSpecification specification, Severity severity, String message){
        addRule(create(id, name, guard, specification, severity, message));
    }

    public List<BuildRule> rules() {
        return new ArrayList<>(rules.values());
    }
}
