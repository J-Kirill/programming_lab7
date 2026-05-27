package common.commands.dbCommands;

import common.commands.Command;
import common.MetaHashSet;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.util.Deque;
import java.util.Scanner;

public class Clear implements Command {
    public Clear() {}
    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route route = request.route();
        try {
            String ownerLogin = request.login();
            int deleted = dbCommands.deleteAllByOwner(ownerLogin);
            collection.removeIf(r -> r.getOwner().equals(ownerLogin));
            return new Response("Удалено маршрутов: " + deleted, true);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
}
