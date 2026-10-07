package build.rule;

import build.PcBuilder;
import model.attribute.ComponentType;
import model.component.PowerSupply;
import specification.CompositeSpecification;

public final class PsuSufficientWattsSpecification extends CompositeSpecification<PcBuilder> {
    public PsuSufficientWattsSpecification() {}

    @Override
    public boolean isSatisfiedBy(PcBuilder build) {
        return build.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).wattage() >= build.totalWattage();
    }
}
