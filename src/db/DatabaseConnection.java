package db;

import org.sqlite.SQLiteConfig;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * Unico punto di accesso al database SQLite.
 *
 * <p>Pattern Singleton per l'applicazione ({@link #getInstance()}); la factory
 * {@link #forFile(Path)} crea istanze su file diversi (usata nei test).
 *
 * <p>Ogni operazione apre una connessione nuova e la chiude alla fine: con SQLite locale
 * costa poco e evita problemi di thread (JavaFX usa thread diversi). Le foreign key sono
 * attivate SEMPRE: in SQLite sono spente di default e vanno abilitate su ogni connessione.
 *
 * <p>I DAO non gestiscono connessioni a mano: usano {@link #read} per le letture e
 * {@link #write} per le scritture (transazione: tutto o niente).
 */
public final class DatabaseConnection {

    /** File del database, relativo alla cartella da cui parte l'app (la radice del progetto). */
    public static final String DEFAULT_FILE = "db/pcconfigurator.db";

    private static DatabaseConnection instance;

    private final Path file;
    private final String url;
    private final Properties properties;

    private DatabaseConnection(Path file) {
        this.file = Objects.requireNonNull(file, "file");
        this.url = "jdbc:sqlite:" + file;
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        this.properties = config.toProperties();
    }

    /** Istanza condivisa dell'applicazione, sul file {@link #DEFAULT_FILE}. */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection(Path.of(DEFAULT_FILE));
        }
        return instance;
    }

    /** Istanza su un file specifico (test, database alternativi). */
    public static DatabaseConnection forFile(Path file) {
        return new DatabaseConnection(file);
    }

    public Path file() {
        return file;
    }

    /** Apre una connessione con le foreign key attive. Chi la apre la deve chiudere. */
    public Connection open() throws SQLException {
        return DriverManager.getConnection(url, properties);
    }

    /** Esegue una lettura su una connessione che viene chiusa al termine. */
    public <T> T read(SqlFunction<T> work, String errorMessage) {
        try (Connection connection = open()) {
            return work.apply(connection);
        } catch (SQLException e) {
            throw new PersistenceException(errorMessage, e);
        }
    }

    /**
     * Esegue una scrittura in una transazione: se il lavoro termina bene si fa commit,
     * se lancia un'eccezione si fa rollback e non resta nulla a meta'.
     * Le eccezioni non SQL (per esempio IllegalArgumentException) passano invariate.
     */
    public <T> T write(SqlFunction<T> work, String errorMessage) {
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try {
                T result = work.apply(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException(errorMessage, e);
        }
    }
}
