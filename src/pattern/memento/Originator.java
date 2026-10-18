package pattern.memento;

public interface Originator<M extends Memento> {
    M createMemento();
    void restoreMemento(M memento);
}
