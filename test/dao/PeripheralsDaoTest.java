package dao;

import db.DatabaseConnection;
import model.component.Peripherals;

class PeripheralsDaoTest extends ComponentDaoContractTest<Peripherals> {

    @Override ComponentDao<Peripherals> createDao(DatabaseConnection db) { return new PeripheralsDao(db); }

    @Override Peripherals sample(String model) { return new Peripherals(0, "Logitech", model, 49.0, 2); }

    @Override Peripherals modified(Peripherals saved) {
        return new Peripherals(saved.id(), "Razer", "Basilisk", 69.0, 3);
    }

    @Override Peripherals missing() { return new Peripherals(999, "X", "Y", 1.0, 1); }

}
