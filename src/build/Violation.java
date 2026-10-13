package build;

import model.attribute.ComponentType;
import model.component.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public record Violation(
        String rule,
        Severity severity,
        String message,                                   // template with {TYPE} placeholders
        Map<ComponentType, List<Component>> involvedComponents
) {
    // package-private: also used by BuildRule to validate message templates when the rule is created
    static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Z_]+)}");

    public Violation {
        involvedComponents = Map.copyOf(involvedComponents);
    }

    public String getMessage() {
        return PLACEHOLDER.matcher(message).replaceAll(m -> {
            ComponentType type = ComponentType.valueOf(m.group(1));
            List<Component> list = involvedComponents.getOrDefault(type, List.of());
            String text = list.isEmpty()
                    ? "none"
                    : list.stream().map(Component::name).collect(Collectors.joining(", "));
            return Matcher.quoteReplacement(text);
        });
    }
}
