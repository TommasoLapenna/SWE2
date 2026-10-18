package build.specification;

import pattern.factory.Factory;
import model.attribute.ComponentType;
import model.component.*;

import java.util.EnumSet;
import java.util.Set;

public final class Specs implements Factory {
    private Specs() {}

    public static BuildSpecification atMost(ComponentType t, int n)  { return new ComponentCountSpecification(t, 0, n); }
    public static BuildSpecification atLeast(ComponentType t, int n) { return new ComponentCountSpecification(t, n, Integer.MAX_VALUE); }
    public static BuildSpecification exactly(ComponentType t, int n) { return new ComponentCountSpecification(t, n, n); }

    public static BuildSpecification mutipleExcatclyOne(ComponentType... t) {
        BuildSpecification res = new ComponentCountSpecification(t[0], 1, 1);
        for (int i = 1; i < t.length; i++){
            res = res.and(new ComponentCountSpecification(t[i], 1, 1));
        }
        return res;
    }

    public static BuildSpecification socketMatchSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CPU, Cpu.class).socket().equals(b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).socket()),
            Set.of(ComponentType.CPU, ComponentType.MOTHERBOARD)
    );
    public static BuildSpecification psuSufficientWattsSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.POWER_SUPPLY, PowerSupply.class).wattage() >= b.totalWattage(),
            EnumSet.allOf(ComponentType.class)
    );

    public static BuildSpecification formFactorMatchSpecification = new PredicateSpecification(
            b -> b.getSingleComponent(ComponentType.CASE, Case.class).supportedForms().contains(b.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).formFactor()),
            Set.of(ComponentType.CASE, ComponentType.MOTHERBOARD)
    );

}
