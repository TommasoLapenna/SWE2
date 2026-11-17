package dao;

import build.BuildView;
import build.PcBuilder;
import db.DatabaseConnection;
import db.DatabaseInitializer;
import db.PersistenceException;
import model.attribute.*;
import model.component.*;
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

    /** La vista di sola lettura di una build in memoria: e' quello che l'applicazione passa al DAO. */
    private static BuildView build(String name, List<? extends Component> components) {
        PcBuilder builder = new PcBuilder(name);
        components.forEach(builder::addComponent);
        return builder.view();
    }

    /** Tutti i componenti della build salvata, nell'ordine canonico. */
    private static List<Component> componentsOf(SavedBuild saved) {
        return saved.components().values().stream().flatMap(List::stream).toList();
    }

    @Test
    void saveAndFindByIdReturnTheSameBuild() {
        SavedBuild saved = dao.save(build("Gaming", fullBuild()));

        assertTrue(saved.id() > 0);
        assertEquals("Gaming", saved.name());
        assertNotNull(saved.createdAt());
        assertEquals(fullBuild(), componentsOf(saved));
        assertEquals(saved, dao.findById(saved.id()).orElseThrow());
    }

    @Test
    void inputOrderDoesNotMatterAndComponentsAreReadInCanonicalOrder() {
        SavedBuild saved = dao.save(build("Disordinata", List.of(mouse, cooler, psu, pcCase, motherboard, hdd, ssd, memory, gpu, cpu)));

        assertEquals(fullBuild(), componentsOf(saved));
    }

    @Test
    void theSameComponentTwiceIsStoredOnceWithQuantityTwo() {
        SavedBuild saved = dao.save(build("Doppio SSD", List.of(cpu, ssd, ssd)));

        assertEquals(List.of(cpu, ssd, ssd), componentsOf(saved));
        assertEquals(2, count("SELECT count(*) FROM build_component"));     // una riga per la CPU, una per l'SSD
        assertEquals(2, count("SELECT quantity FROM build_component WHERE id_component = " + ssd.id()));
    }

    @Test
    void anEmptyBuildIsAllowed() {
        SavedBuild saved = dao.save(build("Vuota", List.of()));

        assertTrue(componentsOf(saved).isEmpty());
        assertEquals(EnumSet.allOf(ComponentType.class), saved.components().keySet());   // tutti i tipi, liste vuote
        assertFalse(saved.hasComponent(ComponentType.CPU));
        assertEquals(saved, dao.findById(saved.id()).orElseThrow());
    }

    @Test
    void totalsAreDerivedNotStored() {
        SavedBuild saved = dao.save(build("Totali", List.of(cpu, gpu, ssd, ssd)));

        assertEquals(389.0 + 599.0 + 99.0 + 99.0, saved.totalPrice(), 0.001);
        assertEquals(120 + 200 + 10 + 10, saved.totalWattage());   // CPU e GPU: TDP; Storage: 10 W
    }

    @Test
    void updateReplacesNameAndComponents() {
        SavedBuild saved = dao.save(build("Prima", fullBuild()));

        assertTrue(dao.update(saved.id(), build("Dopo", List.of(cpu, gpu))));

        SavedBuild reloaded = dao.findById(saved.id()).orElseThrow();
        assertEquals("Dopo", reloaded.name());
        assertEquals(List.of(cpu, gpu), componentsOf(reloaded));
        assertEquals(2, count("SELECT count(*) FROM build_component"));    // le righe vecchie non restano
    }

    @Test
    void updateReturnsFalseWhenBuildDoesNotExist() {
        assertFalse(dao.update(999, build("X", List.of(cpu))));
    }

    @Test
    void deleteRemovesTheBuildButKeepsTheComponents() {
        SavedBuild saved = dao.save(build("Da cancellare", fullBuild()));

        assertTrue(dao.delete(saved.id()));
        assertTrue(dao.findById(saved.id()).isEmpty());
        assertEquals(0, count("SELECT count(*) FROM build_component"));
        assertTrue(new CpuDao(db).findById(cpu.id()).isPresent());
        assertFalse(dao.delete(saved.id()));
    }

    @Test
    void findAllReturnsSummariesOrderedById() {
        SavedBuild a = dao.save(build("A", List.of(cpu)));
        SavedBuild b = dao.save(build("B", List.of(gpu)));

        List<BuildSummary> all = dao.findAll();

        assertEquals(List.of(a.id(), b.id()), all.stream().map(BuildSummary::id).toList());
        assertEquals(List.of("A", "B"), all.stream().map(BuildSummary::name).toList());
    }

    @Test
    void aComponentNotInTheCatalogIsRejectedBeforeWritingAnything() {
        Cpu unsaved = new Cpu(0, "AMD", "NonSalvata", 1.0, 65, Socket.AM5, 4, 8, 3.0, Set.of(MemoryGen.DDR5), false, false);

        assertThrows(IllegalArgumentException.class, () -> dao.save(build("X", List.of(cpu, unsaved))));
        assertEquals(0, count("SELECT count(*) FROM build"));
    }

    @Test
    void aComponentIdThatDoesNotExistRollsBackTheWholeSave() {
        Cpu ghost = new Cpu(999, "AMD", "Fantasma", 1.0, 65, Socket.AM5, 4, 8, 3.0, Set.of(MemoryGen.DDR5), false, false);

        assertThrows(PersistenceException.class, () -> dao.save(build("X", List.of(cpu, gpu, ghost))));
        assertEquals(0, count("SELECT count(*) FROM build"));
        assertEquals(0, count("SELECT count(*) FROM build_component"));     // niente a meta'
    }

    @Test
    void aSavedBuildCanBeSavedAgainAsANewBuild() {
        SavedBuild first = dao.save(build("Originale", List.of(cpu, ssd, ssd)));
        SavedBuild copy = dao.save(first);      // SavedBuild e' una BuildView

        assertTrue(copy.id() != first.id());
        assertEquals(first.components(), copy.components());
        assertEquals(2, copy.getComponentNumber(ComponentType.STORAGE));
        assertEquals(List.of(ssd, ssd), copy.getComponents(ComponentType.STORAGE));
    }

    @Test
    void theNameIsTrimmed() {
        assertEquals("Gaming", dao.save(build("  Gaming ", List.of(cpu))).name());
    }

    @Test
    void aBuildNeedsAName() {
        assertThrows(IllegalArgumentException.class, () -> dao.save(build("  ", List.of(cpu))));
    }

    @Test
    void everyComponentTypeCanBeStoredInABuild() {
        assertEquals(EnumSet.allOf(ComponentType.class), dao.supportedTypes());
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
