package build;

import model.attribute.ComponentType;
import model.component.Component;
import observer.Event;

public class BuildEvent extends Event<BuildEventType> {

    private final Component component;

    public BuildEvent(BuildEventType type, Component component) {
        super(type);
        this.component = component;
    }

    public Component component() { return component; }
    public ComponentType componentType() { return component.type(); }

}
