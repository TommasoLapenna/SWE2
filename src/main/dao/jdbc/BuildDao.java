package main.dao.jdbc;

import main.dao.BuildSummary;
import main.dao.SavedBuild;
import db.DatabaseConnection;
import db.PersistenceException;
import db.SqlUtil;
import main.domain.model.attribute.ComponentType;
import main.domain.model.component.Component;

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
 * Tabelle: build e una tabella ponte per ogni tipo di componente (build_cpu, build_gpu, ...),
 * ciascuna con (id_build, id del componente, quantity).
 *
 * <p>Il salvataggio e' una transazione: se un componente non esiste, non resta nemmeno la build.
 * Per ricostruire i componenti si usano i DAO dei singoli tipi.
 *
 * @throws IllegalArgumentException se il nome e' vuoto o un componente non e' nel catalogo (id 0)
 * @throws db.PersistenceException  se un componente con quell'id non esiste o il database fallisce
 */
public class BuildDao {

    private record Link(String table, String idColumn) {
    }

    /** Per ogni tipo di componente: tabella ponte delle build e colonna verso il componente. */
    private static final Map<ComponentType, Link> LINKS = new EnumMap<>(ComponentType.class);

    static {
        LINKS.put(ComponentType.CPU, new Link("build_cpu", "id_cpu"));
        LINKS.put(ComponentType.GPU, new Link("build_gpu", "id_gpu"));
        LINKS.put(ComponentType.RAM, new Link("build_memory", "id_memory"));
        LINKS.put(ComponentType.STORAGE, new Link("build_storage", "id_storage"));
        LINKS.put(ComponentType.MOTHERBOARD, new Link("build_motherboard", "id_motherboard"));
        LINKS.put(ComponentType.POWER_SUPPLY, new Link("build_power_supply", "id_power_supply"));
        LINKS.put(ComponentType.CASE, new Link("build_pc_case", "id_pc_case"));
        LINKS.put(ComponentType.COOLING, new Link("build_cooler", "id_cooler"));
        LINKS.put(ComponentType.PERIPHERAL, new Link("build_peripheral", "id_peripheral"));
    }

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private record BuildRow(String name, LocalDateTime createdAt, Map<ComponentType, Map<Integer, Integer>> items) {
    }

    private final DatabaseConnection database;
    private final Map<ComponentType, ComponentDao<? extends Component>> daos = new EnumMap<>(ComponentType.class);

    public BuildDao(DatabaseConnection database) {
        this.database = Objects.requireNonNull(database, "database");
        daos.put(ComponentType.CPU, new CpuDao(database));
        daos.put(ComponentType.GPU, new GpuDao(database));
        daos.put(ComponentType.RAM, new MemoryDao(database));
        daos.put(ComponentType.STORAGE, new StorageDao(database));
        daos.put(ComponentType.MOTHERBOARD, new MotherboardDao(database));
        daos.put(ComponentType.POWER_SUPPLY, new PowerSupplyDao(database));
        daos.put(ComponentType.CASE, new CaseDao(database));
        daos.put(ComponentType.COOLING, new CoolerDao(database));
        daos.put(ComponentType.PERIPHERAL, new PeripheralsDao(database));
    }

    public BuildDao() {
        this(DatabaseConnection.getInstance());
    }

    /** Tipi di componente che si possono salvare in una build (deve coincidere con tutto ComponentType). */
    public static Set<ComponentType> supportedTypes() {
        return Collections.unmodifiableSet(LINKS.keySet());
    }

    // ------------------------------------------------------------------ operazioni

