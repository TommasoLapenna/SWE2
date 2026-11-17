package dao;

import db.DatabaseConnection;
import db.SqlUtil;
import model.attribute.ComponentType;
import model.component.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Parte comune di tutti i DAO dei componenti (pattern Template Method).
 *
 * <p>Nel database un componente e' una riga di {@code component} (id, tipo, brand, model, price)
 * piu' una riga della tabella del suo tipo (cpu, gpu, ...) con le colonne specifiche; gli insiemi
 * (Set) stanno in {@code component_support}. Questa classe scrive le query e gestisce transazioni
 * e errori. Ogni DAO concreto dice solo:
 * <ul>
 *   <li>tipo di componente, tabella e colonne specifiche (il costruttore);</li>
 *   <li>come leggere una riga ({@link #map}) e come scrivere le colonne specifiche ({@link #bindDetails});</li>
 *   <li>come copiare il record con un nuovo id ({@link #withId});</li>
 *   <li>se serve: controlli ({@link #validate}) e insiemi ({@link #insertSupports}).</li>
 * </ul>
 */
public abstract class AbstractComponentDao<T extends Component> implements ComponentDao<T> {

    /** Colonna id, uguale in component e in tutte le tabelle dei tipi. */
    protected static final String ID = "id_component";

    protected final DatabaseConnection database;
    private final ComponentType type;
    private final String table;
    private final int detailCount;
    private final String selectSql;
    private final String insertDetailSql;
    private final String updateDetailSql;

    protected AbstractComponentDao(DatabaseConnection database, ComponentType type, String table,
                                   String... detailColumns) {
        this.database = Objects.requireNonNull(database, "database");
        this.type = type;
        this.table = table;
        this.detailCount = detailColumns.length;

        List<String> columns = Arrays.asList(detailColumns);
        this.selectSql = "SELECT c.id_component, c.brand, c.model, c.price, "
                + columns.stream().map(c -> "d." + c).collect(Collectors.joining(", "))
                + " FROM component c JOIN " + table + " d ON d.id_component = c.id_component";
        this.insertDetailSql = "INSERT INTO " + table + " (id_component, " + String.join(", ", columns)
                + ") VALUES (?" + ", ?".repeat(detailCount) + ")";
        this.updateDetailSql = "UPDATE " + table + " SET "
                + columns.stream().map(c -> c + " = ?").collect(Collectors.joining(", "))
                + " WHERE id_component = ?";
    }

    // ------------------------------------------------------------------ da implementare

    /** Trasforma la riga corrente in un record. Puo' leggere gli insiemi con la stessa connessione. */
    protected abstract T map(Connection connection, ResultSet row) throws SQLException;

    /** Imposta le colonne specifiche, nell'ordine del costruttore, a partire dal parametro {@code first}. */
    protected abstract void bindDetails(PreparedStatement ps, int first, T component) throws SQLException;

    /** Copia del record con l'id assegnato dal database. */
    protected abstract T withId(T component, int id);

    // ------------------------------------------------------------------ facoltativi

    /** Controlli che il database non puo' fare (lanciare IllegalArgumentException). */
    protected void validate(T component) {
    }

    /** Scrive gli insiemi del componente con {@link #insertSupport}. Di default non c'e' nulla da scrivere. */
    protected void insertSupports(Connection connection, int id, T component) throws SQLException {
    }

    // ------------------------------------------------------------------ insiemi (component_support)

    /** Una riga per ogni valore dell'insieme: il nome per gli enum, il numero per gli interi. */
    protected final void insertSupport(Connection connection, int id, String attribute, Collection<?> values)
            throws SQLException {
        if (values.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO component_support (id_component, type, attribute, value) VALUES (?, ?, ?, ?)")) {
            for (Object value : values) {
                ps.setInt(1, id);
                ps.setString(2, type.name());
                ps.setString(3, attribute);
                ps.setString(4, value instanceof Enum<?> e ? e.name() : value.toString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    protected final <E extends Enum<E>> Set<E> loadEnumSupport(Connection connection, int id, String attribute,
                                                               Class<E> enumClass) throws SQLException {
        Set<E> result = EnumSet.noneOf(enumClass);
        for (String value : loadSupport(connection, id, attribute)) {
            result.add(Enum.valueOf(enumClass, value));
        }
        return Collections.unmodifiableSet(result);
    }

    protected final Set<Integer> loadIntSupport(Connection connection, int id, String attribute) throws SQLException {
        Set<Integer> result = new TreeSet<>();
        for (String value : loadSupport(connection, id, attribute)) {
            result.add(Integer.valueOf(value));
        }
        return Collections.unmodifiableSet(result);
    }

    private static List<String> loadSupport(Connection connection, int id, String attribute) throws SQLException {
        List<String> values = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT value FROM component_support WHERE id_component = ? AND attribute = ?")) {
            ps.setInt(1, id);
            ps.setString(2, attribute);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    values.add(rs.getString(1));
                }
            }
        }
        return values;
    }

    // ------------------------------------------------------------------ operazioni

    @Override
    public Optional<T> findById(int id) {
        return database.read(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(selectSql + " WHERE c.id_component = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(connection, rs)) : Optional.<T>empty();
                }
            }
        }, "Lettura da " + table + " (id " + id + ") fallita");
    }

    @Override
    public List<T> findAll() {
        return database.read(connection -> {
            List<T> result = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(selectSql + " ORDER BY c.id_component")) {
                while (rs.next()) {
                    result.add(map(connection, rs));
                }
            }
            return List.copyOf(result);
        }, "Lettura di " + table + " fallita");
    }

    @Override
    public T insert(T component) {
        validate(component);
        return database.write(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO component (type, brand, model, price) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, type.name());
                ps.setString(2, component.brand());
                ps.setString(3, component.model());
                ps.setDouble(4, component.price());
                ps.executeUpdate();
            }
            int id = SqlUtil.lastInsertId(connection);
            try (PreparedStatement ps = connection.prepareStatement(insertDetailSql)) {
                ps.setInt(1, id);
                bindDetails(ps, 2, component);
                ps.executeUpdate();
            }
            insertSupports(connection, id, component);
            return withId(component, id);
        }, "Inserimento in " + table + " fallito: " + component.name());
    }

    @Override
    public boolean update(T component) {
        validate(component);
        int id = component.id();
        return database.write(connection -> {
            int rows;
            // "AND type = ?": un DAO non tocca i componenti di un altro tipo
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE component SET brand = ?, model = ?, price = ? WHERE id_component = ? AND type = ?")) {
                ps.setString(1, component.brand());
                ps.setString(2, component.model());
                ps.setDouble(3, component.price());
                ps.setInt(4, id);
                ps.setString(5, type.name());
                rows = ps.executeUpdate();
            }
            if (rows == 0) {
                return false;
            }
            try (PreparedStatement ps = connection.prepareStatement(updateDetailSql)) {
                bindDetails(ps, 1, component);
                ps.setInt(detailCount + 1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM component_support WHERE id_component = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            insertSupports(connection, id, component);
            return true;
        }, "Aggiornamento di " + table + " (id " + id + ") fallito");
    }

    @Override
    public boolean delete(int id) {
        // La riga del tipo e gli insiemi spariscono da soli (ON DELETE CASCADE); se il componente
        // e' in una build il database rifiuta la cancellazione (ON DELETE RESTRICT).
        return database.write(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM component WHERE id_component = ? AND type = ?")) {
                ps.setInt(1, id);
                ps.setString(2, type.name());
                return ps.executeUpdate() > 0;
            }
        }, "Cancellazione da " + table + " (id " + id + ") fallita (forse e' usato in una build)");
    }
}
