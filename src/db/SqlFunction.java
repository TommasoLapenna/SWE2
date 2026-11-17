package db;

import java.sql.Connection;
import java.sql.SQLException;

/** Un blocco di lavoro su una connessione: puo' lanciare SQLException e restituire un risultato. */
@FunctionalInterface
public interface SqlFunction<T> {
    T apply(Connection connection) throws SQLException;
}
