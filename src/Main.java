import build.CompatibilityChecker;
import build.PcBuilder;
import build.Violation;
import model.attribute.FormFactor;
import model.attribute.MemoryGen;
import model.attribute.Socket;
import model.component.Component;
import model.component.Cpu;
import model.component.Motherboard;

import java.util.Set;

public class Main {
    void main() {

        PcBuilder pc = new PcBuilder("Faaah");
        CompatibilityChecker comp = new CompatibilityChecker(pc);

        Component cpu1 = new Cpu(1234, "Intel", "i7", 100.32, 40, Socket.LGA1200, 4, 8, 89.0, Set.of(MemoryGen.DDR4), true, true);
        Component cpu2 = new Cpu(1235, "AMD", "Ryzen 5", 150.32, 30, Socket.AM5, 4, 8, 79.0, Set.of(MemoryGen.DDR4), true, true);

        Component mobo = new Motherboard(1236, "ASUS", "ROG", 200.32, Socket.AM5, "A620", FormFactor.ATX, MemoryGen.DDR4, 8, 1000, 1000, 2, 2, 2);

        pc.print();

        pc.addComponent(cpu1);
        printRoutine(pc, comp);

        pc.addComponent(cpu2);
        printRoutine(pc, comp);

        pc.addComponent(mobo);
        printRoutine(pc, comp);

        pc.removeComponent(cpu2);
        printRoutine(pc, comp);
    }

    private void printRoutine(PcBuilder pc, CompatibilityChecker comp) {
        pc.print();
        for (Violation violation : comp.violations()) {
            System.out.println(" - " + violation.getMessage());
        }
        System.out.println();
    }
}
