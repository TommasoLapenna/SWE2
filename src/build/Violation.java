package build;

import model.attribute.ComponentType;
import model.component.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Violation {
    private String rule;
    private String message;
    private Map<ComponentType, List<Component>> involvedComponents;

    public Violation(String rule, String message, Map<ComponentType, List<Component>> involvedComponents) {
        this.rule = rule;
        this.message = message;
        this.involvedComponents = involvedComponents;
    }

    public void add(ComponentType type, List<Component> components) {
        this.involvedComponents.put(type, components);
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Z_]+)}");

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
