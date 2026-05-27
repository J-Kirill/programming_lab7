package Client;

import java.io.IOException;

import static Client.ClientIOManager.startCLI;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            startCLI();
        } else if (args.length == 4) {
            String sshHost = args[0];
            int sshPort = Integer.parseInt(args[1]);
            String sshUser = args[2];
            String sshPassword = args[3];

            startCLI(sshHost, sshPort, sshUser, sshPassword);
        } else {
            throw new IllegalArgumentException("Invalid arguments");
        }
    }
}
