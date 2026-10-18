package build.specification;

import build.PcBuilder;
import model.attribute.ComponentType;
import pattern.specification.CompositeSpecification;
import pattern.specification.Specification;

import java.util.EnumSet;
import java.util.Set;

public abstract class BuildSpecification extends CompositeSpecification<PcBuilder> {

    public abstract Set<ComponentType> dependsOn();

    @Override public BuildSpecification and(Specification<PcBuilder> o)     { return combine(super.and(o), o); }
    @Override public BuildSpecification or(Specification<PcBuilder> o)      { return combine(super.or(o), o); }
    @Override public BuildSpecification andNot(Specification<PcBuilder> o)  { return combine(super.andNot(o), o); }
    @Override public BuildSpecification orNot(Specification<PcBuilder> o)   { return combine(super.orNot(o), o); }
    @Override public BuildSpecification implies(Specification<PcBuilder> o) { return combine(super.implies(o), o); }
    @Override public BuildSpecification not() {
        return new PredicateSpecification(super.not()::isSatisfiedBy, dependsOn());
    }

    private BuildSpecification combine(Specification<PcBuilder> composed, Specification<PcBuilder> other) {
        if (!(other instanceof BuildSpecification b))
            throw new IllegalArgumentException(
                    "Cannot combine with a spec that does not declare its dependencies: " + other);
        Set<ComponentType> union = EnumSet.noneOf(ComponentType.class);
        union.addAll(dependsOn());
        union.addAll(b.dependsOn());
        return new PredicateSpecification(composed::isSatisfiedBy, union);
    }
}

