package build.build_specification;

import build.PcBuilder;
import model.attribute.ComponentType;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

public class PredicateSpecification extends BuildSpecification {

    private final Predicate<PcBuilder> rule;
    private final Set<ComponentType> deps;

    public PredicateSpecification(Predicate<PcBuilder> rule, Set<ComponentType> deps) {
        this.rule = rule;
        this.deps = deps.isEmpty() ? Set.of() : EnumSet.copyOf(deps);
    }

    @Override
    public Set<ComponentType> dependsOn() {
        return deps;
    }

    @Override
    public boolean isSatisfiedBy(PcBuilder candidate) {
        return rule.test(candidate);
    }
}
