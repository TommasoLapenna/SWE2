package dao;

import db.DatabaseConnection;
import model.attribute.ComponentType;
import model.attribute.StorageTech;
import model.attribute.StorageType;
import model.component.Storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tabella: storage. Nota: nel record il campo si chiama CapacityGb (con la C maiuscola). */
public class StorageDao extends AbstractComponentDao<Storage> {

    public StorageDao(DatabaseConnection database) {
        super(database, ComponentType.STORAGE, "storage", "storage_type", "storage_tech", "capacity_gb");
    }

    public StorageDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Storage map(Connection connection, ResultSet rs) throws SQLException {
        return new Storage(rs.getInt(ID), rs.getString("brand"), rs.getString("model"), rs.getDouble("price"),
                StorageType.valueOf(rs.getString("storage_type")),
                StorageTech.valueOf(rs.getString("storage_tech")),
                rs.getInt("capacity_gb"));
    }

    @Override
    protected void bindDetails(PreparedStatement ps, int first, Storage storage) throws SQLException {
        int i = first;
        ps.setString(i++, storage.storageType().name());
        ps.setString(i++, storage.storageTech().name());
        ps.setInt(i, storage.CapacityGb());
    }

    @Override
    protected Storage withId(Storage s, int id) {
        return new Storage(id, s.brand(), s.model(), s.price(), s.storageType(), s.storageTech(), s.CapacityGb());
    }
}
