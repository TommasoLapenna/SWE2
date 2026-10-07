package build;

import build.rule.ComponentCountSpecification;
import build.rule.PsuSufficientWattsSpecification;
import build.rule.SocketMatchSpecification;
import factory.Factory;
import model.attribute.ComponentType;

import specification.Specification;

public final class Specs<PcBuild> implements Factory {
    private Specs() {}

    //CARDINALITY SPECIFICATIONS FACTORY:
    static Specification<PcBuilder> atMost(ComponentType t, int n)  { return new ComponentCountSpecification(t, 0, n); }
    static Specification<PcBuilder> atLeast(ComponentType t, int n) { return new ComponentCountSpecification(t, n, Integer.MAX_VALUE); }
    static Specification<PcBuilder> exactly(ComponentType t, int n) { return new ComponentCountSpecification(t, n, n); }

    static Specification<PcBuilder> socketMatchSpecification = new SocketMatchSpecification();
    static Specification<PcBuilder> psuSufficientWattsSpecification = new PsuSufficientWattsSpecification();
}
