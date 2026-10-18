import application.PcBuildSession;
import build.CompatibilityChecker;
import build.PcBuilder;
import build.rule.Violation;
import model.attribute.FormFactor;
import model.attribute.MemoryGen;
import model.attribute.PsuFormFactor;
import model.attribute.Socket;
import model.component.Case;
import model.component.Component;
import model.component.Cpu;
import model.component.Motherboard;

import java.util.Set;

public class Main {
    void main() {

        PcBuildSession session = new PcBuildSession("Test1");

        Component cpu1 = new Cpu(1234, "Intel", "i7", 100.32, 40, Socket.LGA1200, 4, 8, 89.0, Set.of(MemoryGen.DDR4), true, true);
        Component cpu2 = new Cpu(1235, "AMD", "Ryzen 5", 150.32, 30, Socket.AM5, 4, 8, 79.0, Set.of(MemoryGen.DDR4), true, true);
        Component mobo = new Motherboard(1236, "ASUS", "ROG", 200.32, Socket.AM5, "A620", FormFactor.ATX, MemoryGen.DDR4, 8, 1000, 1000, 2, 2, 2);
        Component case1 = new Case(1234, "Gigabyte", "Peepeepoopoo", 450.50, 100, Set.of(FormFactor.E_ATX), Set.of(PsuFormFactor.SFX), 100, 100, 4, 4, Set.of(1, 2));


        session.add(cpu1);
        session.print();

        session.add(cpu2);
        session.print();

        session.undo();
        session.print();

        session.add(mobo);
        session.print();

        session.remove(cpu2);
        session.print();

        session.add(case1);
        session.print();
    }

}
