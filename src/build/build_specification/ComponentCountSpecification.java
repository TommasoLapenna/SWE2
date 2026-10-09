package build.build_specification;

import build.PcBuilder;
import model.attribute.ComponentType;
import specification.CompositeSpecification;

import java.util.EnumSet;
import java.util.Set;

public final class ComponentCountSpecification extends BuildSpecification {
    private final ComponentType type;
    private final int min, max;

    public ComponentCountSpecification(ComponentType type, int min, int max) {
        this.type = type;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isSatisfiedBy(PcBuilder build) {
        int n = build.getComponentNumber(type);
        return n >= min && n <= max;
    }

    @Override public Set<ComponentType> dependsOn() { return EnumSet.of(type); }
}

