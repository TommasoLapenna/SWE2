package main.domain.rule.specification;

import main.domain.model.PcBuilder;
import main.domain.model.attribute.ComponentType;

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

