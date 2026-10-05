package build.rule;

import build.PcBuilder;
import model.attribute.ComponentType;
import specification.CompositeSpecification;

public class HasComponentSpecification extends CompositeSpecification<PcBuilder> {
    private final ComponentType componentType;

    public HasComponentSpecification(ComponentType componentType) {
        this.componentType = componentType;
    }

    @Override
    public boolean isSatisfiedBy(PcBuilder candidate) {
        return candidate.hasComponent(componentType);
    }
}
