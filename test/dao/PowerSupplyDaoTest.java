package dao;

import db.DatabaseConnection;
import model.attribute.EfficencyRating;
import model.attribute.PsuFormFactor;
import model.component.PowerSupply;

class PowerSupplyDaoTest extends ComponentDaoContractTest<PowerSupply> {

    @Override ComponentDao<PowerSupply> createDao(DatabaseConnection db) { return new PowerSupplyDao(db); }

    @Override PowerSupply sample(String model) {
        return new PowerSupply(0, "Corsair", model, 99.0, 750, EfficencyRating.GOLD, PsuFormFactor.ATX, 4);
    }

    @Override PowerSupply modified(PowerSupply saved) {
        return new PowerSupply(saved.id(), "Seasonic", "Focus", 129.0, 850, EfficencyRating.PLATINUM, PsuFormFactor.SFX, 6);
    }

    @Override PowerSupply missing() {
        return new PowerSupply(999, "X", "Y", 1.0, 500, EfficencyRating.BRONZE, PsuFormFactor.ATX, 2);
    }

}
