package dao;

import build.BuildView;
import db.DatabaseConnection;
import db.PersistenceException;
import db.SqlUtil;
import model.attribute.ComponentType;
import model.component.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Salvataggio delle build. Quello che si salva e' una {@link BuildView}, la vista di sola lettura
 * della build: {@code new BuildDao().save(session.getBuild())}.
 *
 * <p>Tabelle: build e build_component (id_build, id_component, quantity). Il salvataggio e' una
 * transazione: se un componente non esiste, non resta nemmeno la build. Per ricostruire i
 * componenti si usano i DAO dei singoli tipi.
 *
 * <p>Ogni componente deve gia' essere nel catalogo (id maggiore di 0). Una build puo' essere vuota o
 * incompleta, e puo' contenere piu' esemplari dello stesso componente: le regole di compatibilita'
 * e i limiti per tipo (per esempio una sola CPU) non sono controllati qui, ma dalle regole Java.
 * Chi vuole salvare solo build senza errori controlla prima {@code PcBuildSession.canCheckout()}.
 *
 * <p>Errori: {@link IllegalArgumentException} se il nome e' vuoto o un componente non e' nel catalogo
 * (id 0); {@link PersistenceException} se un componente con quell'id non esiste o il database fallisce.
 */
public class BuildDao {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private record Item(ComponentType type, int componentId, int quantity) {
    }

    private record BuildRow(String name, LocalDateTime createdAt, List<Item> items) {
    }

    private final DatabaseConnection database;
    private final Map<ComponentType, ComponentDao<? extends Component>> catalog = new EnumMap<>(ComponentType.class);

    public BuildDao(DatabaseConnection database) {
        this.database = Objects.requireNonNull(database, "database");
        catalog.put(ComponentType.CPU, new CpuDao(database));
        catalog.put(ComponentType.GPU, new GpuDao(database));
        catalog.put(ComponentType.RAM, new MemoryDao(database));
        catalog.put(ComponentType.STORAGE, new StorageDao(database));
        catalog.put(ComponentType.MOTHERBOARD, new MotherboardDao(database));
        catalog.put(ComponentType.POWER_SUPPLY, new PowerSupplyDao(database));
        catalog.put(ComponentType.CASE, new CaseDao(database));
        catalog.put(ComponentType.COOLING, new CoolerDao(database));
        catalog.put(ComponentType.PERIPHERAL, new PeripheralsDao(database));
    }

    public BuildDao() {
        this(DatabaseConnection.getInstance());
    }

    /** Tipi di componente che si sanno rileggere da una build (deve coincidere con tutto ComponentType). */
    Set<ComponentType> supportedTypes() {
        return Collections.unmodifiableSet(catalog.keySet());
    }

    // ------------------------------------------------------------------ operazioni

