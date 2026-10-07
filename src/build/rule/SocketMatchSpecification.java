package build.rule;

import build.PcBuilder;
import model.attribute.ComponentType;
import model.component.Cpu;
import model.component.Motherboard;
import specification.CompositeSpecification;

public final class SocketMatchSpecification extends CompositeSpecification<PcBuilder> {
    public SocketMatchSpecification() {}

    @Override
    public boolean isSatisfiedBy(PcBuilder build) {
        return build.getSingleComponent(ComponentType.CPU, Cpu.class).socket().equals(build.getSingleComponent(ComponentType.MOTHERBOARD, Motherboard.class).socket());
    }
}
