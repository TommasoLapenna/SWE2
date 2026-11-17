package main.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Lettura e scrittura delle tabelle collegate che rappresentano un insieme (Set) di un componente,
 * per esempio cpu_memory_gen(id_cpu, memory_gen). I nomi di tabella e colonna sono costanti
 * del codice dei DAO, mai input dell'utente.
 */
final class ChildRows {

    private ChildRows() {
    }

    static void insertEnums(Connection connection, String table, String idColumn, String valueColumn,
                            int id, Set<? extends Enum<?>> values) throws SQLException {
        if (values.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO " + table + " (" + idColumn + ", " + valueColumn + ") VALUES (?, ?)")) {
            for (Enum<?> value : values) {
                ps.setInt(1, id);
                ps.setString(2, value.name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    static <E extends Enum<E>> Set<E> loadEnums(Connection connection, String table, String idColumn,
                                                String valueColumn, int id, Class<E> type) throws SQLException {
        Set<E> result = EnumSet.noneOf(type);
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT " + valueColumn + " FROM " + table + " WHERE " + idColumn + " = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(Enum.valueOf(type, rs.getString(1)));
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    static void insertInts(Connection connection, String table, String idColumn, String valueColumn,
                           int id, Set<Integer> values) throws SQLException {
        if (values.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO " + table + " (" + idColumn + ", " + valueColumn + ") VALUES (?, ?)")) {
            for (int value : values) {
                ps.setInt(1, id);
                ps.setInt(2, value);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    static Set<Integer> loadInts(Connection connection, String table, String idColumn,
                                 String valueColumn, int id) throws SQLException {
        Set<Integer> result = new TreeSet<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT " + valueColumn + " FROM " + table + " WHERE " + idColumn + " = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    static void deleteAll(Connection connection, String table, String idColumn, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM " + table + " WHERE " + idColumn + " = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
