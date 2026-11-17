package db;

import main.domain.model.attribute.Chipset;
import main.domain.model.attribute.CoolerType;
import main.domain.model.attribute.EfficencyRating;
import main.domain.model.attribute.FormFactor;
import main.domain.model.attribute.MemoryGen;
import main.domain.model.attribute.PsuFormFactor;
import main.domain.model.attribute.Socket;
import main.domain.model.attribute.StorageTech;
import main.domain.model.attribute.StorageType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Controlla che i valori degli enum Java coincidano con le righe delle tabelle lookup.
 * Se qualcuno aggiunge, toglie o rinomina un valore di un enum (o corregge un refuso)
 * senza aggiornare db/seed_lookups.sql, questo test fallisce e dice quale enum e' fuori allineamento.
 */
class LookupTablesTest {

    @TempDir
    static Path tempDir;

    static DatabaseConnection db;

    @BeforeAll
    static void createDatabase() {
        db = DatabaseConnection.forFile(tempDir.resolve("lookup.db"));
        DatabaseInitializer.ensureCreated(db);
    }

    @Test void socket()           throws SQLException { assertAligned("socket", Socket.class); }
    @Test void memoryGen()        throws SQLException { assertAligned("memory_gen", MemoryGen.class); }
    @Test void formFactor()       throws SQLException { assertAligned("form_factor", FormFactor.class); }
    @Test void psuFormFactor()    throws SQLException { assertAligned("psu_form_factor", PsuFormFactor.class); }
    @Test void storageTech()      throws SQLException { assertAligned("storage_tech", StorageTech.class); }
    @Test void storageType()      throws SQLException { assertAligned("storage_type", StorageType.class); }
    @Test void coolerType()       throws SQLException { assertAligned("cooler_type", CoolerType.class); }
    @Test void efficiencyRating() throws SQLException { assertAligned("efficiency_rating", EfficencyRating.class); }
    @Test void chipset()          throws SQLException { assertAligned("chipset", Chipset.class); }

    private static <E extends Enum<E>> void assertAligned(String table, Class<E> enumClass) throws SQLException {
        Set<String> inJava = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> inDatabase = new TreeSet<>();
        try (Connection c = db.open(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM " + table)) {
            while (rs.next()) {
                inDatabase.add(rs.getString(1));
            }
        }
        assertEquals(inJava, inDatabase,
                "L'enum " + enumClass.getSimpleName() + " e la tabella " + table
                        + " non coincidono: aggiorna l'enum o db/seed_lookups.sql");
    }
}
