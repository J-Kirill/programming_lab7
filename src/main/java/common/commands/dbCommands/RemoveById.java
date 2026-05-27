package common.commands.dbCommands;

import common.MetaHashSet;
import common.commands.Command;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.util.Deque;
import java.util.Scanner;

public class RemoveById implements Command {
    public RemoveById() {}
    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route route = request.route();
        try {
            if (dbCommands.deleteById(Long.getLong(args.group2()), request.login())) {
                return new Response("Успешно удалено", true);
            }
            return new Response("Ошибка при удалении из БД", false);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
}
