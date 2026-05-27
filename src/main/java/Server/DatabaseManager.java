package Server;

import common.commands.dbCommands.DatabaseCommands;
import common.InvalidData;
import common.MetaHashSet;
import common.stored.Coordinates;
import common.stored.Location;
import common.stored.Route;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseManager implements DatabaseCommands {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    private static DatabaseManager instance;
    public static DatabaseManager getInstance() throws IOException {
        if (instance == null) {
            Properties props = new Properties();
            props.load(new FileInputStream(".\\config.properties"));
            URL = props.getProperty("URL");
            USER = props.getProperty("USER");
            PASSWORD = props.getProperty("PASSWORD");
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public Long insert(Route route, String ownerLogin) throws SQLException{
        return insertRoute(route, ownerLogin);
    }
    public boolean updateById(long id, Route newRoute, String ownerLogin) throws SQLException{
        return updateRoute(id, newRoute, ownerLogin);
    }
    public boolean deleteById(long id, String ownerLogin) throws SQLException{
        return deleteRoute(id, ownerLogin);
    }
    @Override
    public int deleteAllByOwner(String ownerLogin) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            String selectSql = """
            SELECT coordinate_id, from_id, to_id
            FROM Routes WHERE owner_login = ?
        """;

            List<Long> coordIds = new ArrayList<>();
            List<Long> locationIds = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, ownerLogin);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    coordIds.add(rs.getLong("coordinate_id"));
                    locationIds.add(rs.getLong("from_id"));
                    Long toId = (Long) rs.getObject("to_id");
                    if (toId != null) locationIds.add(toId);
                }
            }

            int deleted;
            String deleteSql = "DELETE FROM Routes WHERE owner_login = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setString(1, ownerLogin);
                deleted = stmt.executeUpdate();
            }

            for (Long id : coordIds)    deleteIfUnused(conn, "coordinates", "coordinate_id", id);
            for (Long id : locationIds) deleteIfUnused(conn, "locations",   "location_id",   id);

            conn.commit();
            return deleted;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    public MetaHashSet<Route> loadAll() throws InvalidData {
        MetaHashSet<Route> collection = new MetaHashSet<>(Route.class);
        String sql = """
                SELECT
                    r.id AS id,
                    r.name AS name,
                    r.creation_date AS creation_date,
                    r.distance AS distance,
                    r.owner_login AS owner,
                
                    r.coordinate_id AS coordinate_id,
                    c.coordinate_x AS coordinate_x,
                    c.coordinate_y AS coordinate_y,
                    c.owner_login AS coordinate_owner,
                
                    r.from_id AS from_id,
                    lf.x    AS from_x,
                    lf.y    AS from_y,
                    lf.z    AS from_z,
                    lf.name AS from_name,
                    lf.owner_login AS from_owner,
                
                    r.to_id AS to_id,
                    lt.x    AS to_x,
                    lt.y    AS to_y,
                    lt.z    AS to_z,
                    lt.name AS to_name,
                    lt.owner_login AS to_owner
                
                FROM Routes r
                JOIN coordinates c  ON r.coordinate_id = c.coordinate_id
                JOIN locations   lf ON r.from_id       = lf.location_id
                LEFT JOIN locations lt ON r.to_id      = lt.location_id;
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                collection.add(Route.getFromSQL(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return collection;
    }

    public static Long insertRoute(Route route, String ownerLogin) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            Long coordinateId = insertOrGetCoordinates(conn, route.getCoordinates(), ownerLogin);
            Long fromId = insertOrGetLocation(conn, route.getFrom(), ownerLogin);
            Long toId = route.getTo() != null ? insertOrGetLocation(conn, route.getTo(), ownerLogin) : null;

            Long routeId = insertRoute(conn, route, coordinateId, fromId, toId, ownerLogin);

            conn.commit();
            return routeId;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    private static Long insertRoute(Connection conn, Route route,
                                    Long coordinateId, Long fromId, Long toId,
                                    String ownerLogin) throws SQLException {
        String sql = """
                    INSERT INTO Routes (name, coordinate_id, from_id, to_id, distance, owner_login)
                    VALUES (?, ?, ?, ?, ?, ?)
                    RETURNING id
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, route.getName());
            stmt.setLong(2, coordinateId);
            stmt.setLong(3, fromId);
            stmt.setObject(4, toId);
            stmt.setObject(5, route.getDistance());
            stmt.setString(6, ownerLogin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("id");
        }

        throw new SQLException("Не удалось вставить route");
    }

    private static Long insertOrGetCoordinates(Connection conn, Coordinates c, String ownerLogin) throws SQLException {
        String findSql = """
                    SELECT coordinate_id FROM coordinates
                    WHERE coordinate_x = ? 
                      AND (coordinate_y = ? OR (coordinate_y IS NULL AND ? IS NULL))
                      AND (owner_login = ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setDouble(1, c.getX());
            stmt.setObject(2, c.getY());
            stmt.setObject(3, c.getY());
            stmt.setString(4, ownerLogin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("coordinate_id");
            }
        }
        String insertSql = """
                    INSERT INTO coordinates (coordinate_x, coordinate_y, owner_login)
                    VALUES (?, ?, ?)
                    RETURNING coordinate_id
                """;

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setDouble(1, c.getX());
            stmt.setObject(2, c.getY());
            stmt.setString(3, ownerLogin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("coordinate_id");
        }

        throw new SQLException("Не удалось вставить coordinates");
    }

    private static Long insertOrGetLocation(Connection conn, Location loc, String ownerLogin) throws SQLException {
        String findSql = """
                    SELECT location_id FROM locations
                    WHERE name = ?
                      AND y = ?
                      AND z = ?
                      AND (x = ? OR (x IS NULL AND ? IS NULL))
                      AND owner_login = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setString(1, loc.getName());
            stmt.setFloat(2, loc.getY());
            stmt.setDouble(3, loc.getZ());
            stmt.setObject(4, loc.getX());
            stmt.setObject(5, loc.getX());
            stmt.setString(6, ownerLogin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("location_id"); // уже существует
            }
        }
        String insertSql = """
                    INSERT INTO locations (x, y, z, name, owner_login)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING location_id
                """;

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setObject(1, loc.getX());
            stmt.setFloat(2, loc.getY());
            stmt.setDouble(3, loc.getZ());
            stmt.setString(4, loc.getName());
            stmt.setString(5, ownerLogin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("location_id");
        }
        throw new SQLException("Не удалось вставить location");
    }

    public boolean deleteRoute(long id, String ownerLogin) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);


            Long coordinateId, fromId, toId;
            String selectSql = """
                        SELECT coordinate_id, from_id, to_id
                        FROM Routes
                        WHERE id = ? AND owner_login = ?
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setLong(1, id);
                stmt.setString(2, ownerLogin);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                coordinateId = rs.getLong("coordinate_id");
                fromId = rs.getLong("from_id");
                toId = (Long) rs.getObject("to_id");
            }

            String deleteRouteSql = "DELETE FROM Routes WHERE id = ? AND owner_login = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteRouteSql)) {
                stmt.setLong(1, id);
                stmt.setString(2, ownerLogin);
                stmt.executeUpdate();
            }

            deleteIfUnused(conn, "coordinates", "coordinate_id", coordinateId);
            deleteIfUnused(conn, "locations", "location_id", fromId);
            if (toId != null) {
                deleteIfUnused(conn, "locations", "location_id", toId);
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    private void deleteIfUnused(Connection conn, String table,
                                String idCol, Long id) throws SQLException {
        String checkCol = table.equals("coordinates") ? "coordinate_id" :
                idCol.equals("from_id") ? "from_id" : "to_id";

        String checkSql = "SELECT COUNT(*) FROM Routes WHERE " + checkCol + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return; // ещё используется
        }

        String deleteSql = "DELETE FROM " + table + " WHERE " + idCol + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public boolean updateRoute(long id, Route newRoute, String ownerLogin) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            String checkSql = """
                        SELECT coordinate_id, from_id, to_id
                        FROM Routes WHERE id = ? AND owner_login = ?
                    """;

            Long oldCoordId, oldFromId, oldToId;
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setLong(1, id);
                stmt.setString(2, ownerLogin);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                oldCoordId = rs.getLong("coordinate_id");
                oldFromId = rs.getLong("from_id");
                oldToId = (Long) rs.getObject("to_id");
            }

            Long newCoordId = insertOrGetCoordinates(conn, newRoute.getCoordinates(), ownerLogin);
            Long newFromId = insertOrGetLocation(conn, newRoute.getFrom(), ownerLogin);
            Long newToId = newRoute.getTo() != null ? insertOrGetLocation(conn, newRoute.getTo(), ownerLogin) : null;
            String updateSql = """
                        UPDATE Routes SET
                            name          = ?,
                            coordinate_id = ?,
                            from_id       = ?,
                            to_id         = ?,
                            distance      = ?
                        WHERE id = ? AND owner_login = ?
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, newRoute.getName());
                stmt.setLong(2, newCoordId);
                stmt.setLong(3, newFromId);
                stmt.setObject(4, newToId);
                stmt.setObject(5, newRoute.getDistance());
                stmt.setLong(6, newRoute.getId());
                stmt.setString(7, ownerLogin);
                stmt.executeUpdate();
            }

            if (!oldCoordId.equals(newCoordId))
                deleteIfUnused(conn, "coordinates", "coordinate_id", oldCoordId);
            if (!oldFromId.equals(newFromId))
                deleteIfUnused(conn, "locations", "location_id", oldFromId);
            if (oldToId != null && !oldToId.equals(newToId))
                deleteIfUnused(conn, "locations", "location_id", oldToId);

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }
}
