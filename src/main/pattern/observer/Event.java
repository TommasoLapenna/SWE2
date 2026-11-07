package main.pattern.observer;

public abstract class Event<T extends Enum<T>> {
    private final T type;
    protected Event(T type) {
        this.type = type;
    }
    public T type() {
        return type;
    }
}
