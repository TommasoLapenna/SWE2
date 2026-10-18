package build.snapshot;

import pattern.memento.Caretaker;
import pattern.memento.Memento;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public class SnapshotHistory implements Caretaker  {

    // Functional interface, it's a function with no arguments and has .get() to call it
    Supplier<Memento> originatorMementoFunction;

    private Deque<Memento> undoStack = new ArrayDeque<>();
    private Deque<Memento> redoStack = new ArrayDeque<>();

    public SnapshotHistory(Supplier<Memento> supplier) {
        this.originatorMementoFunction = supplier;
    }

    public void save() {
        if(!redoStack.isEmpty())
            redoStack.clear();
        undoStack.push(originatorMementoFunction.get());
    }

    public boolean canUndo() {return !undoStack.isEmpty(); }

    public boolean canRedo() { return !redoStack.isEmpty(); }


    // They throw NoSuchElementException if they sre called with the stacks empty
    // The facade checks it before calling them
    public Memento undo() {
        redoStack.push(originatorMementoFunction.get());
        return undoStack.pop();
    }

    public Memento redo() {
        undoStack.push(originatorMementoFunction.get());
        return redoStack.pop();
    }

}
