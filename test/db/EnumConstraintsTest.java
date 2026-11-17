package db;

import main.domain.model.attribute.Chipset;
import main.domain.model.attribute.ComponentType;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Gli enum Java non hanno tabelle di lookup: i valori ammessi sono scritti nei CHECK
 * "colonna IN (...)" di db/schema.sql. Questo test controlla che ogni CHECK elenchi ESATTAMENTE
 * i nomi dell'enum corrispondente, nello stesso ordine: chi aggiunge un valore a un enum e si
 * dimentica dello schema (o viceversa) lo scopre qui e non a runtime.
 */
class EnumConstraintsTest {

    /** CONSTRAINT nome CHECK (colonna IN ('A', 'B', ...)) */
    private static final Pattern COLUMN_CHECK = Pattern.compile(
            "CONSTRAINT\\s+(\\w+)\\s+CHECK\\s*\\(\\s*\\w+\\s+IN\\s*\\(([^)]*)\\)\\s*\\)");

    /** Dentro ck_component_support: attribute = 'X' AND value IN ('A', 'B', ...) */
    private static final Pattern SUPPORT_CHECK = Pattern.compile(
            "attribute\\s*=\\s*'(\\w+)'\\s+AND\\s+value\\s+IN\\s*\\(([^)]*)\\)");

    private static final Map<String, Class<? extends Enum<?>>> COLUMN_ENUMS = new LinkedHashMap<>();
    private static final Map<String, Class<? extends Enum<?>>> SUPPORT_ENUMS = new LinkedHashMap<>();

    static {
        COLUMN_ENUMS.put("ck_component_type", ComponentType.class);
        COLUMN_ENUMS.put("ck_cpu_socket", Socket.class);
        COLUMN_ENUMS.put("ck_memory_gen", MemoryGen.class);
        COLUMN_ENUMS.put("ck_storage_kind", StorageType.class);
        COLUMN_ENUMS.put("ck_storage_tech", StorageTech.class);
        COLUMN_ENUMS.put("ck_mobo_socket", Socket.class);
        COLUMN_ENUMS.put("ck_mobo_chipset", Chipset.class);
        COLUMN_ENUMS.put("ck_mobo_form_factor", FormFactor.class);
        COLUMN_ENUMS.put("ck_mobo_memory_gen", MemoryGen.class);
        COLUMN_ENUMS.put("ck_psu_efficiency", EfficencyRating.class);
        COLUMN_ENUMS.put("ck_psu_form_factor", PsuFormFactor.class);
        COLUMN_ENUMS.put("ck_cooler_kind", CoolerType.class);

        SUPPORT_ENUMS.put("MEMORY_GEN", MemoryGen.class);
        SUPPORT_ENUMS.put("FORM_FACTOR", FormFactor.class);
        SUPPORT_ENUMS.put("PSU_FORM_FACTOR", PsuFormFactor.class);
        SUPPORT_ENUMS.put("SOCKET", Socket.class);
    }

    private static String schema;

    @BeforeAll
    static void readSchema() throws IOException {
        try (InputStream in = EnumConstraintsTest.class.getResourceAsStream("/db/schema.sql")) {
            assertNotNull(in, "db/schema.sql non e' nel classpath");
            schema = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void columnChecksListExactlyTheEnumNames() {
        Map<String, List<String>> found = find(COLUMN_CHECK);

        COLUMN_ENUMS.forEach((constraint, enumClass) ->
                assertEquals(namesOf(enumClass), found.get(constraint), constraint + " <-> " + enumClass.getSimpleName()));
    }

    @Test
    void componentSupportCheckListsExactlyTheEnumNames() {
        Map<String, List<String>> found = find(SUPPORT_CHECK);

        SUPPORT_ENUMS.forEach((attribute, enumClass) ->
                assertEquals(namesOf(enumClass), found.get(attribute), attribute + " <-> " + enumClass.getSimpleName()));
    }

    /** Un nuovo CHECK su valori testuali va aggiunto alle mappe qui sopra: nessuno resta senza controllo. */
    @Test
    void everyTextListInTheSchemaIsCovered() {
        Map<String, List<String>> columns = find(COLUMN_CHECK);
        columns.values().removeIf(values -> values.stream().allMatch(v -> v.matches("\\d+")));   // booleani: IN (0, 1)

        assertEquals(COLUMN_ENUMS.keySet(), columns.keySet());
        assertEquals(SUPPORT_ENUMS.keySet(), find(SUPPORT_CHECK).keySet());
    }

    private static Map<String, List<String>> find(Pattern pattern) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        Matcher matcher = pattern.matcher(schema);
        while (matcher.find()) {
            List<String> values = Arrays.stream(matcher.group(2).split(","))
                    .map(v -> v.trim().replace("'", ""))
                    .toList();
            assertTrue(result.put(matcher.group(1), values) == null, "duplicato nello schema: " + matcher.group(1));
        }
        return result;
    }

    private static List<String> namesOf(Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toList();
    }
}
