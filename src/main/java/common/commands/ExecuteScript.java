package common.commands;

import Server.CommandManager;
import common.commands.dbCommands.DatabaseCommands;
import common.InvalidData;
import common.MetaHashSet;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Scanner;

public class ExecuteScript implements Command {
    protected ArrayList<String> paths;

    public ExecuteScript(ArrayList<String> paths) {
        this.paths = paths;
    }

    @Override
    public Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) {
        CArgs args = request.cArgs();
        Route route = request.route();
        try {
            StringBuilder message = new StringBuilder();
            try {
                Scanner newScanner = new Scanner(new File(args.group2().trim().replaceAll("^\"|\"$", "")));
                while (newScanner.hasNextLine()) {
                    String line = newScanner.nextLine();
                    CArgs cArgs = CArgs.parse(line);
                    Response response;
                    if (TypesOfAvailableCommands.DONT_NEED_OBJECT.isThisType(cArgs.group1())) {
                        response = CommandManager.handle(new Request(cArgs, null, request.login(), request.password()));
                    } else if (TypesOfAvailableCommands.NEED_OBJECT.isThisType(cArgs.group1())) {
                        response = CommandManager.handle(new Request(cArgs, Route.getFromDescription(newScanner), request.login(), request.password()));
                    } else {
                        throw new InvalidData("Something went wrong with command types");
                    }
                    if (response == null) {
                        message.append("No response from command: ").append(cArgs.group1()).append("\n");
                    } else {
                        message.append(response.message()).append("\n");
                    }
                }

            } catch (FileNotFoundException e) {
                return new Response(e.getMessage(), false);
            }
            return new Response(message.toString(), true);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        }
    }
    @Override
    public Response chooseCommand(Request request, MetaHashSet<Route> collection, Scanner scanner, Deque<String> history, DatabaseCommands dbCommands) throws InvalidData {
        CArgs cArgs = request.cArgs();
        if (cArgs.group1().equals("execute_script")) {
            if (this.paths.contains(cArgs.group2())) {
                throw new InvalidData("Цикл запрещён");
            }
            ArrayList<String> newPaths = (ArrayList<String>) this.paths.clone();
            newPaths.add(cArgs.group2());
            return new ExecuteScript(newPaths).execute(request, collection, scanner, history, dbCommands);
        } else {
            try {
                return TypesOfAvailableCommands.commands.get(request.cArgs().group1()).execute(request, collection, scanner, history, dbCommands);
            } catch (Exception e) {
                throw new InvalidData("Нет такой команды");
            }
        }
    }
}
