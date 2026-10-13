package build.build_specification;

import build.PcBuilder;
import model.attribute.ComponentType;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

// Generic BuildSpecification defined by a lambda, so simple compatibility checks don't each need their own class.
// The dependencies must be declared explicitly: they drive the inverted index, so a missing type means the rule
// is NOT re-evaluated when a component of that type is added/removed
public final class PredicateSpecification extends BuildSpecification {
    private final Predicate<PcBuilder> predicate;
    private final Set<ComponentType> dependsOn;

    public PredicateSpecification(Predicate<PcBuilder> predicate, Set<ComponentType> dependsOn) {
        this.predicate = predicate;
        // noneOf + addAll instead of copyOf, which throws on an empty collection
        this.dependsOn = EnumSet.noneOf(ComponentType.class);
        this.dependsOn.addAll(dependsOn);
    }

    @Override
    public boolean isSatisfiedBy(PcBuilder build) {
        return predicate.test(build);
    }

    @Override public Set<ComponentType> dependsOn() { return dependsOn; }
}
