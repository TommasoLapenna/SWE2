package main.dao.jdbc;

import db.DatabaseConnection;
import main.domain.model.attribute.StorageTech;
import main.domain.model.attribute.StorageType;
import main.domain.model.component.Storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tabella: storage. Nota: nel record il campo si chiama CapacityGb (con la C maiuscola). */
public class StorageDao extends ComponentDao<Storage> {

    public StorageDao(DatabaseConnection database) {
        super(database, "storage", "id_storage", "storage_type", "storage_tech", "capacity_gb");
    }

    public StorageDao() {
        this(DatabaseConnection.getInstance());
    }

    @Override
    protected Storage map(Connection connection, ResultSet rs) throws SQLException {
        return new Storage(rs.getInt("id_storage"), rs.getString("brand"), rs.getString("model"), rs.getDouble("price"),
                StorageType.valueOf(rs.getString("storage_type")),
                StorageTech.valueOf(rs.getString("storage_tech")),
                rs.getInt("capacity_gb"));
    }

    @Override
    protected void bind(PreparedStatement ps, Storage storage) throws SQLException {
        int i = bindCommon(ps, storage);
        ps.setString(i++, storage.storageType().name());
        ps.setString(i++, storage.storageTech().name());
        ps.setInt(i, storage.CapacityGb());
    }

    @Override
    protected Storage withId(Storage s, int id) {
        return new Storage(id, s.brand(), s.model(), s.price(), s.storageType(), s.storageTech(), s.CapacityGb());
    }
}
