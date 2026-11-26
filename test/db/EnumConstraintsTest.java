package db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * I valori ammessi per gli attributi di tipo enum non sono scritti in CHECK "colonna IN (...)", ma stanno
 * nelle tabelle di lookup (socket, memory_gen, ...): ogni colonna di tipo enum ha una chiave esterna verso
 * la sua tabella. Questo test controlla che ogni colonna abbia la chiave esterna giusta e che nessuna
 * colonna nuova resti senza vincolo. L'allineamento tra enum Java e righe delle tabelle e' in LookupTablesTest.
 */
class EnumConstraintsTest {

    @TempDir
    static Path tempDir;

    static DatabaseConnection db;

    /** Tabelle di lookup create da db/seed_lookups.sql. */
    private static final Set<String> LOOKUP_TABLES = Set.of(
            "socket", "memory_gen", "form_factor", "psu_form_factor", "storage_tech",
            "storage_type", "cooler_type", "efficiency_rating", "chipset");

    /** tabella.colonna -> tabella di lookup a cui deve puntare. */
    private static final Map<String, String> EXPECTED = new TreeMap<>(Map.ofEntries(
            Map.entry("cpu.socket", "socket"),
            Map.entry("cpu_memory_gen.memory_gen", "memory_gen"),
            Map.entry("memory.memory_gen", "memory_gen"),
            Map.entry("storage.storage_type", "storage_type"),
            Map.entry("storage.storage_tech", "storage_tech"),
            Map.entry("motherboard.socket", "socket"),
            Map.entry("motherboard.chipset", "chipset"),
            Map.entry("motherboard.form_factor", "form_factor"),
            Map.entry("motherboard.memory_gen", "memory_gen"),
            Map.entry("power_supply.efficiency_rating", "efficiency_rating"),
            Map.entry("power_supply.form_factor", "psu_form_factor"),
            Map.entry("case_form_factor.form_factor", "form_factor"),
            Map.entry("case_psu_form_factor.psu_form_factor", "psu_form_factor"),
            Map.entry("cooler.cooler_type", "cooler_type"),
            Map.entry("cooler_socket.socket", "socket")));

    @BeforeAll
    static void createDatabase() {
        db = DatabaseConnection.forFile(tempDir.resolve("enum-constraints.db"));
        DatabaseInitializer.ensureCreated(db);
    }

    @Test
    void everyEnumColumnReferencesItsLookupTable() throws SQLException {
        // se qualcuno aggiunge una colonna enum (o toglie una chiave esterna), le due mappe non coincidono piu'
        assertEquals(EXPECTED, foreignKeysToLookupTables());
    }

    @Test
    void aValueThatIsNotInTheEnumIsRejected() {
        assertThrows(SQLException.class, () -> {
            try (Connection c = db.open(); Statement st = c.createStatement()) {
                st.executeUpdate("INSERT INTO memory (brand, model, price, memory_gen, sticks_qty, stick_capacity_gb, frequency_mhz) "
                        + "VALUES ('X', 'Y', 10.0, 'DDR9', 2, 8, 3200)");   // DDR9 non esiste in MemoryGen
            }
        });
    }

    /** Legge le chiavi esterne di tutte le tabelle e tiene solo quelle che puntano a una tabella di lookup. */
    private static Map<String, String> foreignKeysToLookupTables() throws SQLException {
        Map<String, String> found = new TreeMap<>();
        try (Connection c = db.open(); Statement st = c.createStatement()) {
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = st.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'")) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
            for (String table : tables) {
                try (ResultSet rs = st.executeQuery("PRAGMA foreign_key_list(" + table + ")")) {
                    while (rs.next()) {
                        String parent = rs.getString("table");
                        if (LOOKUP_TABLES.contains(parent)) {
                            found.put(table + "." + rs.getString("from"), parent);
                        }
                    }
                }
            }
        }
        return found;
    }
}
