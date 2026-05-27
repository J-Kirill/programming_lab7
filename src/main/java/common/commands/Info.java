package common.commands;

import common.commands.dbCommands.DatabaseCommands;
import common.MetaHashSet;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.util.Deque;
import java.util.Scanner;

public class Info implements Command {
    public Info() {}
    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route route = request.route();
        try {
            String line = "type: " + collection.getType() +
                    "\ninitialization_date: " + collection.getCreatedAt() +
                    "\nsize: " + collection.size();
            return new Response(line, true);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
}