    public SavedBuild save(String name, List<? extends Component> components) {
        validate(name, components);
        int id = database.write(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO build (nome) VALUES (?)")) {
                ps.setString(1, name.trim());
                ps.executeUpdate();
            }
            int buildId = SqlUtil.lastInsertId(connection);
            insertItems(connection, buildId, components);
            return buildId;
        }, "Salvataggio della build " + name + " fallito");
        return findById(id).orElseThrow(() -> new PersistenceException("Build appena salvata non trovata: " + id));
    }

    public boolean update(int id, String name, List<? extends Component> components) {
        validate(name, components);
        return database.write(connection -> {
            int rows;
            try (PreparedStatement ps = connection.prepareStatement("UPDATE build SET nome = ? WHERE id_build = ?")) {
                ps.setString(1, name.trim());
                ps.setInt(2, id);
                rows = ps.executeUpdate();
            }
            if (rows == 0) {
                return false;
            }
            for (Link link : LINKS.values()) {
                ChildRows.deleteAll(connection, link.table(), "id_build", id);
            }
            insertItems(connection, id, components);
            return true;
        }, "Aggiornamento della build " + id + " fallito");
    }

    public Optional<SavedBuild> findById(int id) {
        Optional<BuildRow> row = database.read(connection -> readRow(connection, id),
                "Lettura della build " + id + " fallita");
        if (row.isEmpty()) {
            return Optional.empty();
        }
        List<Component> components = new ArrayList<>();
        for (Map.Entry<ComponentType, Map<Integer, Integer>> byType : row.get().items().entrySet()) {
            ComponentDao<? extends Component> dao = daos.get(byType.getKey());
            for (Map.Entry<Integer, Integer> item : byType.getValue().entrySet()) {
                Component component = dao.findById(item.getKey()).orElseThrow(() -> new PersistenceException(
                        "Componente " + byType.getKey() + " con id " + item.getKey()
                                + " mancante nel catalogo (build " + id + ")"));
                for (int i = 0; i < item.getValue(); i++) {
                    components.add(component);
                }
            }
        }
        return Optional.of(new SavedBuild(id, row.get().name(), row.get().createdAt(), components));
    }

    public List<BuildSummary> findAll() {
        return database.read(connection -> {
            List<BuildSummary> result = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(
                         "SELECT id_build, nome, data_creazione FROM build ORDER BY id_build")) {
                while (rs.next()) {
                    result.add(new BuildSummary(rs.getInt("id_build"), rs.getString("nome"),
                            parse(rs.getString("data_creazione"))));
                }
            }
            return List.copyOf(result);
        }, "Lettura delle build fallita");
    }

    public boolean delete(int id) {
        // le righe dei ponti spariscono da sole (ON DELETE CASCADE); i componenti restano
        return database.write(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM build WHERE id_build = ?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        }, "Cancellazione della build " + id + " fallita");
    }

    // ------------------------------------------------------------------ dettagli

    private static void validate(String name, List<? extends Component> components) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Una build deve avere un nome");
        }
        Objects.requireNonNull(components, "components");
        for (Component component : components) {
            if (component.id() <= 0) {
                throw new IllegalArgumentException("Il componente " + component.name()
                        + " non e' nel catalogo (id " + component.id() + "): salvarlo prima con il suo DAO");
            }
        }
    }

    /** Un elemento per ogni esemplare -> per tipo, per id, la quantita'. */
    private static Map<ComponentType, Map<Integer, Integer>> countByType(List<? extends Component> components) {
        Map<ComponentType, Map<Integer, Integer>> result = new EnumMap<>(ComponentType.class);
        for (Component component : components) {
            result.computeIfAbsent(component.type(), t -> new TreeMap<>()).merge(component.id(), 1, Integer::sum);
        }
        return result;
    }

    private static void insertItems(Connection connection, int buildId, List<? extends Component> components)
            throws SQLException {
        for (Map.Entry<ComponentType, Map<Integer, Integer>> byType : countByType(components).entrySet()) {
            Link link = LINKS.get(byType.getKey());
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + link.table()
                    + " (id_build, " + link.idColumn() + ", quantity) VALUES (?, ?, ?)")) {
                for (Map.Entry<Integer, Integer> item : byType.getValue().entrySet()) {
                    ps.setInt(1, buildId);
                    ps.setInt(2, item.getKey());
                    ps.setInt(3, item.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private static Optional<BuildRow> readRow(Connection connection, int id) throws SQLException {
        String name;
        LocalDateTime createdAt;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT nome, data_creazione FROM build WHERE id_build = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                name = rs.getString("nome");
                createdAt = parse(rs.getString("data_creazione"));
            }
        }
        Map<ComponentType, Map<Integer, Integer>> items = new EnumMap<>(ComponentType.class);
        for (Map.Entry<ComponentType, Link> entry : LINKS.entrySet()) {
            Link link = entry.getValue();
            try (PreparedStatement ps = connection.prepareStatement("SELECT " + link.idColumn()
                    + ", quantity FROM " + link.table() + " WHERE id_build = ? ORDER BY " + link.idColumn())) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.computeIfAbsent(entry.getKey(), t -> new TreeMap<>())
                                .put(rs.getInt(1), rs.getInt(2));
                    }
                }
            }
        }
        return Optional.of(new BuildRow(name, createdAt, items));
    }

    private static LocalDateTime parse(String timestamp) {
        return LocalDateTime.parse(timestamp, TIMESTAMP);
    }
}
