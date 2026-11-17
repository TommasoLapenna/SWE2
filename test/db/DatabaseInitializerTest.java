package db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseInitializerTest {

    @TempDir
    Path tempDir;

    @Test
    void splitStatementsIgnoresSemicolonsAndApostrophesInComments() {
        String script = """
                -- commento con punto e virgola; e apostrofo dell'app
                CREATE TABLE a (x TEXT DEFAULT 'a;b'); -- altro; commento
                INSERT INTO a (x) VALUES ('it''s');
                """;
        List<String> statements = DatabaseInitializer.splitStatements(script);
        assertEquals(2, statements.size());
        assertEquals("CREATE TABLE a (x TEXT DEFAULT 'a;b')", statements.get(0));
        assertEquals("INSERT INTO a (x) VALUES ('it''s')", statements.get(1));
    }

    @Test
    void ensureCreatedBuildsAllTables() throws SQLException {
        DatabaseConnection db = DatabaseConnection.forFile(tempDir.resolve("test.db"));
        DatabaseInitializer.ensureCreated(db);

        // component + 9 tabelle dei tipi + component_support + build + build_component
        assertEquals(13, count(db, "SELECT count(*) FROM sqlite_master "
                + "WHERE type = 'table' AND name NOT LIKE 'sqlite_%'"));
        assertEquals(0, count(db, "SELECT count(*) FROM component"));
    }

    @Test
    void ensureCreatedIsIdempotentAndKeepsTheData() throws SQLException {
        DatabaseConnection db = DatabaseConnection.forFile(tempDir.resolve("test.db"));
        DatabaseInitializer.ensureCreated(db);
        execute(db, "INSERT INTO build (name) VALUES ('prova')");

        DatabaseInitializer.ensureCreated(db);   // la seconda volta non deve fare nulla ne' fallire

        assertEquals(1, count(db, "SELECT count(*) FROM build"));
    }

    @Test
    void aDatabaseWithTheOldSchemaIsRejectedWithAClearError() throws SQLException {
        DatabaseConnection db = DatabaseConnection.forFile(tempDir.resolve("old.db"));
        execute(db, "CREATE TABLE cpu (id_cpu INTEGER PRIMARY KEY)");   // schema vecchio: cpu senza component

        assertThrows(PersistenceException.class, () -> DatabaseInitializer.ensureCreated(db));
        assertEquals(1, count(db, "SELECT count(*) FROM sqlite_master WHERE type = 'table'"));   // non ha toccato nulla
    }

    @Test
    void connectionsHaveForeignKeysEnabled() throws SQLException {
        DatabaseConnection db = DatabaseConnection.forFile(tempDir.resolve("test.db"));
        assertEquals(1, count(db, "PRAGMA foreign_keys"));
    }

    private static void execute(DatabaseConnection db, String sql) throws SQLException {
        try (Connection c = db.open(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static int count(DatabaseConnection db, String sql) throws SQLException {
        try (Connection c = db.open(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
