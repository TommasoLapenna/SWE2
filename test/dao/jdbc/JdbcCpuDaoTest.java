package dao.jdbc;

import db.DatabaseConnection;
import db.DatabaseInitializer;
import db.PersistenceException;
import model.attribute.MemoryGen;
import model.attribute.Socket;
import model.component.Cpu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcCpuDaoTest {

    @TempDir
    Path tempDir;

    DatabaseConnection db;
    JdbcCpuDao dao;

    @BeforeEach
    void setUp() {
        db = DatabaseConnection.forFile(tempDir.resolve("cpu-test.db"));
        DatabaseInitializer.ensureCreated(db);
        dao = new JdbcCpuDao(db);
    }

    /** id = 0: record non ancora salvato. */
    private static Cpu newCpu(String model, Set<MemoryGen> gens) {
        return new Cpu(0, "AMD", model, 389.0, 120, Socket.AM5, 8, 16, 4.2, gens, true, false);
    }

    @Test
    void insertAssignsIdAndFindByIdReturnsTheSameCpu() {
        Cpu saved = dao.insert(newCpu("Ryzen 7 7800X3D", Set.of(MemoryGen.DDR5)));

        assertTrue(saved.id() > 0);
        assertEquals(saved, dao.findById(saved.id()).orElseThrow());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(dao.findById(999).isEmpty());
    }

    @Test
    void findAllReturnsAllCpusWithTheirMemoryGensOrderedById() {
        Cpu a = dao.insert(newCpu("A", Set.of(MemoryGen.DDR5)));
        Cpu b = dao.insert(newCpu("B", Set.of(MemoryGen.DDR4, MemoryGen.DDR5)));

        List<Cpu> all = dao.findAll();

        assertEquals(List.of(a, b), all);
        assertNotEquals(a.id(), b.id());
    }

    @Test
    void updateChangesFieldsAndMemoryGens() {
        Cpu saved = dao.insert(newCpu("Ryzen", Set.of(MemoryGen.DDR5)));
        Cpu changed = new Cpu(saved.id(), "AMD", "Ryzen X", 299.0, 65, Socket.AM4, 6, 12, 3.6,
                Set.of(MemoryGen.DDR4, MemoryGen.DDR5), false, true);

        assertTrue(dao.update(changed));
        assertEquals(changed, dao.findById(saved.id()).orElseThrow());
    }

    @Test
    void updateReturnsFalseWhenCpuDoesNotExist() {
        assertFalse(dao.update(new Cpu(999, "AMD", "X", 1.0, 65, Socket.AM5, 4, 8, 3.0,
                Set.of(MemoryGen.DDR5), false, false)));
    }

    @Test
    void deleteRemovesCpuAndItsMemoryGens() throws Exception {
        Cpu saved = dao.insert(newCpu("Ryzen", Set.of(MemoryGen.DDR4, MemoryGen.DDR5)));

        assertTrue(dao.delete(saved.id()));
        assertTrue(dao.findById(saved.id()).isEmpty());
        assertEquals(0, rows("SELECT count(*) FROM cpu_memory_gen"));   // cancellate in cascata
        assertFalse(dao.delete(saved.id()));                            // seconda volta: non esiste piu'
    }

    @Test
    void insertWithoutMemoryGensIsRejectedAndWritesNothing() {
        assertThrows(IllegalArgumentException.class, () -> dao.insert(newCpu("Senza", Set.of())));
        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    void failedInsertLeavesNothingBehind() throws Exception {
        // threads < cores viola un CHECK del database: l'intera transazione deve annullarsi
        Cpu invalid = new Cpu(0, "AMD", "Invalida", 1.0, 65, Socket.AM5, 8, 4, 3.0,
                Set.of(MemoryGen.DDR5), false, false);

        assertThrows(PersistenceException.class, () -> dao.insert(invalid));
        assertTrue(dao.findAll().isEmpty());
        assertEquals(0, rows("SELECT count(*) FROM cpu_memory_gen"));
    }

    @Test
    void cpuUsedInABuildCannotBeDeleted() throws Exception {
        Cpu saved = dao.insert(newCpu("Ryzen", Set.of(MemoryGen.DDR5)));
        db.write(connection -> {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("INSERT INTO build (nome) VALUES ('prova')");
                st.executeUpdate("INSERT INTO build_cpu (id_build, id_cpu) VALUES (1, " + saved.id() + ")");
            }
            return null;
        }, "preparazione test");

        assertThrows(PersistenceException.class, () -> dao.delete(saved.id()));
        assertTrue(dao.findById(saved.id()).isPresent());
    }

    private int rows(String sql) throws Exception {
        try (var c = db.open(); var st = c.createStatement(); var rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
