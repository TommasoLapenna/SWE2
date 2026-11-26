package main.dao.jdbc;

import db.DatabaseConnection;
import db.SqlUtil;
import main.domain.model.component.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Parte comune di tutti i DAO JDBC dei componenti (pattern Template Method).
 *
 * <p>Questa classe scrive le query e gestisce transazioni e errori. Ogni DAO concreto dice solo:
 * <ul>
 *   <li>nome della tabella, della colonna id e delle colonne specifiche (il costruttore);
 *       le colonne brand, model e price sono comuni e le aggiunge la classe base;</li>
 *   <li>come leggere una riga ({@link #map}) e come scrivere i parametri ({@link #bind});</li>
 *   <li>come copiare il record con un nuovo id ({@link #withId});</li>
 *   <li>se serve: controlli ({@link #validate}) e tabelle collegate
 *       ({@link #insertChildren}, {@link #deleteChildren}).</li>
 * </ul>
 */
public abstract class ComponentDao<T extends Component> {

    protected final DatabaseConnection database;
    private final String table;
    private final String idColumn;
    private final int columnCount;
    private final String selectSql;
    private final String insertSql;
    private final String updateSql;

    protected ComponentDao(DatabaseConnection database, String table, String idColumn,
                           String... specificColumns) {
        this.database = Objects.requireNonNull(database, "database");
        this.table = table;
        this.idColumn = idColumn;

        List<String> columns = new ArrayList<>(List.of("brand", "model", "price"));
        columns.addAll(List.of(specificColumns));
        this.columnCount = columns.size();

        String names = String.join(", ", columns);
        this.selectSql = "SELECT " + idColumn + ", " + names + " FROM " + table;
        this.insertSql = "INSERT INTO " + table + " (" + names + ") VALUES ("
                + "?, ".repeat(columnCount - 1) + "?)";
        this.updateSql = "UPDATE " + table + " SET "
                + columns.stream().map(c -> c + " = ?").collect(Collectors.joining(", "))
                + " WHERE " + idColumn + " = ?";
    }

    // ------------------------------------------------------------------ da implementare

    /** Trasforma la riga corrente in un record. Puo' leggere le tabelle collegate con la stessa connessione. */
    protected abstract T map(Connection connection, ResultSet row) throws SQLException;

    /** Imposta i parametri: prima {@link #bindCommon}, poi le colonne specifiche nell'ordine del costruttore. */
    protected abstract void bind(PreparedStatement ps, T component) throws SQLException;

    /** Copia del record con l'id assegnato dal database. */
    protected abstract T withId(T component, int id);

    // ------------------------------------------------------------------ facoltativi

    /** Controlli che il database non puo' fare (lanciare IllegalArgumentException). */
    protected void validate(T component) {
    }

    /** Scrive le tabelle collegate (insiemi). Di default non c'e' nulla da scrivere. */
    protected void insertChildren(Connection connection, int id, T component) throws SQLException {
    }

    /** Svuota le tabelle collegate di quel componente (usato dall'update). */
    protected void deleteChildren(Connection connection, int id) throws SQLException {
    }

    /** Imposta brand, model e price (parametri 1-3). Restituisce l'indice del prossimo parametro. */
    protected final int bindCommon(PreparedStatement ps, T component) throws SQLException {
        ps.setString(1, component.brand());
        ps.setString(2, component.model());
        ps.setDouble(3, component.price());
        return 4;
    }

    // ------------------------------------------------------------------ operazioni

    public Optional<T> findById(int id) {
        return database.read(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(selectSql + " WHERE " + idColumn + " = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(connection, rs)) : Optional.<T>empty();
                }
            }
        }, "Lettura da " + table + " (id " + id + ") fallita");
    }

    public List<T> findAll() {
        return database.read(connection -> {
            List<T> result = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(selectSql + " ORDER BY " + idColumn)) {
                while (rs.next()) {
                    result.add(map(connection, rs));
                }
            }
            return List.copyOf(result);
        }, "Lettura di " + table + " fallita");
    }

    public T insert(T component) {
        validate(component);
        return database.write(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                bind(ps, component);
                ps.executeUpdate();
            }
            int id = SqlUtil.lastInsertId(connection);
            insertChildren(connection, id, component);
            return withId(component, id);
        }, "Inserimento in " + table + " fallito: " + component.name());
    }

    public boolean update(T component) {
        validate(component);
        return database.write(connection -> {
            int rows;
            try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                bind(ps, component);
                ps.setInt(columnCount + 1, component.id());
                rows = ps.executeUpdate();
            }
            if (rows == 0) {
                return false;
            }
            deleteChildren(connection, component.id());
            insertChildren(connection, component.id(), component);
            return true;
        }, "Aggiornamento di " + table + " (id " + component.id() + ") fallito");
    }

    public boolean delete(int id) {
        // Le tabelle collegate si svuotano da sole (ON DELETE CASCADE); se il componente e' in una
        // build il database rifiuta la cancellazione (ON DELETE RESTRICT).
        return database.write(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM " + table + " WHERE " + idColumn + " = ?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        }, "Cancellazione da " + table + " (id " + id + ") fallita (forse e' usato in una build)");
    }
}
