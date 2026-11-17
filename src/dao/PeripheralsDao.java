package dao;

import db.DatabaseConnection;
import model.attribute.ComponentType;
import model.component.Peripherals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tabella: peripheral (il record si chiama Peripherals, la tabella peripheral). */
public class PeripheralsDao extends AbstractComponentDao<Peripherals> {

    public PeripheralsDao(DatabaseConnection database) {
        super(database, ComponentType.PERIPHERAL, "peripheral", "power_draw_watts");
    }

    public PeripheralsDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Peripherals map(Connection connection, ResultSet rs) throws SQLException {
        return new Peripherals(rs.getInt(ID), rs.getString("brand"), rs.getString("model"),
                rs.getDouble("price"), rs.getInt("power_draw_watts"));
    }

    @Override
    protected void bindDetails(PreparedStatement ps, int first, Peripherals p) throws SQLException {
        ps.setInt(first, p.powerDrawWatts());
    }

    @Override
    protected Peripherals withId(Peripherals p, int id) {
        return new Peripherals(id, p.brand(), p.model(), p.price(), p.powerDrawWatts());
    }
}
