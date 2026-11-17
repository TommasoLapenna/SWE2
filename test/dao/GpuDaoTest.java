package dao;

import db.DatabaseConnection;
import db.PersistenceException;
import model.component.Gpu;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GpuDaoTest extends ComponentDaoContractTest<Gpu> {

    @Override ComponentDao<Gpu> createDao(DatabaseConnection db) { return new GpuDao(db); }

    @Override Gpu sample(String model) {
        return new Gpu(0, "NVIDIA", model, 599.0, 200, 12, 244, 2.5, 650, 1);
    }

    @Override Gpu modified(Gpu saved) {
        return new Gpu(saved.id(), "AMD", "RX", 449.0, 250, 16, 300, 3.0, 750, 2);
    }

    @Override Gpu missing() { return new Gpu(999, "X", "Y", 1.0, 100, 8, 200, 2.0, 500, 0); }


    @Test
    void aGpuPoweredOnlyByTheSlotIsValid() {
        Gpu slotPowered = dao.insert(new Gpu(0, "NVIDIA", "GT", 99.0, 50, 4, 150, 2.0, 300, 0));

        assertEquals(0, dao.findById(slotPowered.id()).orElseThrow().powerConnectors());
    }

    @Test
    void invalidLengthIsRejectedAndWritesNothing() {
        Gpu invalid = new Gpu(0, "T", "BadLength", 1.0, 100, 8, 0, 2.0, 500, 1);

        assertThrows(PersistenceException.class, () -> dao.insert(invalid));
        assertEquals(0, count("SELECT count(*) FROM gpu"));
    }
}
