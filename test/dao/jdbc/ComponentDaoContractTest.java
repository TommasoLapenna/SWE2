package dao.jdbc;

import main.dao.ComponentDao;
import db.DatabaseConnection;
import db.DatabaseInitializer;
import db.PersistenceException;
import main.domain.model.component.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test comuni a TUTTI i DAO dei componenti. Ogni DAO ha una sottoclasse che dice solo come
 * costruire un componente di esempio. Un nuovo DAO eredita questi test gratis.
 */
abstract class ComponentDaoContractTest<T extends Component> {

    @TempDir
    Path tempDir;

    DatabaseConnection db;
    ComponentDao<T> dao;

    abstract ComponentDao<T> createDao(DatabaseConnection db);

    /** Componente di esempio VALIDO, non ancora salvato (id 0). Modelli diversi = componenti diversi. */
    abstract T sample(String model);

    /** Stesso id del componente salvato, ma con campi diversi (e ancora validi). */
    abstract T modified(T saved);

    /** Un componente con un id che non esiste nel database. */
    abstract T missing();

    /** Tabella ponte delle build per questo tipo e sua colonna verso il componente (es. build_cpu, id_cpu). */
    abstract String buildTable();

    abstract String buildIdColumn();

    @BeforeEach
    void setUp() {
        db = DatabaseConnection.forFile(tempDir.resolve("component-test.db"));
        DatabaseInitializer.ensureCreated(db);
        dao = createDao(db);
    }

    @Test
    void insertAssignsIdAndFindByIdReturnsTheSameComponent() {
        T saved = dao.insert(sample("Uno"));

        assertTrue(saved.id() > 0);
        assertEquals(saved, dao.findById(saved.id()).orElseThrow());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertTrue(dao.findById(999).isEmpty());
    }

    @Test
    void findAllReturnsAllComponentsOrderedById() {
        T a = dao.insert(sample("A"));
        T b = dao.insert(sample("B"));

        assertEquals(List.of(a, b), dao.findAll());
        assertNotEquals(a.id(), b.id());
    }

    @Test
    void updateChangesFields() {
        T saved = dao.insert(sample("Uno"));
        T changed = modified(saved);

        assertTrue(dao.update(changed));
        assertEquals(changed, dao.findById(saved.id()).orElseThrow());
    }

    @Test
    void updateReturnsFalseWhenComponentDoesNotExist() {
        assertFalse(dao.update(missing()));
    }

    @Test
    void deleteRemovesTheComponent() {
        T saved = dao.insert(sample("Uno"));

        assertTrue(dao.delete(saved.id()));
        assertTrue(dao.findById(saved.id()).isEmpty());
        assertFalse(dao.delete(saved.id()));    // la seconda volta non esiste piu'
    }

    @Test
    void componentUsedInABuildCannotBeDeleted() {
        T saved = dao.insert(sample("Uno"));
        db.write(connection -> {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("INSERT INTO build (nome) VALUES ('prova')");
                st.executeUpdate("INSERT INTO " + buildTable() + " (id_build, " + buildIdColumn()
                        + ") VALUES (1, " + saved.id() + ")");
            }
            return null;
        }, "preparazione del test");

        assertThrows(PersistenceException.class, () -> dao.delete(saved.id()));
        assertTrue(dao.findById(saved.id()).isPresent());
    }

    /** Conta le righe con una query libera (per controllare cosa e' finito davvero nel database). */
    int count(String sql) {
        return db.read(connection -> {
            try (Statement st = connection.createStatement(); var rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }, "conteggio fallito");
    }
}
