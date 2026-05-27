package Client;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHTunnel {
    private static final int LOCAL_PORT = 11280;
    private static final int REMOTE_PORT = 11280;

    private Session session;

    public void connect(String sshHost, int sshPort, String user, String password) throws JSchException {
        JSch jsch = new JSch();

        session = jsch.getSession(user, sshHost, sshPort);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(5000);

        session.setPortForwardingL(LOCAL_PORT, "localhost", REMOTE_PORT);
        System.out.println("SSH tunnel established: localhost:" + LOCAL_PORT + " -> " + sshHost + ":" + REMOTE_PORT);
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("SSH tunnel closed");
        }
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }
}