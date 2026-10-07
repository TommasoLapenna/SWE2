package build;

import build.rule.Severity;
import model.attribute.ComponentType;
import specification.Specification;
import java.util.Set;

public record BuildRule (
                            int id,
                            String name,
                            Specification<PcBuilder> specification,
                            Severity severity,
                            String message,
                            Set<ComponentType> involvedComponents
    ) {

    boolean evaluate(PcBuilder build) {
        return specification().isSatisfiedBy(build);
    }

    public boolean isSatisfiedBy(PcBuilder build) {
        return specification().isSatisfiedBy(build);
    }

}
