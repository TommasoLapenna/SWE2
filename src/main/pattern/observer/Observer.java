package main.pattern.observer;

public interface Observer <E extends Event<?>> {
    void update(E event);
}

