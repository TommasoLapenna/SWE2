package build;

import build.build_specification.*;
import factory.Factory;
import model.attribute.ComponentType;
import model.component.*;

import java.util.EnumSet;
import java.util.Set;

public final class Specs implements Factory {
    private Specs() {}

    static BuildSpecification atMost(ComponentType t, int n)  { return new ComponentCountSpecification(t, 0, n); }
    static BuildSpecification atLeast(ComponentType t, int n) { return new ComponentCountSpecification(t, n, Integer.MAX_VALUE); }
    static BuildSpecification exactly(ComponentType t, int n) { return new ComponentCountSpecification(t, n, n); }

    static BuildSpecification socketMatchSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CPU, Cpu.class).socket().equals(b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).socket()),
            Set.of(ComponentType.CPU, ComponentType.MOTHERBOARD)
    );
    static BuildSpecification psuSufficientWattsSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).wattage() >= b.totalWattage(),
            EnumSet.allOf(ComponentType.class)
    );

    static BuildSpecification formFactorMatchSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CASE, Case.class).supportedForms().contains(b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).formFactor()),
            Set.of(ComponentType.CPU, ComponentType.MOTHERBOARD)
    );

}
