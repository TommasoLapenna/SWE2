package build.rule;

import data_structure.InvertedIndexMap;
import build.specification.BuildSpecification;
import model.attribute.ComponentType;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RulesRegistry extends InvertedIndexMap<ComponentType, BuildRule> {

    private final Map<String, BuildRule> rules = new LinkedHashMap<>();

    public RulesRegistry() {
        super(ComponentType.class, BuildRule::involvedComponents);
    }

    public Set<BuildRule> rules() {
        return new LinkedHashSet<>(rules.values());
    }

    public BuildRule get(String name) {
        if (rules.get(name) == null)
            throw new IllegalArgumentException("Rule with name " + name + " does not exist");
        return rules.get(name);
    }

    @Override
    public void add(BuildRule rule) {
        // Names are the rule identity (lookup by name, violations keyed by name): a duplicate would overwrite the
        // map entry while the old rule stayed in the inverted index as a "ghost" rule, so it is rejected
        if (rules.containsKey(rule.name()))
            throw new IllegalArgumentException("Rule with name " + rule.name() + " already exists");
        rules.put(rule.name(), rule);
        super.add(rule);
    }

    @Override
    public boolean remove(BuildRule rule) {
        boolean res;
        res = rules.remove(rule.name(), rule);
        return super.remove(rule) && res;
    }

    // NOTE: for a guarded rule this is "guard implies check", which is (vacuously) true when the guard fails,
    // so it is NOT suitable as a guard for other rules: use guard(name) for that
    /*
    public BuildSpecification specification(String name) {
        return get(name).specification();
    }
    */

}
