package dao;

import db.DatabaseConnection;
import model.attribute.ComponentType;
import model.attribute.CoolerType;
import model.attribute.Socket;
import model.component.Cooler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

/**
 * Tabella: cooler. I socket supportati stanno in component_support (SOCKET).
 *
 * <p>Nel record heightMm (solo AIR) e radiatorSizeMm (solo LIQUID) sono int, quindi la dimensione
 * non applicabile vale 0. Nel database invece e' NULL: il DAO scrive NULL al posto dello 0 e
 * rilegge NULL come 0. Il database pretende questa coerenza con un CHECK; qui la si controlla prima,
 * cosi' il messaggio d'errore e' chiaro.
 */
public class CoolerDao extends AbstractComponentDao<Cooler> {

    private static final String SOCKETS = "SOCKET";

    /** Il consumo del cooler e' sempre 5 W: Cooler.powerDrawWatts() ignora il valore del costruttore. */
    private static final int POWER_DRAW_WATTS = 5;

    public CoolerDao(DatabaseConnection database) {
        super(database, ComponentType.COOLING, "cooler",
                "cooler_type", "max_tdp_watts", "height_mm", "radiator_size_mm");
    }

    public CoolerDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Cooler map(Connection connection, ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        double price = rs.getDouble("price");
        CoolerType type = CoolerType.valueOf(rs.getString("cooler_type"));
        int maxTdp = rs.getInt("max_tdp_watts");
        int heightMm = rs.getInt("height_mm");             // NULL -> 0
        int radiatorSizeMm = rs.getInt("radiator_size_mm"); // NULL -> 0
        Set<Socket> sockets = loadEnumSupport(connection, id, SOCKETS, Socket.class);
        return new Cooler(id, brand, model, price, POWER_DRAW_WATTS, type, sockets, maxTdp, heightMm, radiatorSizeMm);
    }

    @Override
    protected void bindDetails(PreparedStatement ps, int first, Cooler c) throws SQLException {
        int i = first;
        ps.setString(i++, c.coolingType().name());
        ps.setInt(i++, c.maxTdp());
        if (c.coolingType() == CoolerType.AIR) {
            ps.setInt(i++, c.heightMm());
            ps.setNull(i, Types.INTEGER);
        } else {
            ps.setNull(i++, Types.INTEGER);
            ps.setInt(i, c.radiatorSizeMm());
        }
    }

    @Override
    protected Cooler withId(Cooler c, int id) {
        return new Cooler(id, c.brand(), c.model(), c.price(), c.powerDrawWatts(), c.coolingType(),
                c.supportedSocktes(), c.maxTdp(), c.heightMm(), c.radiatorSizeMm());
    }

    @Override
    protected void validate(Cooler c) {
        if (c.coolingType() == CoolerType.AIR) {
            if (c.heightMm() <= 0 || c.radiatorSizeMm() != 0) {
                throw new IllegalArgumentException(
                        "Un cooler AIR deve avere heightMm > 0 e radiatorSizeMm = 0: " + c.name());
            }
        } else if (c.coolingType() == CoolerType.LIQUID) {
            if (c.radiatorSizeMm() <= 0 || c.heightMm() != 0) {
                throw new IllegalArgumentException(
                        "Un cooler LIQUID deve avere radiatorSizeMm > 0 e heightMm = 0: " + c.name());
            }
        }
        if (c.supportedSocktes() == null || c.supportedSocktes().isEmpty()) {
            throw new IllegalArgumentException("Un cooler deve supportare almeno un socket: " + c.name());
        }
    }

    @Override
    protected void insertSupports(Connection connection, int id, Cooler c) throws SQLException {
        insertSupport(connection, id, SOCKETS, c.supportedSocktes());
    }
}
