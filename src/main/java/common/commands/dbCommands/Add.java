package common.commands.dbCommands;

import common.MetaHashSet;
import common.commands.Command;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.util.Deque;
import java.util.Scanner;

public class Add implements Command {
    public Add() {}
    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route newRoute = request.route();
        try {
            Long id = dbCommands.insert(newRoute, request.login());
            if (id != null) {
                newRoute.setId(id.intValue());
                collection.add(newRoute);
                return new Response("Элемент добавлен с id=" + id, true);
            }
            return new Response("Ошибка при добавлении в БД", false);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
}
