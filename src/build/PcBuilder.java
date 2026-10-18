package build;

import model.attribute.ComponentType;
import model.component.Component;
import pattern.memento.Memento;
import pattern.memento.Originator;
import pattern.observer.Observer;
import pattern.observer.Subject;

import java.util.*;

public class PcBuilder implements Subject <BuildEvent>, Originator<Memento> {
    private Map<ComponentType, List<Component>> components = new EnumMap<>(ComponentType.class);
    private final List<Observer<BuildEvent>> observers = new ArrayList<Observer<BuildEvent>>();
    private String name;

    private final BuildView view = new ReadOnlyView();

    public PcBuilder(String name) {
        this.name = name;
        // PRE-Initialization for the all component lists, memory usage negligible (only a few types of components)
        // compared to the benefits of avoiding null checks and simplifying code
        for (ComponentType type : ComponentType.values()) {
            components.put(type, new ArrayList<>());
        }
    }

    private final class ReadOnlyView implements BuildView {
        @Override public String name()                        { return PcBuilder.this.name(); }
        @Override public int totalWattage()                   { return PcBuilder.this.totalWattage(); }
        @Override public boolean hasComponent(ComponentType t){ return PcBuilder.this.hasComponent(t); }
        @Override public List<Component> getComponents(ComponentType t) { return PcBuilder.this.getComponents(t); }
        @Override public int getComponentNumber(ComponentType t) { return PcBuilder.this.getComponentNumber(t); }
        @Override public Map<ComponentType, List<Component>> components() { return PcBuilder.this.immutableMapCopy(); }
    }

    // Return a read-only view of the pc build
    public BuildView view() {
        return view;
    }

    public void addComponent(Component c) {
        Objects.requireNonNull(c, "Component must not be null");
        components.get(c.type()).add(c);
        notify(new BuildEvent(BuildEventType.ADDED, c));
    }

    public boolean removeComponent(Component c) {
        Objects.requireNonNull(c, "Component must not be null");
        if (components.get(c.type()).remove(c)) {
            notify(new BuildEvent(BuildEventType.REMOVED, c));
            return true;
        }
        return false;
    }

    public String name() {
        return name;
    }

    public int totalWattage() {
        int totalWattage = 0;
        for (ComponentType type : ComponentType.values()) {
            for (Component c : components.get(type)) {
                totalWattage += c.powerDrawWatts();
            }
        }
        return totalWattage;
    }

    public boolean hasComponent(ComponentType componentType) {
        return !components.get(componentType).isEmpty();
    }

    public List<Component> getComponents(ComponentType componentType) {
        return Collections.unmodifiableList(components.get(componentType));
    }

    //Ai version
    public <T extends Component> List<T> getComponents(ComponentType componentType, Class<T> componentClass) {
        return components.get(componentType).stream().map(componentClass::cast).toList();
    }

    public int getComponentNumber(ComponentType componentType) {
        return components.get(componentType).size();
    }

    public <T extends Component> T getSingleComponent(ComponentType type, Class<T> componentClass) {
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
        for (Observer<BuildEvent> o : List.copyOf(observers)) {
            o.update(event);
        }
    }

    private Map<ComponentType, List<Component>> immutableMapCopy() {
        Map<ComponentType, List<Component>> copy = new EnumMap<>(ComponentType.class);
        components.forEach((type, list) -> copy.put(type, List.copyOf(list)));
        return Collections.unmodifiableMap(copy);
    }

    private Map<ComponentType, List<Component>> mutableMapCopy(Map<ComponentType, List<Component>> map) {
        Map<ComponentType, List<Component>> copy = new EnumMap<>(ComponentType.class);
        map.forEach((type, list) -> copy.put(type, new ArrayList<>(list)));
        return copy;
    }

    @Override
    public Memento createMemento() {
        return new BuildSnapshot(this);
    }

    @Override
    public void restoreMemento(Memento memento) {
        if (memento instanceof BuildSnapshot m){
            components = mutableMapCopy(m.getState());
        }else
            throw new IllegalArgumentException("Invalid memento type");
        notify(new BuildEvent(BuildEventType.RESTORED, null));
    }

    // The Memento marker interface is VERY important, it makes the snapshot ottaniable outside, but it will not be accessible
    private class BuildSnapshot implements Memento {

        //private Originator<Memento> originator; This is not needed it's obtained with PcbìBuilder.this
        private final Map<ComponentType, List<Component>> components;

        public BuildSnapshot(PcBuilder build) {
            this.components = immutableMapCopy();
        }

        public Map<ComponentType, List<Component>> getState() {
            return components;
        }
    }

}