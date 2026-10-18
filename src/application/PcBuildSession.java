package application;

import build.BuildView;
import build.CompatibilityChecker;
import build.PcBuilder;
import build.rule.Violation;
import build.snapshot.SnapshotHistory;
import model.component.Component;
import pattern.facade.Facade;

import java.util.List;

public class PcBuildSession implements Facade {

    private final PcBuilder build;
    private final CompatibilityChecker checker;
    private final SnapshotHistory history;

    public PcBuildSession(String name) {
        build = new PcBuilder(name);
        checker = new CompatibilityChecker(build, DefaultRules.create());
        history = new SnapshotHistory(build::createMemento);
    }

    public void add(Component c) {
        history.save();
        build.addComponent(c);
    }

    public boolean remove(Component c) {
        if (!build.getComponents(c.type()).contains(c))
            return false;
        history.save();
        return build.removeComponent(c);
    }

    public boolean undo() {
        if (!history.canUndo())
            return false;
        build.restoreMemento(history.undo());
        return true;
    }

    public boolean redo() {
        if (!history.canRedo())
            return false;
        build.restoreMemento(history.redo());
        return true;
    }

    public void print() {
        System.out.println("===========================================================================================");
        System.out.println(BuildPrinter.render(build.view()));
        for (Violation violation : checker.violations()) {
            System.out.println(" - " + violation.getMessage());
        }
        System.out.println("===========================================================================================");
    }

    //READ ONLY VERSION OF PCBUILDER
    public BuildView getBuild() {
        return build.view();
    }

    public List<Violation> getViolations() {
        return checker.violations();
    }

}
