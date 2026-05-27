package Client;

import com.jcraft.jsch.JSchException;
import common.InvalidData;
import common.PasswordHasher;
import common.commands.Command.TypesOfAvailableCommands;
import common.stored.CArgs;
import common.stored.Request;
import common.stored.Response;
import common.stored.Route;

import java.io.IOException;
import java.util.Scanner;

public class ClientIOManager {
    /**
     * Метод, запускающий CLI, изменяет коллекцию из файла.
     *
     * @throws IOException Ошибка ввода/вывода.
     */
    public static void startCLI() throws IOException {
        try {
            TCPClient client = new TCPClient();
            Scanner scanner = new Scanner(System.in);
            String login;
            String password;
            do {
                System.out.flush();
                System.out.print("Type the login: ");
                login = scanner.nextLine();
                System.out.print("Type the password: ");
                password = PasswordHasher.hash(scanner.nextLine());
            } while (!authenticate(client, login, password));
            System.out.println("Type \"help\" for a list of available commands");
            while (true) {
                String line = scanner.nextLine();
                try {
                    if (line.equals("help")) {
                        System.out.println("Available commands:");
                        for (String commandName : TypesOfAvailableCommands.getFullListOfAvailableCommands()) {
                            System.out.println(commandName);
                        }
                    } else if (line.equals("exit")) {
                        System.out.println("Bye!");
                        System.exit(0);
                    } else {
                        CArgs cArgs = CArgs.parse(line);
                        if (TypesOfAvailableCommands.commandAvailable(cArgs.group1())) {
                            if (TypesOfAvailableCommands.DONT_NEED_OBJECT.isThisType(cArgs.group1())) {
                                exchange(new Request(cArgs, null, login, password), client);
                            } else if (TypesOfAvailableCommands.NEED_OBJECT.isThisType(cArgs.group1())) {
                                exchange(new Request(cArgs, Route.getFromDescription(scanner), login, password), client);
                            }
                        } else {
                            throw new InvalidData("Invalid command");
                        }
                    }
                } catch (InvalidData e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    public static void startCLI(String sshHost, int sshPort, String sshUser, String sshPassword){
        SSHTunnel tunnel = new SSHTunnel();
        try {
            tunnel.connect(sshHost, sshPort, sshUser, sshPassword);
            startCLI();
        } catch (JSchException e) {
            System.err.println("SSH error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            tunnel.disconnect();
        }
    }
    public static boolean authenticate(TCPClient client, String login, String password) throws IOException {
        Request request = new Request(null, null, login, password);
        Response response = client.sendRequest(request);
        System.out.println(response.message());
        return response.success();
    }
    public static void exchange(Request request, TCPClient client) throws IOException {
        showResponse(client.sendRequest(request));
    }

    public static void showResponse(Response response) throws IOException {
        if (response.success()) {
            System.out.println(response.message());
        } else {
            System.err.println(response.message());
        }

    }
}
