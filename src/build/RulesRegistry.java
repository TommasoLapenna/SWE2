package build;

import build.build_specification.BuildSpecification;
import model.attribute.ComponentType;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RulesRegistry extends InvertedIndexEnumMap<ComponentType, BuildRule>{

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
        rules.put(rule.name(), rule);
        super.add(rule);
    }

    @Override
    public boolean remove(BuildRule rule) {
        boolean res;
        res = rules.remove(rule.name(), rule);
        return super.remove(rule) && res;
    }

    public BuildSpecification specification(String name) {
        return get(name).specification();
    }

}
