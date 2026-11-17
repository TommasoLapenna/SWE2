package main.dao.jdbc;

import db.DatabaseConnection;
import main.domain.model.component.Peripherals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tabella: peripheral (il record si chiama Peripherals, la tabella peripheral). */
public class PeripheralsDao extends ComponentDao<Peripherals> {

    public PeripheralsDao(DatabaseConnection database) {
        super(database, "peripheral", "id_peripheral", "power_draw_watts");
    }

    public PeripheralsDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Peripherals map(Connection connection, ResultSet rs) throws SQLException {
        return new Peripherals(rs.getInt("id_peripheral"), rs.getString("brand"), rs.getString("model"),
                rs.getDouble("price"), rs.getInt("power_draw_watts"));
    }

    @Override
    protected void bind(PreparedStatement ps, Peripherals p) throws SQLException {
        int i = bindCommon(ps, p);
        ps.setInt(i, p.powerDrawWatts());
    }

    @Override
    protected Peripherals withId(Peripherals p, int id) {
        return new Peripherals(id, p.brand(), p.model(), p.price(), p.powerDrawWatts());
    }
}
