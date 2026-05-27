package common.commands.dbCommands;

import common.MetaHashSet;
import common.commands.Command;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

public class RemoveGreater implements Command {
    public RemoveGreater() {}
    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route route = request.route();
        try {
            String ownerLogin = request.login();
            List<Route> toRemove = collection.stream()
                    .filter(r -> r.compareTo(route) > 0)
                    .filter(r -> r.getOwner().equals(ownerLogin))
                    .toList();

            if (toRemove.isEmpty()) {
                return new Response("Нет элементов для удаления", true);
            }
            int deleted = 0;
            List<String> errors = new ArrayList<>();

            for (Route route1 : toRemove) {
                try {
                    boolean success = dbCommands.deleteById(route1.getId(), ownerLogin);
                    if (success) {
                        collection.remove(route1);
                        deleted++;
                    }
                } catch (SQLException e) {
                    errors.add("id=" + route1.getId() + ": " + e.getMessage());
                }
            }

            String result = "Удалено элементов: " + deleted + "/" + toRemove.size();
            if (!errors.isEmpty()) result += "\nОшибки: " + String.join(", ", errors);

            return new Response(result, errors.isEmpty());
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
}
