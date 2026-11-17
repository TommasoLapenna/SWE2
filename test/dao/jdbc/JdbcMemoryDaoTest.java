package dao.jdbc;

import main.dao.ComponentDao;
import db.DatabaseConnection;
import main.dao.jdbc.MemoryDao;
import main.domain.model.attribute.MemoryGen;
import main.domain.model.component.Memory;

class JdbcMemoryDaoTest extends ComponentDaoContractTest<Memory> {

    @Override ComponentDao<Memory> createDao(DatabaseConnection db) { return new MemoryDao(db); }

    @Override Memory sample(String model) {
        return new Memory(0, "Corsair", model, 119.0, MemoryGen.DDR5, 2, 16, 6000);
    }

    @Override Memory modified(Memory saved) {
        return new Memory(saved.id(), "Kingston", "Fury", 99.0, MemoryGen.DDR4, 4, 8, 3200);
    }

    @Override Memory missing() { return new Memory(999, "X", "Y", 1.0, MemoryGen.DDR5, 1, 8, 4800); }

    @Override String buildTable() { return "build_memory"; }
    @Override String buildIdColumn() { return "id_memory"; }
}
