package build;

import main.domain.event.BuildEventType;
import main.domain.model.PcBuilder;
import main.domain.model.attribute.ComponentType;
import main.domain.model.attribute.FormFactor;
import main.domain.model.attribute.MemoryGen;
import main.domain.model.attribute.Socket;
import main.domain.model.attribute.StorageTech;
import main.domain.model.attribute.StorageType;
import main.domain.model.component.Cpu;
import main.domain.model.component.Motherboard;
import main.domain.model.component.Storage;
import main.pattern.memento.Memento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Totali della build e i due pattern implementati da PcBuilder: Observer e Memento. */
class PcBuilderTest {

    PcBuilder build;

    @BeforeEach
    void setUp() {
        build = new PcBuilder("testbuild");
    }

    /** 299.0 EUR, 105 W. */
    private static Cpu newCpu() {
        return new Cpu(1, "AMD", "Ryzen 7 7700X", 299.0, 105, Socket.AM5, 8, 16, 4.5,
                Set.of(MemoryGen.DDR5), true, false);
    }

    /** 239.0 EUR, 40 W (consumo fisso della scheda madre). */
    private static Motherboard newMotherboard() {
        return new Motherboard(2, "ASUS", "TUF GAMING B850-PLUS", 239.0, Socket.AM5, "B850",
                FormFactor.ATX, MemoryGen.DDR5, 4, 192, 8000, 2, 3, 4);
    }

    /** 99.0 EUR, 10 W (consumo fisso dello storage). */
    private static Storage newSsd() {
        return new Storage(3, "Samsung", "990 Pro 1TB", 99.0, StorageType.SSD, StorageTech.M2_NVME, 1000);
    }

    @Test
    void name() {
        assertEquals("testbuild", build.name());
    }

    @Test
    void emptyBuildHasZeroTotals() {
        assertEquals(0.0, build.totalPrice(), 1e-9);
        assertEquals(0, build.totalWattage());
    }

    @Test
    void totalPriceAndWattageAreTheSumOfTheComponents() {
        build.addComponent(newCpu());
        build.addComponent(newMotherboard());

        assertEquals(538.0, build.totalPrice(), 1e-9);
        assertEquals(145, build.totalWattage());
    }

    @Test
    void componentsOfTheSameTypeAreAllCounted() {
        build.addComponent(newSsd());
        build.addComponent(newSsd());

        assertEquals(2, build.getComponentNumber(ComponentType.STORAGE));
        assertEquals(198.0, build.totalPrice(), 1e-9);
        assertEquals(20, build.totalWattage());
    }

    @Test
    void observersAreNotifiedOfAddAndRemove() {
        List<BuildEventType> received = new ArrayList<>();
        build.attach(e -> received.add(e.type()));
        Cpu cpu = newCpu();

        build.addComponent(cpu);
        build.removeComponent(cpu);
        build.removeComponent(cpu);   // gia' rimosso: nessuna notifica

        assertEquals(List.of(BuildEventType.ADDED, BuildEventType.REMOVED), received);
    }

    @Test
    void restoringAMementoNotCreatedByTheBuilderIsRejected() {
        Memento foreign = new Memento() {};

        assertThrows(IllegalArgumentException.class, () -> build.restoreMemento(foreign));
    }
}
