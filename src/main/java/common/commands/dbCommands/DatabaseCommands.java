package common.commands.dbCommands;

import common.stored.Route;

import java.sql.SQLException;

public interface DatabaseCommands {
    Long insert(Route route, String ownerLogin) throws SQLException;
    boolean updateById(long id, Route newRoute, String ownerLogin) throws SQLException;
    boolean deleteById(long id, String ownerLogin) throws SQLException;
}
