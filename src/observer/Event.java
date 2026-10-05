package observer;

public abstract class Event<T> {
    private final T type;
    protected Event(T type) {
        this.type = type;
    }
    public T type() {
        return type;
    }
}
