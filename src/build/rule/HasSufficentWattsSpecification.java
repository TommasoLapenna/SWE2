package build.rule;

import build.PcBuilder;
import model.attribute.ComponentType;
import model.component.Component;
import specification.CompositeSpecification;

public class HasSufficentWattsSpecification extends CompositeSpecification<PcBuilder> {
    @Override
    public boolean isSatisfiedBy(PcBuilder candidate) {
        boolean result = false;
        return result;
    }
}
