package build.build_specification;

import build.PcBuilder;
import model.attribute.ComponentType;
import model.component.Cpu;
import model.component.Motherboard;
import specification.CompositeSpecification;

import java.util.EnumSet;
import java.util.Set;

public final class SocketMatchSpecification extends BuildSpecification {
    public SocketMatchSpecification() {}

    @Override
    public boolean isSatisfiedBy(PcBuilder build) {
        return build.getSingleComponent(ComponentType.CPU, Cpu.class).socket().equals(build.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).socket());
    }

    @Override public Set<ComponentType> dependsOn() {
        return EnumSet.of(ComponentType.CPU, ComponentType.MOTHERBOARD);
    }
}
