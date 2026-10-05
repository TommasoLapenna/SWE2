package observer;

public interface Subject <E> {
    void attach(Observer<E> o);
    void detach(Observer<E> o);
    void notify(E event);
}
