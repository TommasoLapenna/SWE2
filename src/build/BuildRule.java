package build;

import build.build_specification.BuildSpecification;
import model.attribute.ComponentType;
import specification.Specification;
import java.util.Set;

public record BuildRule (
                            int id,
                            String name,
                            BuildSpecification specification,
                            Severity severity,
                            String message
    ) {

    boolean evaluate(PcBuilder build) {
        return specification().isSatisfiedBy(build);
    }

    public boolean isSatisfiedBy(PcBuilder build) {
        return specification().isSatisfiedBy(build);
    }

    public Set<ComponentType> involvedComponents() {
        return specification.dependsOn();
    }
}
