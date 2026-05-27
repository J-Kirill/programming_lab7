package common.commands;

import common.commands.dbCommands.DatabaseCommands;
import common.InvalidData;
import common.MetaHashSet;
import common.commands.dbCommands.*;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.util.*;

public interface Command {
    HashMap<String, Command> commands = null;

    Response execute(Request request, MetaHashSet<Route> collection, Scanner scanner,
                     Deque<String> history, DatabaseCommands dbCommands) throws Exception;

    default Response historyExecute(Request request, MetaHashSet<Route> collection, Scanner scanner,
                                    Deque<String> history, DatabaseCommands dbCommands) throws Exception {
        history.removeLast();
        history.addFirst(request.cArgs().group1());
        return execute(request, collection, scanner, history, dbCommands);
    }
    default Response doCommand(Request request, MetaHashSet<Route> collection, Scanner scanner,
                               Deque<String> history, DatabaseCommands dbCommands){
        try {
            return chooseCommand(request, collection, scanner, history, dbCommands);
        } catch (InvalidData e) {
            return new Response(e.getMessage(), false);
        }
    }
    default Response chooseCommand(Request request, MetaHashSet<Route> collection, Scanner scanner,
                                   Deque<String> history, DatabaseCommands dbCommands) throws InvalidData {
        try{
            return TypesOfAvailableCommands.commands.get(request.cArgs().group1()).historyExecute(request, collection,
                    scanner, history, dbCommands);
        } catch (Exception e) {
            throw new InvalidData("Нет такой команды");
        }
    }

    enum TypesOfAvailableCommands {
        DONT_NEED_OBJECT(new ArrayList<>(List.of(
                new Pair("execute_script", new ExecuteScript(new ArrayList<String>())),
                new Pair("info", new Info()),
                new Pair("show", new Show()),
                new Pair("clear", new Clear()),
                new Pair("history", new History()),
                new Pair("remove_by_id", new RemoveById()),
                new Pair("count_less_than_distance", new CountLessThanDistance()),
                new Pair("print_descending", new PrintDescending()),
                new Pair("print_field_descending_distance", new PrintFieldDescendingDistance())))),

        NEED_OBJECT(new ArrayList<>(List.of(
                new Pair("add", new Add()),
                new Pair("update", new Update()),
                new Pair("add_if_max", new AddIfMax()),
                new Pair("remove_greater", new RemoveGreater()))));

        private final ArrayList<Pair> commandPairs;

        public static final HashMap<String, Command> commands = initMap();

        private static HashMap<String, Command> initMap() {
            HashMap<String, Command> map = new HashMap<>();
            for (TypesOfAvailableCommands type : TypesOfAvailableCommands.values()) {
                for (Pair pair : type.commandPairs) {
                    map.put(pair.key(), pair.command());
                }
            }
            return map;
        }

        TypesOfAvailableCommands(ArrayList<Pair> commands){
            this.commandPairs = commands;
        }

        public ArrayList<Pair> getCommandPairs(){
            return commandPairs;
        }
        public boolean isThisType(String commandName){
            for (Pair p : this.commandPairs){
                if (p.key().equals(commandName)){
                    return true;
                }
            }
            return false;
        }
        public static boolean commandAvailable(String commandName){
            for (TypesOfAvailableCommands type : TypesOfAvailableCommands.values()){
                if (type.isThisType(commandName)){
                    return true;
                }
            }
            return false;
        }
        public static ArrayList<String> getFullListOfAvailableCommands(){
            ArrayList<String> commandNames = new ArrayList<>();
            for (TypesOfAvailableCommands type : TypesOfAvailableCommands.values()){
                for(Pair p : type.getCommandPairs()){
                    commandNames.add(p.key());
                }
            }
            return commandNames;
        }
        public static HashMap<String, Command> getMapOfAvailableCommands(){
            HashMap<String, Command> map = new HashMap<>();
            for (TypesOfAvailableCommands type : TypesOfAvailableCommands.values()){
                for(Pair p : type.getCommandPairs()){
                    map.put(p.key(),p.command());
                }
            }
            return map;
        }
        public static record Pair (String key, Command command){}
    }
}
