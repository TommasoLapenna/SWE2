package build.rule;

import build.PcBuilder;
import model.attribute.ComponentType;
import specification.CompositeSpecification;

public final class ComponentCountSpecification extends CompositeSpecification<PcBuilder> {
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
}

