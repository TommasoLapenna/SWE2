package dao.jdbc;

import dao.ComponentDao;
import db.DatabaseConnection;
import db.PersistenceException;
import model.attribute.StorageTech;
import model.attribute.StorageType;
import model.component.Storage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcStorageDaoTest extends ComponentDaoContractTest<Storage> {

    @Override ComponentDao<Storage> createDao(DatabaseConnection db) { return new JdbcStorageDao(db); }

    @Override Storage sample(String model) {
        return new Storage(0, "Samsung", model, 99.0, StorageType.SSD, StorageTech.M2_NVME, 1000);
    }

    @Override Storage modified(Storage saved) {
        return new Storage(saved.id(), "Seagate", "Barracuda", 55.0, StorageType.HDD, StorageTech.SATA, 2000);
    }

    @Override Storage missing() { return new Storage(999, "X", "Y", 1.0, StorageType.SSD, StorageTech.SATA, 250); }

    @Override String buildTable() { return "build_storage"; }
    @Override String buildIdColumn() { return "id_storage"; }

    @Test
    void anHddCannotUseAnM2Interface() {
        Storage invalid = new Storage(0, "Test", "HddNvme", 1.0, StorageType.HDD, StorageTech.M2_NVME, 500);

        assertThrows(PersistenceException.class, () -> dao.insert(invalid));
        assertEquals(0, count("SELECT count(*) FROM storage"));
    }
}
