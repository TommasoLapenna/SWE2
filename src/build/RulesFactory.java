package build;

import build.build_specification.BuildSpecification;
import factory.Factory;

import java.util.*;

public class RulesFactory implements Factory {
    //private Map<String, BuildRule> rules = new LinkedHashMap<>();

    public static BuildRule create(int id, String name, BuildSpecification specification, Severity severity, String message) {
            return new BuildRule(id, name, specification, severity, message);
    }

    public static BuildRule create(int id, String name, BuildRule guard, BuildSpecification specification, Severity severity, String message) {
        // FIXED: was guard.specification().implies(specification). When the guard rule is itself guarded
        // (e.g. SINGLE-CPU = atLeast(1) -> atMost(1)) its specification is vacuously true when its own guard fails
        // (0 CPUs), so the new rule was evaluated anyway and getSingleComponent threw.
        // The guard form (atLeast(1) AND atMost(1) = exactly one CPU) is used instead
        return create(id, name, guard.guardSpecification(), specification, severity, message);
    }

    public static BuildRule create(int id, String name, BuildSpecification guard, BuildSpecification specification, Severity severity, String message) {
        BuildSpecification totalSpecification = guard.implies(specification);
        // How this rule behaves when used as a guard: it must apply (guard) AND be satisfied (specification)
        BuildSpecification guardSpecification = guard.and(specification);

        return new BuildRule(id, name, totalSpecification, severity, message, guardSpecification);
    }

    /*
    public BuildRule get(String name) {
        BuildRule rule = rules.get(name);
        if(rule == null)
            throw new IllegalArgumentException("No rule with name " + name);
        return rule;
    }
     */

    /*
    public BuildSpecification specification(String name) {
        BuildRule rule = rules.get(name);
        if(rule == null)
            throw new IllegalArgumentException("No rule with name " + name);
        return rule.specification();
    }
    public void addRule(BuildRule rule) {
        rules.put(rule.name(), rule);
    }
     */

    /*
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

     */
}
