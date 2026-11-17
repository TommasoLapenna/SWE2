package application;

import build.PcBuilder;
import dao.BuildSummary;
import dao.SavedBuild;
import dao.jdbc.JdbcBuildDao;
import dao.jdbc.JdbcCpuDao;
import dao.jdbc.JdbcGpuDao;
import dao.jdbc.JdbcStorageDao;
import db.DatabaseConnection;
import db.DatabaseInitializer;
import model.attribute.MemoryGen;
import model.attribute.Socket;
import model.attribute.StorageTech;
import model.attribute.StorageType;
import model.component.Component;
import model.component.Cpu;
import model.component.Gpu;
import model.component.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prova il percorso completo con il vero PcBuilder: build in memoria, database, build ricostruita. */
class BuildStoreTest {

    @TempDir
    Path tempDir;

    BuildStore store;
    Cpu cpu;
    Gpu gpu;
    Storage ssd;
    Storage hdd;

    @BeforeEach
    void setUp() {
        DatabaseConnection db = DatabaseConnection.forFile(tempDir.resolve("store-test.db"));
        DatabaseInitializer.ensureCreated(db);
        store = new BuildStore(new JdbcBuildDao(db));

        // il catalogo: componenti salvati con il loro DAO, quindi con un id vero
        cpu = new JdbcCpuDao(db).insert(new Cpu(0, "AMD", "Ryzen", 389.0, 120, Socket.AM5, 8, 16, 4.2,
                Set.of(MemoryGen.DDR5), true, false));
        gpu = new JdbcGpuDao(db).insert(new Gpu(0, "NVIDIA", "RTX", 599.0, 200, 12, 244, 2.0, 650, 1));
        ssd = new JdbcStorageDao(db).insert(new Storage(0, "Samsung", "980", 99.0, StorageType.SSD, StorageTech.M2_NVME, 1000));
        hdd = new JdbcStorageDao(db).insert(new Storage(0, "Seagate", "Barracuda", 55.0, StorageType.HDD, StorageTech.SATA, 2000));
    }

    private static PcBuilder builderWith(String name, Component... components) {
        PcBuilder builder = new PcBuilder(name);
        for (Component component : components) {
            builder.addComponent(component);
        }
        return builder;
    }

    /** Ricostruzione di una build salvata, come fara' l'applicazione. */
    private static PcBuilder rebuild(SavedBuild saved) {
        PcBuilder builder = new PcBuilder(saved.name());
        saved.components().forEach(builder::addComponent);
        return builder;
    }

    @Test
    void aSavedBuildCanBeRebuiltExactly() {
        // nello stesso tipo l'ordine di lettura e' per id: qui ssd (id minore) prima di hdd
        PcBuilder original = builderWith("Gaming", cpu, gpu, ssd, ssd, hdd);

        SavedBuild saved = store.save(original.view());
        PcBuilder rebuilt = rebuild(store.find(saved.id()).orElseThrow());

        assertEquals(original.name(), rebuilt.name());
        assertEquals(original.view().components(), rebuilt.view().components());
        assertEquals(original.totalWattage(), rebuilt.totalWattage());
        assertEquals(original.totalWattage(), saved.totalPowerDrawWatts());   // stesso consumo, calcolato in due modi
    }

    @Test
    void updateOverwritesTheSavedBuild() {
        SavedBuild saved = store.save(builderWith("Gaming", cpu, gpu).view());

        assertTrue(store.update(saved.id(), builderWith("Gaming v2", cpu).view()));

        SavedBuild reloaded = store.find(saved.id()).orElseThrow();
        assertEquals("Gaming v2", reloaded.name());
        assertEquals(List.of(cpu), reloaded.components());
    }

    @Test
    void listAndDelete() {
        SavedBuild a = store.save(builderWith("A", cpu).view());
        SavedBuild b = store.save(builderWith("B", gpu).view());

        assertEquals(List.of("A", "B"), store.list().stream().map(BuildSummary::name).toList());
        assertTrue(store.delete(a.id()));
        assertEquals(List.of(b.id()), store.list().stream().map(BuildSummary::id).toList());
        assertFalse(store.delete(a.id()));
    }

    @Test
    void aComponentThatDoesNotComeFromTheCatalogCannotBeSaved() {
        Cpu notInCatalog = new Cpu(0, "AMD", "Inventata", 1.0, 65, Socket.AM5, 4, 8, 3.0,
                Set.of(MemoryGen.DDR5), false, false);

        assertThrows(IllegalArgumentException.class, () -> store.save(builderWith("X", cpu, notInCatalog).view()));
        assertTrue(store.list().isEmpty());
    }
}
