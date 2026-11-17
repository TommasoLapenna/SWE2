package dao;

import db.DatabaseConnection;
import model.attribute.ComponentType;
import model.attribute.EfficencyRating;
import model.attribute.PsuFormFactor;
import model.component.PowerSupply;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tabella: power_supply. */
public class PowerSupplyDao extends AbstractComponentDao<PowerSupply> {

    public PowerSupplyDao(DatabaseConnection database) {
        super(database, ComponentType.POWER_SUPPLY, "power_supply",
                "wattage", "efficiency_rating", "form_factor", "pcie_connectors");
    }

    public PowerSupplyDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected PowerSupply map(Connection connection, ResultSet rs) throws SQLException {
        return new PowerSupply(rs.getInt(ID), rs.getString("brand"), rs.getString("model"),
                rs.getDouble("price"), rs.getInt("wattage"),
                EfficencyRating.valueOf(rs.getString("efficiency_rating")),
                PsuFormFactor.valueOf(rs.getString("form_factor")),
                rs.getInt("pcie_connectors"));
    }

    @Override
    protected void bindDetails(PreparedStatement ps, int first, PowerSupply p) throws SQLException {
        int i = first;
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
