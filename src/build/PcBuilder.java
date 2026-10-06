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

    public PcBuilder name(String name) {
        this.name = name;
        // PRE-Initialization for the all component lists, memory usage negligible (only a few types of components)
        // compared to the benefits of avoiding null checks and simplifying code
        for(ComponentType type : ComponentType.values()) {
            components.put(type, new ArrayList<>());
        }
        return this;
    }

    public void addComponent(Component c) {
        components.get(c.type()).add(c);
        notify(new BuildEvent(BuildEventType.ADDED));
    }

    public boolean removeComponent(Component c) {
        if(components.get(c.type()).remove(c)) {
            notify(new BuildEvent(BuildEventType.REMOVED));
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

}
