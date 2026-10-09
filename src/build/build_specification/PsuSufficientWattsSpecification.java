package build.build_specification;

import build.PcBuilder;
import model.attribute.ComponentType;
import model.component.PowerSupply;
import specification.CompositeSpecification;

import java.util.EnumSet;
import java.util.Set;

public final class PsuSufficientWattsSpecification extends BuildSpecification {
    public PsuSufficientWattsSpecification() {}

    @Override
    public boolean isSatisfiedBy(PcBuilder build) {
        return build.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).wattage() >= build.totalWattage();
    }

    @Override public Set<ComponentType> dependsOn() {
        return EnumSet.allOf(ComponentType.class);
    }
}
