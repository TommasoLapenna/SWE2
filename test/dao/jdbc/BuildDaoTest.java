package dao.jdbc;

import main.dao.BuildSummary;
import main.dao.SavedBuild;
import main.dao.jdbc.*;
import db.DatabaseConnection;
import db.DatabaseInitializer;
import db.PersistenceException;
import main.domain.model.attribute.*;
import main.domain.model.component.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildDaoTest {

    @TempDir
    Path tempDir;

    DatabaseConnection db;
    BuildDao dao;

    Cpu cpu;
    Gpu gpu;
    Memory memory;
    Storage ssd;
    Storage hdd;
    Motherboard motherboard;
    Case pcCase;
    PowerSupply psu;
    Cooler cooler;
    Peripherals mouse;

    @BeforeEach
    void setUp() {
        db = DatabaseConnection.forFile(tempDir.resolve("build-test.db"));
        DatabaseInitializer.ensureCreated(db);
        dao = new BuildDao(db);

        cpu = new CpuDao(db).insert(new Cpu(0, "AMD", "Ryzen", 389.0, 120, Socket.AM5, 8, 16, 4.2,
                Set.of(MemoryGen.DDR5), true, false));
        gpu = new GpuDao(db).insert(new Gpu(0, "NVIDIA", "RTX", 599.0, 200, 12, 244, 2.0, 650, 1));
        memory = new MemoryDao(db).insert(new Memory(0, "Corsair", "Vengeance", 119.0, MemoryGen.DDR5, 2, 16, 6000));
        ssd = new StorageDao(db).insert(new Storage(0, "Samsung", "980", 99.0, StorageType.SSD, StorageTech.M2_NVME, 1000));
        hdd = new StorageDao(db).insert(new Storage(0, "Seagate", "Barracuda", 55.0, StorageType.HDD, StorageTech.SATA, 2000));
        motherboard = new MotherboardDao(db).insert(new Motherboard(0, "MSI", "Tomahawk", 219.0, Socket.AM5, "B850",
                FormFactor.ATX, MemoryGen.DDR5, 4, 192, 8000, 2, 3, 4));
        pcCase = new CaseDao(db).insert(new Case(0, "NZXT", "H5", 89.0, 5, Set.of(FormFactor.ATX),
                Set.of(PsuFormFactor.ATX), 365, 165, 7, 2, Set.of(240)));
        psu = new PowerSupplyDao(db).insert(new PowerSupply(0, "Corsair", "RM750e", 99.0, 750,
                EfficencyRating.GOLD, PsuFormFactor.ATX, 4));
        cooler = new CoolerDao(db).insert(new Cooler(0, "Noctua", "D15", 109.0, 5, CoolerType.AIR,
                Set.of(Socket.AM5), 250, 165, 0));
        mouse = new PeripheralsDao(db).insert(new Peripherals(0, "Logitech", "G502", 49.0, 2));
    }

    /** Ordine canonico di lettura: per tipo (ordine dell'enum ComponentType), poi per id. */
    private List<Component> fullBuild() {
        return List.of(cpu, gpu, memory, ssd, hdd, motherboard, pcCase, psu, cooler, mouse);
    }

    @Test
    void saveAndFindByIdReturnTheSameBuild() {
        SavedBuild saved = dao.save("Gaming", fullBuild());

        assertTrue(saved.id() > 0);
        assertEquals("Gaming", saved.name());
        assertNotNull(saved.createdAt());
        assertEquals(fullBuild(), saved.components());
        assertEquals(saved, dao.findById(saved.id()).orElseThrow());
    }

    @Test
    void inputOrderDoesNotMatterAndComponentsAreReadInCanonicalOrder() {
        SavedBuild saved = dao.save("Disordinata", List.of(mouse, cooler, psu, pcCase, motherboard, hdd, ssd, memory, gpu, cpu));

        assertEquals(fullBuild(), saved.components());
    }

    @Test
    void theSameComponentTwiceIsStoredOnceWithQuantityTwo() {
        SavedBuild saved = dao.save("Doppio SSD", List.of(cpu, ssd, ssd));

        assertEquals(List.of(cpu, ssd, ssd), saved.components());
        assertEquals(1, count("SELECT count(*) FROM build_storage"));
        assertEquals(2, count("SELECT quantity FROM build_storage"));
    }

    @Test
    void anEmptyBuildIsAllowed() {
        SavedBuild saved = dao.save("Vuota", List.of());

        assertTrue(saved.components().isEmpty());
        assertEquals(saved, dao.findById(saved.id()).orElseThrow());
    }

    @Test
    void totalsAreDerivedNotStored() {
        SavedBuild saved = dao.save("Totali", List.of(cpu, gpu, ssd, ssd));

        assertEquals(389.0 + 599.0 + 99.0 + 99.0, saved.totalPrice(), 0.001);
        assertEquals(120 + 200 + 10 + 10, saved.totalPowerDrawWatts());   // CPU e GPU: TDP; Storage: 10 W
    }

    @Test
    void updateReplacesNameAndComponents() {
        SavedBuild saved = dao.save("Prima", fullBuild());

        assertTrue(dao.update(saved.id(), "Dopo", List.of(cpu, gpu)));

        SavedBuild reloaded = dao.findById(saved.id()).orElseThrow();
        assertEquals("Dopo", reloaded.name());
        assertEquals(List.of(cpu, gpu), reloaded.components());
        assertEquals(0, count("SELECT count(*) FROM build_memory"));    // le righe vecchie non restano
    }

    @Test
    void updateReturnsFalseWhenBuildDoesNotExist() {
        assertFalse(dao.update(999, "X", List.of(cpu)));
    }

    @Test
    void deleteRemovesTheBuildButKeepsTheComponents() {
        SavedBuild saved = dao.save("Da cancellare", fullBuild());

        assertTrue(dao.delete(saved.id()));
        assertTrue(dao.findById(saved.id()).isEmpty());
        assertEquals(0, count("SELECT count(*) FROM build_cpu"));
        assertTrue(new CpuDao(db).findById(cpu.id()).isPresent());
        assertFalse(dao.delete(saved.id()));
    }

    @Test
    void findAllReturnsSummariesOrderedById() {
        SavedBuild a = dao.save("A", List.of(cpu));
        SavedBuild b = dao.save("B", List.of(gpu));

        List<BuildSummary> all = dao.findAll();

        assertEquals(List.of(a.id(), b.id()), all.stream().map(BuildSummary::id).toList());
        assertEquals(List.of("A", "B"), all.stream().map(BuildSummary::name).toList());
    }

    @Test
    void aComponentNotInTheCatalogIsRejectedBeforeWritingAnything() {
        Cpu unsaved = new Cpu(0, "AMD", "NonSalvata", 1.0, 65, Socket.AM5, 4, 8, 3.0, Set.of(MemoryGen.DDR5), false, false);

        assertThrows(IllegalArgumentException.class, () -> dao.save("X", List.of(cpu, unsaved)));
        assertEquals(0, count("SELECT count(*) FROM build"));
    }

    @Test
    void aComponentIdThatDoesNotExistRollsBackTheWholeSave() {
        Cpu ghost = new Cpu(999, "AMD", "Fantasma", 1.0, 65, Socket.AM5, 4, 8, 3.0, Set.of(MemoryGen.DDR5), false, false);

        assertThrows(PersistenceException.class, () -> dao.save("X", List.of(cpu, gpu, ghost)));
        assertEquals(0, count("SELECT count(*) FROM build"));
        assertEquals(0, count("SELECT count(*) FROM build_cpu"));     // niente a meta'
    }

    @Test
    void aBuildNeedsAName() {
        assertThrows(IllegalArgumentException.class, () -> dao.save("  ", List.of(cpu)));
    }

    @Test
    void everyComponentTypeCanBeStoredInABuild() {
        assertEquals(EnumSet.allOf(ComponentType.class), BuildDao.supportedTypes());
    }

    private int count(String sql) {
        return db.read(connection -> {
            try (Statement st = connection.createStatement(); var rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }, "conteggio fallito");
    }
}
