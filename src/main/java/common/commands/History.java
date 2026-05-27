package common.commands;

import common.commands.dbCommands.DatabaseCommands;
import common.MetaHashSet;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.util.Deque;
import java.util.Scanner;

public class History implements Command {
    public History() {}
    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route route = request.route();
        try {
            StringBuilder output = new StringBuilder();
            for (String line1 : history) {
                output.append(line1).append("\n");
            }
            return new Response(output.toString(), true);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
}
