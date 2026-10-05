package build.rule;

import build.PcBuilder;
import specification.CompositeSpecification;

public class SocketMatchSpecification extends CompositeSpecification<PcBuilder> {
    @Override
    public boolean isSatisfiedBy(PcBuilder candidate) {
        return false;
    }
}
