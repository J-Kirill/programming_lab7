package common.commands.dbCommands;

import common.MetaHashSet;
import common.commands.Command;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.util.Deque;
import java.util.Scanner;

public class Update implements Command {
    public Update() {
    }

    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route newRoute = request.route();
        try {
            if (dbCommands.updateById(Long.getLong(args.group2()), newRoute, request.login())) {
                for (Route route : collection) {
                    if (route.getId() == Integer.parseInt(args.group2())) {
                        collection.remove(route);
                        newRoute.setId(route.getId());
                        collection.add(newRoute);
                        return new Response("Успешно обновлено", true);
                    }
                }
                return new Response("Если это вылезло, значит в коде ошибка, коллекция рассинхронизировалась", false);
            }
            return new Response("Ошибка при добавлении в БД", false);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
}
