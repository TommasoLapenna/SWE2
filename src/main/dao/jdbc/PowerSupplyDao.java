package main.dao.jdbc;

import db.DatabaseConnection;
import main.domain.model.attribute.EfficencyRating;
import main.domain.model.attribute.PsuFormFactor;
import main.domain.model.component.PowerSupply;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tabella: power_supply. */
public class PowerSupplyDao extends ComponentDao<PowerSupply> {

    public PowerSupplyDao(DatabaseConnection database) {
        super(database, "power_supply", "id_power_supply",
                "wattage", "efficiency_rating", "form_factor", "pcie_connectors");
    }

    public PowerSupplyDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected PowerSupply map(Connection connection, ResultSet rs) throws SQLException {
        return new PowerSupply(rs.getInt("id_power_supply"), rs.getString("brand"), rs.getString("model"),
                rs.getDouble("price"), rs.getInt("wattage"),
                EfficencyRating.valueOf(rs.getString("efficiency_rating")),
                PsuFormFactor.valueOf(rs.getString("form_factor")),
                rs.getInt("pcie_connectors"));
    }

    @Override
    protected void bind(PreparedStatement ps, PowerSupply p) throws SQLException {
        int i = bindCommon(ps, p);
        ps.setInt(i++, p.wattage());
        ps.setString(i++, p.efficencyRating().name());
        ps.setString(i++, p.formFactor().name());
        ps.setInt(i, p.pcieConnectors());
    }

    @Override
    protected PowerSupply withId(PowerSupply p, int id) {
        return new PowerSupply(id, p.brand(), p.model(), p.price(), p.wattage(),
                p.efficencyRating(), p.formFactor(), p.pcieConnectors());
    }
}
