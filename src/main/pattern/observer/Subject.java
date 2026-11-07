package main.pattern.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class Subject <E extends Event<?>> {
    public void attach(Observer<E> o) { observers.add(o); }
    public void detach(Observer<E> o) { observers.remove(o); }
    protected void notify(E event) {
        for (Observer<E> o : List.copyOf(observers)) {
            o.update(event);
        }
    }

    protected List<Observer<E>> observers = new ArrayList<>();
}
