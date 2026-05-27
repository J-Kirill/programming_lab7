package common.commands;

import common.commands.dbCommands.DatabaseCommands;
import common.MetaHashSet;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.util.Comparator;
import java.util.Deque;
import java.util.Scanner;

public class PrintFieldDescendingDistance implements Command {
    public PrintFieldDescendingDistance() {}
    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route route = request.route();
        try {
            StringBuilder builder = new StringBuilder();
            collection.stream()
                    .map(Route::getDistance)
                    .sorted(Comparator.reverseOrder())
                    .forEach(r -> builder.append(r).append("\n"));
            return new Response(builder.toString(), true);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
}
