package build;

import model.attribute.ComponentType;
import model.component.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
