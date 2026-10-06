package build;

import model.attribute.ComponentType;
import model.component.*;
import specification.CompositeSpecification;
import specification.Specification;

public final class Specs<PcBuild> {
    private Specs() {}

    static final class ComponentCountSpecification extends CompositeSpecification<PcBuilder> {
        private final ComponentType type;
        private final int min, max;

        private ComponentCountSpecification(ComponentType type, int min, int max) {
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

    //CARDINALITY SPECIFICATIONS FACTORY:
    static Specification<PcBuilder> atMost(ComponentType t, int n)  { return new ComponentCountSpecification(t, 0, n); }
    static Specification<PcBuilder> atLeast(ComponentType t, int n) { return new ComponentCountSpecification(t, n, Integer.MAX_VALUE); }
    static Specification<PcBuilder> exactly(ComponentType t, int n) { return new ComponentCountSpecification(t, n, n); }


    public static final class PsuSufficientWatts extends CompositeSpecification<PcBuilder> {
        private PsuSufficientWatts() {}

        @Override
        public boolean isSatisfiedBy(PcBuilder build) {
            return build.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).wattage() >= build.totalWattage();
        }
    }
}
