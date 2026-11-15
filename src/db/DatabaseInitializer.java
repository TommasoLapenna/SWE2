package db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Crea il database dagli script SQL se non esiste ancora.
 *
 * <p>Gli script (db/schema.sql e db/seed_lookups.sql) sono copiati nel classpath da Maven
 * (vedi la sezione resources del pom.xml). Chiamare {@link #ensureCreated} all'avvio
 * dell'applicazione: se il database c'e' gia' non fa nulla, quindi si puo' chiamare sempre.
 */
public final class DatabaseInitializer {

    private static final String[] SCRIPTS = {"/db/schema.sql", "/db/seed_lookups.sql"};

    private DatabaseInitializer() {
    }

    public static void ensureCreated(DatabaseConnection database) {
        try {
            Path parent = database.file().toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Connection connection = database.open()) {
                if (schemaExists(connection)) {
                    return;
                }
                connection.setAutoCommit(false);
                try (Statement statement = connection.createStatement()) {
                    for (String script : SCRIPTS) {
                        for (String sql : splitStatements(read(script))) {
                            statement.execute(sql);
                        }
                    }
                    connection.commit();
                } catch (SQLException | IOException | RuntimeException e) {
                    connection.rollback();
                    throw e;
                }
            }
        } catch (SQLException | IOException e) {
            throw new PersistenceException("Impossibile creare il database " + database.file(), e);
        }
    }

    private static boolean schemaExists(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'cpu'")) {
            return rs.next();
        }
    }

    private static String read(String resource) throws IOException {
        try (InputStream in = DatabaseInitializer.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Script non trovato nel classpath: " + resource
                        + " (il pom.xml deve copiare db/schema.sql e db/seed_lookups.sql tra le risorse)");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Divide uno script in istruzioni separate dal punto e virgola, ignorando i ';'
     * dentro ai commenti ("-- ...") e dentro alle stringhe ('...').
     */
    static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean inComment = false;
        for (int i = 0; i < script.length(); i++) {
            char ch = script.charAt(i);
            if (inComment) {
                if (ch == '\n') {
                    inComment = false;
                    current.append(ch);
                }
                continue;
            }
            if (inString) {
                current.append(ch);
                if (ch == '\'') {
                    inString = false;   // un apice raddoppiato ('') chiude e riapre subito: va bene
                }
                continue;
            }
            if (ch == '-' && i + 1 < script.length() && script.charAt(i + 1) == '-') {
                inComment = true;
                continue;
            }
            if (ch == '\'') {
                inString = true;
                current.append(ch);
                continue;
            }
            if (ch == ';') {
                addIfNotBlank(statements, current);
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        addIfNotBlank(statements, current);
        return statements;
    }

    private static void addIfNotBlank(List<String> statements, StringBuilder sql) {
        String text = sql.toString().trim();
        if (!text.isEmpty()) {
            statements.add(text);
        }
    }
}
