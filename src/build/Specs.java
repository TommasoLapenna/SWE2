package build;

import build.build_specification.ComponentCountSpecification;
import build.build_specification.PsuSufficientWattsSpecification;
import build.build_specification.SocketMatchSpecification;
import factory.Factory;
import model.attribute.ComponentType;
import build.build_specification.BuildSpecification;

public final class Specs implements Factory {
    private Specs() {}

    static BuildSpecification atMost(ComponentType t, int n)  { return new ComponentCountSpecification(t, 0, n); }
    static BuildSpecification atLeast(ComponentType t, int n) { return new ComponentCountSpecification(t, n, Integer.MAX_VALUE); }
    static BuildSpecification exactly(ComponentType t, int n) { return new ComponentCountSpecification(t, n, n); }

    static BuildSpecification socketMatchSpecification = new SocketMatchSpecification();
    static BuildSpecification psuSufficientWattsSpecification = new PsuSufficientWattsSpecification();
}
