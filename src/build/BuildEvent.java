package build;

import model.attribute.ComponentType;
import observer.Event;

public class BuildEvent extends Event<BuildEventType> {

    private final ComponentType component;

    public BuildEvent(BuildEventType type, ComponentType component) {
        super(type);
        this.component = component;
    }

}
