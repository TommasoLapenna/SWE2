package dao.jdbc;

import main.dao.ComponentDao;
import db.DatabaseConnection;
import main.dao.jdbc.CoolerDao;
import main.domain.model.attribute.CoolerType;
import main.domain.model.attribute.Socket;
import main.domain.model.component.Cooler;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcCoolerDaoTest extends ComponentDaoContractTest<Cooler> {

    @Override ComponentDao<Cooler> createDao(DatabaseConnection db) { return new CoolerDao(db); }

    @Override Cooler sample(String model) {      // AIR
        return new Cooler(0, "Noctua", model, 109.0, 5, CoolerType.AIR,
                Set.of(Socket.AM5, Socket.LGA1700), 250, 165, 0);
    }

    /** Da AIR a LIQUID: cambia anche quale dimensione e' valorizzata. */
    @Override Cooler modified(Cooler saved) {
        return new Cooler(saved.id(), "Arctic", "Liquid Freezer", 99.0, 5, CoolerType.LIQUID,
                Set.of(Socket.AM5), 300, 0, 280);
    }

    @Override Cooler missing() {
        return new Cooler(999, "X", "Y", 1.0, 5, CoolerType.AIR, Set.of(Socket.AM5), 100, 120, 0);
    }

    @Override String buildTable() { return "build_cooler"; }
    @Override String buildIdColumn() { return "id_cooler"; }

    @Test
    void inapplicableDimensionIsStoredAsNullNotZero() {
        dao.insert(sample("Aria"));
        dao.insert(modified(new Cooler(2, "", "", 0, 5, CoolerType.AIR, Set.of(), 0, 0, 0)));

        assertEquals(1, count("SELECT count(*) FROM cooler WHERE cooler_type = 'AIR' "
                + "AND height_mm = 165 AND radiator_size_mm IS NULL"));
    }

    @Test
    void liquidCoolerIsStoredWithNullHeight() {
        Cooler liquid = dao.insert(new Cooler(0, "Arctic", "LF", 99.0, 5, CoolerType.LIQUID,
                Set.of(Socket.AM5), 300, 0, 280));

        assertEquals(1, count("SELECT count(*) FROM cooler WHERE cooler_type = 'LIQUID' "
                + "AND radiator_size_mm = 280 AND height_mm IS NULL"));
        assertEquals(liquid, dao.findById(liquid.id()).orElseThrow());   // NULL riletto come 0
    }

    @Test
    void invalidDimensionsAreRejectedWithAClearError() {
        Cooler airWithRadiator = new Cooler(0, "T", "A1", 1.0, 5, CoolerType.AIR, Set.of(Socket.AM5), 100, 150, 240);
        Cooler airWithoutHeight = new Cooler(0, "T", "A2", 1.0, 5, CoolerType.AIR, Set.of(Socket.AM5), 100, 0, 0);
        Cooler liquidWithHeight = new Cooler(0, "T", "L1", 1.0, 5, CoolerType.LIQUID, Set.of(Socket.AM5), 100, 100, 240);
        Cooler liquidWithoutRadiator = new Cooler(0, "T", "L2", 1.0, 5, CoolerType.LIQUID, Set.of(Socket.AM5), 100, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> dao.insert(airWithRadiator));
        assertThrows(IllegalArgumentException.class, () -> dao.insert(airWithoutHeight));
        assertThrows(IllegalArgumentException.class, () -> dao.insert(liquidWithHeight));
        assertThrows(IllegalArgumentException.class, () -> dao.insert(liquidWithoutRadiator));
        assertEquals(0, count("SELECT count(*) FROM cooler"));
    }

    @Test
    void aCoolerWithoutSocketsIsRejected() {
        Cooler noSockets = new Cooler(0, "T", "NoSocket", 1.0, 5, CoolerType.AIR, Set.of(), 100, 150, 0);

        assertThrows(IllegalArgumentException.class, () -> dao.insert(noSockets));
    }
}
