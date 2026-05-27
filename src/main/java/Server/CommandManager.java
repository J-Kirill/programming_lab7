package Server;

import common.MetaHashSet;
import common.PasswordHasher;
import common.commands.Command;
import common.commands.dbCommands.DatabaseCommands;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Scanner;

public class CommandManager {
    public static Response handle(Request request) {
        try {
            if (!authenticate(request.login(), request.password())) {
                return new Response("Неверный логин или пароль", false);
            } else if (request.cArgs() == null && request.route() == null && authenticate(request.login(), request.password())) {
                return new Response("Всё верно", true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Command c = new Command() {
            @Override
            public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) throws Exception {
                return null;
            }
        };
        return c.doCommand(request, ServerMain.collection, ServerMain.scanner, ServerMain.history, DatabaseManager.getInstance());
    }

    public static boolean registerUser(String login, String password) throws SQLException {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, PasswordHasher.hash(password));

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) { // 23505 = unique violation
                return false; // логин уже занят
            }
            throw e;
        }
    }
    public static boolean authenticate(String login, String password) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE login = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return false;

            String storedHash = rs.getString("password_hash");
            return storedHash.equals(password);
        }
    }
}
