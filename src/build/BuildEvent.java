package build;

import observer.Event;

public class BuildEvent extends Event<BuildEventType> {
    public BuildEvent(BuildEventType type) {
        super(type);
    }
}
