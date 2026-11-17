package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Piccole funzioni JDBC condivise dai DAO. */
public final class SqlUtil {

    private SqlUtil() {
    }

    /**
     * Id dell'ultima riga inserita su QUESTA connessione (da chiamare subito dopo l'INSERT).
     * Si usa al posto di getGeneratedKeys(), che il driver SQLite non supporta in tutte le versioni.
     */
    public static int lastInsertId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (!rs.next()) {
                throw new SQLException("Il database non ha restituito l'id della riga inserita");
            }
            return rs.getInt(1);
        }
    }
}
