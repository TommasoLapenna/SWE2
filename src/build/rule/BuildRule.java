package build.rule;

import build.PcBuilder;
import build.specification.BuildSpecification;
import model.attribute.ComponentType;

import java.util.Set;
import java.util.regex.Matcher;

public record BuildRule (
                            int id,
                            String name,
                            BuildSpecification specification,
                            Severity severity,
                            String message
    ) {

    // Compact constructor: validates the message template up front, instead of failing (or printing "none")
    // only when a violation is displayed

    public BuildRule {
        Matcher m = Violation.PLACEHOLDER.matcher(message);
        while (m.find()) {
            ComponentType type;
            try {
                type = ComponentType.valueOf(m.group(1));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Rule " + name + ": unknown placeholder {" + m.group(1) + "}", e);
            }
            if (!specification.dependsOn().contains(type))
                throw new IllegalArgumentException("Rule " + name + ": placeholder {" + type + "} is not a component the rule depends on");
        }
    }

    public boolean isSatisfiedBy(PcBuilder build) {
        return specification().isSatisfiedBy(build);
    }

    public Set<ComponentType> involvedComponents() {
        return specification.dependsOn();
    }
}
