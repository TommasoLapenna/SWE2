package build.build_specification;

import build.PcBuilder;
import model.attribute.ComponentType;
import specification.CompositeSpecification;
import specification.Specification;

import java.util.EnumSet;
import java.util.Set;

public abstract class BuildSpecification extends CompositeSpecification<PcBuilder> {

    public abstract Set<ComponentType> dependsOn();

    @Override public BuildSpecification and(Specification<PcBuilder> o)     { return combine(super.and(o), o); }
    @Override public BuildSpecification or(Specification<PcBuilder> o)      { return combine(super.or(o), o); }
    @Override public BuildSpecification andNot(Specification<PcBuilder> o)  { return combine(super.andNot(o), o); }
    @Override public BuildSpecification orNot(Specification<PcBuilder> o)    { return combine(super.orNot(o), o); }
    @Override public BuildSpecification implies(Specification<PcBuilder> o) { return combine(super.implies(o), o); }
    @Override public BuildSpecification not()                               { return new Combined(super.not(), dependsOn()); }

    private BuildSpecification combine(Specification<PcBuilder> composed, Specification<PcBuilder> other) {
        if (!(other instanceof BuildSpecification b))
            throw new IllegalArgumentException(
                    "Cannot combine with a spec that does not declare its dependencies: " + other);
        Set<ComponentType> union = EnumSet.copyOf(dependsOn());
        union.addAll(b.dependsOn());
        return new Combined(composed, union);
    }

    // Result of any combination: delegates evaluation, carries the merged dependencies
    private static final class Combined extends BuildSpecification {
        private final Specification<PcBuilder> delegate;
        private final Set<ComponentType> deps;

        Combined(Specification<PcBuilder> delegate, Set<ComponentType> deps) {
            this.delegate = delegate;
            this.deps = EnumSet.copyOf(deps);
        }

        @Override public boolean isSatisfiedBy(PcBuilder b) { return delegate.isSatisfiedBy(b); }
        @Override public Set<ComponentType> dependsOn()     { return deps; }
    }
}

