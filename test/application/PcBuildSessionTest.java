package application;

import main.application.PcBuildSession;
import main.domain.model.attribute.ComponentType;
import main.domain.model.attribute.EfficencyRating;
import main.domain.model.attribute.FormFactor;
import main.domain.model.attribute.MemoryGen;
import main.domain.model.attribute.PsuFormFactor;
import main.domain.model.attribute.Socket;
import main.domain.model.attribute.StorageTech;
import main.domain.model.attribute.StorageType;
import main.domain.model.component.Case;
import main.domain.model.component.Cpu;
import main.domain.model.component.Memory;
import main.domain.model.component.Motherboard;
import main.domain.model.component.PowerSupply;
import main.domain.model.component.Storage;
import main.domain.rule.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La sessione (Facade) usata dalla GUI: undo/redo con il Memento e verifica di compatibilita' integrata. */
class PcBuildSessionTest {

    PcBuildSession session;

    Cpu cpu;
    Motherboard am5Board;
    Motherboard lga1700Board;

    @BeforeEach
    void setUp() {
        session = new PcBuildSession("test");

        cpu = new Cpu(1, "AMD", "Ryzen 7 7700X", 299.0, 105, Socket.AM5, 8, 16, 4.5,
                Set.of(MemoryGen.DDR5), true, false);
        am5Board = new Motherboard(10, "ASUS", "TUF GAMING B850-PLUS", 239.0, Socket.AM5, "B850",
                FormFactor.ATX, MemoryGen.DDR5, 4, 192, 8000, 2, 3, 4);
        lga1700Board = new Motherboard(11, "MSI", "PRO Z790-P", 229.0, Socket.LGA1700, "Z790",
                FormFactor.ATX, MemoryGen.DDR5, 4, 192, 7200, 2, 4, 6);
    }

    private boolean hasViolation(String ruleName) {
        return session.getViolations().stream().anyMatch(v -> v.rule().equals(ruleName));
    }

    @Test
    void undoRemovesTheLastAddAndRedoBringsItBack() {
        session.add(cpu);

        assertTrue(session.undo());
        assertFalse(session.getBuild().hasComponent(ComponentType.CPU));

        assertTrue(session.redo());
        assertTrue(session.getBuild().hasComponent(ComponentType.CPU));
    }

    @Test
    void undoAndRedoWithoutHistoryReturnFalse() {
        assertFalse(session.undo());
        assertFalse(session.redo());
    }

    @Test
    void removingAComponentThatIsNotInTheBuildReturnsFalse() {
        assertFalse(session.remove(cpu));
        assertFalse(session.undo());   // nessuno snapshot salvato
    }

    @Test
    void violationsAreRecomputedAfterUndo() {
        session.add(cpu);
        session.add(lga1700Board);
        assertTrue(hasViolation("SOCKET-MISMATCH"));

        session.undo();

        assertFalse(hasViolation("SOCKET-MISMATCH"));
    }

    @Test
    void aCompleteAndCompatibleBuildHasNoErrors() {
        session.add(cpu);
        session.add(am5Board);
        session.add(new Memory(30, "Corsair", "Vengeance DDR5 2x16GB", 109.0, MemoryGen.DDR5, 2, 16, 6000));
        session.add(new Storage(40, "Samsung", "990 Pro 1TB", 99.0, StorageType.SSD, StorageTech.M2_NVME, 1000));
        session.add(new Case(50, "Fractal Design", "Meshify 2 Compact", 129.0, 6,
                Set.of(FormFactor.MICRO_ATX, FormFactor.ATX), Set.of(PsuFormFactor.ATX), 330, 169, 7, 4, Set.of(240, 280, 360)));
        session.add(new PowerSupply(60, "Corsair", "RM650", 99.0, 650, EfficencyRating.GOLD, PsuFormFactor.ATX, 3));

        assertTrue(session.getViolations().stream().noneMatch(v -> v.severity() == Severity.ERROR));
    }
}
