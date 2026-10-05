package build.rule;

import build.PcBuilder;
import model.attribute.ComponentType;
import model.component.Component;
import specification.CompositeSpecification;

import java.util.List;

public class HasSingleMotherBoardSpecification extends CompositeSpecification<PcBuilder> {
    @Override
    public boolean isSatisfiedBy(PcBuilder candidate) {
        List<Component> mobos = candidate.getComponents(ComponentType.MOTHERBOARD);
        return mobos.size() == 1;
    }
}
