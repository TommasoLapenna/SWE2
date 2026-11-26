package dao.jdbc;

import main.dao.jdbc.ComponentDao;
import db.DatabaseConnection;
import main.dao.jdbc.PeripheralsDao;
import main.domain.model.component.Peripherals;

class JdbcPeripheralsDaoTest extends ComponentDaoContractTest<Peripherals> {

    @Override ComponentDao<Peripherals> createDao(DatabaseConnection db) { return new PeripheralsDao(db); }

    @Override Peripherals sample(String model) { return new Peripherals(0, "Logitech", model, 49.0, 2); }

    @Override Peripherals modified(Peripherals saved) {
        return new Peripherals(saved.id(), "Razer", "Basilisk", 69.0, 3);
    }

    @Override Peripherals missing() { return new Peripherals(999, "X", "Y", 1.0, 1); }

    @Override String buildTable() { return "build_peripheral"; }
    @Override String buildIdColumn() { return "id_peripheral"; }
}
