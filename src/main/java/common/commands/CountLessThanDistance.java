package common.commands;


import common.commands.dbCommands.DatabaseCommands;
import common.MetaHashSet;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.util.Deque;
import java.util.Scanner;

public class CountLessThanDistance implements Command {
    public CountLessThanDistance() {}
    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route route = request.route();
        try {
            int count = 0;
            for (Route route1 : collection) {
                if (route1.compareDistance(Float.parseFloat(args.group2())) < 0) {
                    count++;
                }
            }
            return new Response(Integer.toString(count), true);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
}
