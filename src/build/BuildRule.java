package build;

import build.build_specification.BuildSpecification;
import model.attribute.ComponentType;
import specification.Specification;
import java.util.Set;
import java.util.regex.Matcher;

public record BuildRule (
                            int id,
                            String name,
                            BuildSpecification specification,
                            Severity severity,
                            String message,
                            // Specification used when this rule is the guard of another rule: "guard AND check",
                            // true only when the rule applies AND is satisfied (see RulesFactory).
                            // It differs from specification ("guard IMPLIES check"), which is vacuously true when
                            // the guard fails (e.g. SINGLE-CPU is satisfied with 0 CPUs)
                            BuildSpecification guardSpecification
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
            // The violation only records the components the rule depends on, any other placeholder would be "none"
            if (!specification.dependsOn().contains(type))
                throw new IllegalArgumentException("Rule " + name + ": placeholder {" + type + "} is not a component the rule depends on");
        }
    }

    // Original constructor, for rules without a guard: as a guard, the rule is just its own specification
    public BuildRule(int id, String name, BuildSpecification specification, Severity severity, String message) {
        this(id, name, specification, severity, message, specification);
    }

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
