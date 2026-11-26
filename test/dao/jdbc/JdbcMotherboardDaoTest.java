package dao.jdbc;

import main.dao.jdbc.ComponentDao;
import db.DatabaseConnection;
import db.PersistenceException;
import main.dao.jdbc.MotherboardDao;
import main.domain.model.attribute.FormFactor;
import main.domain.model.attribute.MemoryGen;
import main.domain.model.attribute.Socket;
import main.domain.model.component.Motherboard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcMotherboardDaoTest extends ComponentDaoContractTest<Motherboard> {

    @Override ComponentDao<Motherboard> createDao(DatabaseConnection db) { return new MotherboardDao(db); }

    @Override Motherboard sample(String model) {
        return new Motherboard(0, "MSI", model, 219.0, Socket.AM5, "B850", FormFactor.ATX,
                MemoryGen.DDR5, 4, 192, 8000, 2, 3, 4);
    }

    @Override Motherboard modified(Motherboard saved) {
        return new Motherboard(saved.id(), "ASUS", "Prime", 149.0, Socket.LGA1700, "B760", FormFactor.MICRO_ATX,
                MemoryGen.DDR4, 2, 64, 5000, 1, 2, 4);
    }

    @Override Motherboard missing() {
        return new Motherboard(999, "X", "Y", 1.0, Socket.AM5, "B850", FormFactor.ATX, MemoryGen.DDR5, 2, 32, 4800, 1, 1, 2);
    }

    @Override String buildTable() { return "build_motherboard"; }
    @Override String buildIdColumn() { return "id_motherboard"; }

    @Test
    void aChipsetNotInTheLookupTableIsRejected() {
        Motherboard invalid = new Motherboard(0, "Test", "ChipsetKO", 1.0, Socket.AM5, "Z999", FormFactor.ATX,
                MemoryGen.DDR5, 4, 128, 6000, 1, 2, 4);

        assertThrows(PersistenceException.class, () -> dao.insert(invalid));
        assertEquals(0, count("SELECT count(*) FROM motherboard"));
    }
}