    /** Salva la build (con il suo nome) come nuova build: la build e tutti i suoi componenti, o niente. */
    public SavedBuild save(BuildView build) {
        String name = nameOf(build);
        Map<Integer, Integer> quantities = quantitiesOf(build);
        int id = database.write(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO build (name) VALUES (?)")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
            int buildId = SqlUtil.lastInsertId(connection);
            insertItems(connection, buildId, quantities);
            return buildId;
        }, "Salvataggio della build " + name + " fallito");
        return findById(id).orElseThrow(() -> new PersistenceException("Build appena salvata non trovata: " + id));
    }

    /** Sostituisce nome e componenti di una build gia' salvata. Restituisce false se non esiste. */
    public boolean update(int id, BuildView build) {
        String name = nameOf(build);
        Map<Integer, Integer> quantities = quantitiesOf(build);
        return database.write(connection -> {
            int rows;
            try (PreparedStatement ps = connection.prepareStatement("UPDATE build SET name = ? WHERE id_build = ?")) {
                ps.setString(1, name);
                ps.setInt(2, id);
                rows = ps.executeUpdate();
            }
            if (rows == 0) {
                return false;
            }
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM build_component WHERE id_build = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            insertItems(connection, id, quantities);
            return true;
        }, "Aggiornamento della build " + id + " fallito");
    }

    public Optional<SavedBuild> findById(int id) {
        Optional<BuildRow> row = database.read(connection -> readRow(connection, id),
                "Lettura della build " + id + " fallita");
        if (row.isEmpty()) {
            return Optional.empty();
        }
        Map<ComponentType, List<Component>> components = new EnumMap<>(ComponentType.class);
        for (Item item : row.get().items()) {
            Component component = catalog.get(item.type()).findById(item.componentId())
                    .orElseThrow(() -> new PersistenceException("Componente " + item.type() + " con id "
                            + item.componentId() + " mancante nel catalogo (build " + id + ")"));
            List<Component> sameType = components.computeIfAbsent(item.type(), t -> new ArrayList<>());
            for (int i = 0; i < item.quantity(); i++) {
                sameType.add(component);
            }
        }
        return Optional.of(new SavedBuild(id, row.get().name(), row.get().createdAt(), components));
    }

    /** Tutte le build salvate, ordinate per id (senza componenti). */
    public List<BuildSummary> findAll() {
        return database.read(connection -> {
            List<BuildSummary> result = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(
                         "SELECT id_build, name, created_at FROM build ORDER BY id_build")) {
                while (rs.next()) {
                    result.add(new BuildSummary(rs.getInt("id_build"), rs.getString("name"),
                            parse(rs.getString("created_at"))));
                }
            }
            return List.copyOf(result);
        }, "Lettura delle build fallita");
    }

    /** Cancella la build (i componenti restano nel catalogo). Restituisce false se non esiste. */
    public boolean delete(int id) {
        // le righe di build_component spariscono da sole (ON DELETE CASCADE)
        return database.write(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM build WHERE id_build = ?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        }, "Cancellazione della build " + id + " fallita");
    }

    // ------------------------------------------------------------------ dettagli

    private static String nameOf(BuildView build) {
        Objects.requireNonNull(build, "build");
        if (build.name() == null || build.name().isBlank()) {
            throw new IllegalArgumentException("Una build deve avere un nome");
        }
        return build.name().trim();
    }

    /** Per ogni id di componente, quanti esemplari ce ne sono nella build. */
    private static Map<Integer, Integer> quantitiesOf(BuildView build) {
        Map<Integer, Integer> quantities = new TreeMap<>();
        for (List<Component> sameType : build.components().values()) {
            for (Component component : sameType) {
                if (component.id() <= 0) {
                    throw new IllegalArgumentException("Il componente " + component.name()
                            + " non e' nel catalogo (id " + component.id() + "): salvarlo prima con il suo DAO");
                }
                quantities.merge(component.id(), 1, Integer::sum);
            }
        }
        return quantities;
    }

    private static void insertItems(Connection connection, int buildId, Map<Integer, Integer> quantities)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO build_component (id_build, id_component, quantity) VALUES (?, ?, ?)")) {
            for (Map.Entry<Integer, Integer> item : quantities.entrySet()) {
                ps.setInt(1, buildId);
                ps.setInt(2, item.getKey());
                ps.setInt(3, item.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static Optional<BuildRow> readRow(Connection connection, int id) throws SQLException {
        String name;
        LocalDateTime createdAt;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT name, created_at FROM build WHERE id_build = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                name = rs.getString("name");
                createdAt = parse(rs.getString("created_at"));
            }
        }
        List<Item> items = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT c.type, bc.id_component, bc.quantity FROM build_component bc "
                        + "JOIN component c ON c.id_component = bc.id_component "
                        + "WHERE bc.id_build = ? ORDER BY bc.id_component")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new Item(ComponentType.valueOf(rs.getString(1)), rs.getInt(2), rs.getInt(3)));
                }
            }
        }
        return Optional.of(new BuildRow(name, createdAt, items));
    }

    private static LocalDateTime parse(String timestamp) {
        return LocalDateTime.parse(timestamp, TIMESTAMP);
    }
}
