package build;

import model.attribute.ComponentType;
import model.component.Component;
import observer.Observer;
import observer.Subject;

import java.util.*;

public class PcBuilder implements Subject <BuildEvent> {
    private final Map<ComponentType, List<Component>> components = new EnumMap<>(ComponentType.class);
    private final List<Observer<BuildEvent>> observers = new ArrayList<Observer<BuildEvent>>();
    private String name;

    public PcBuilder(String name) {
        this.name = name;
        // PRE-Initialization for the all component lists, memory usage negligible (only a few types of components)
        // compared to the benefits of avoiding null checks and simplifying code
        for(ComponentType type : ComponentType.values()) {
            components.put(type, new ArrayList<>());
        }
    }

    public void addComponent(Component c) {
        components.get(c.type()).add(c);
        notify(new BuildEvent(BuildEventType.ADDED, c.type()));
    }

    public boolean removeComponent(Component c) {
        if(components.get(c.type()).remove(c)) {
            notify(new BuildEvent(BuildEventType.REMOVED, c.type()));
            return true;
        }
        return false;
    }

    public int totalWattage() {
        int totalWattage = 0;
        for(ComponentType type : ComponentType.values()) {
            for(Component c : components.get(type)) {
                totalWattage += c.powerDrawWatts();
            }
        }
        return totalWattage;
    }

    public boolean hasComponent(ComponentType componentType) {
        return !components.get(componentType).isEmpty();
    }

    public List<Component> getComponents(ComponentType componentType) {
        return components.get(componentType);
    }

    public int getComponentNumber(ComponentType componentType) {
        return components.get(componentType).size();
    }

    public <T extends Component> T getSingleComponent(ComponentType type, Class<T> componentClass)  {
        List<Component> componentList = components.get(type);

        if (componentList == null || componentList.isEmpty()) {
            throw new IllegalStateException("No component found for type: " + type);
        }
        if (componentList.size() > 1) {
            throw new IllegalStateException("Expected exactly one component for " + type);
        }

        Component c = componentList.getFirst();
        if (!componentClass.isInstance(c)) {
            throw new ClassCastException(
                    "Component for " + type + " is " + c.getClass().getSimpleName()
                            + ", expected " + componentClass.getSimpleName());
        }
        return componentClass.cast(c);
    }

    @Override
    public void attach(Observer<BuildEvent> o) {
        observers.add(o);
    }

    @Override
    public void detach(Observer<BuildEvent> o) {
        observers.remove(o);
    }

    @Override
    public void notify(BuildEvent event) {
        for(Observer<BuildEvent> o : observers) {
            o.update(event);
        }
    }





    //PRINT METHOD WRITTEN ENTIRELY BY CLAUDE
    public void print() {
        final String EMPTY_SLOT = "-- empty --";

        // 1. Build all the text lines first, so the box width can fit the longest one
        String title = "PC BUILD: " + name;
        String footer = "Total draw: " + totalWattage() + " W";

        List<String> headers = new ArrayList<>();
        List<List<String>> bodies = new ArrayList<>();

        for (ComponentType type : ComponentType.values()) {
            List<Component> list = components.get(type);
            headers.add(type + " [" + list.size() + "]");

            List<String> body = new ArrayList<>();
            if (list.isEmpty()) {
                body.add("  " + EMPTY_SLOT);
            } else {
                for (Component c : list) {
                    body.add("  * " + describe(c));
                }
            }
            bodies.add(body);
        }

        // 2. Compute the inner width
        int width = Math.max(title.length(), footer.length());
        for (int i = 0; i < headers.size(); i++) {
            width = Math.max(width, headers.get(i).length());
            for (String line : bodies.get(i)) {
                width = Math.max(width, line.length());
            }
        }
        width += 2; // padding

        // 3. Draw
        String bar = "═".repeat(width);
        String thin = "─".repeat(width);

        System.out.println("╔" + bar + "╗");
        System.out.println("║" + center(title, width) + "║");
        System.out.println("╠" + bar + "╣");

        for (int i = 0; i < headers.size(); i++) {
            System.out.println("║ " + pad(headers.get(i), width - 1) + "║");
            for (String line : bodies.get(i)) {
                System.out.println("║ " + pad(line, width - 1) + "║");
            }
            if (i < headers.size() - 1) {
                System.out.println("╟" + thin + "╢");
            }
        }

        System.out.println("╠" + bar + "╣");
        System.out.println("║ " + pad(footer, width - 1) + "║");
        System.out.println("╚" + bar + "╝");
    }

    private String describe(Component c) {
        // Replace with your own accessors, e.g. c.brand() + " " + c.model() + " (" + c.powerDrawWatts() + " W)"
        return c.name() + " (" + c.powerDrawWatts() + " W)";
    }

    private static String pad(String s, int width) {
        return String.format("%-" + width + "s", s);
    }

    private static String center(String s, int width) {
        int left = (width - s.length()) / 2;
        return " ".repeat(left) + s + " ".repeat(width - s.length() - left);
    }

}
