package best.aog.chat.server;

import lombok.SneakyThrows;

import java.awt.datatransfer.ClipboardOwner;
import java.io.*;
import java.net.Socket;

public class ClientEntity implements Runnable, Observer {
    private Client client;
    private Socket socket;
    private MyServer server;

    public ClientEntity(Socket socket, MyServer server) {
        this.socket = socket;
        this.server = server;
    }

    @SneakyThrows
    @Override
    public void run() {
        BufferedReader clientReader =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while (true) {
            String clientMessage = clientReader.readLine();
            if (clientMessage.startsWith("REG")) {
                String[] logPass = clientMessage.substring(3).split(":");
                client = new Client(logPass[0], logPass[1]);
                System.out.println("New client has connected: "
                        + logPass[0] + " " + logPass[1]);
                server.addObserver(this);
            } else {
                System.out.println("client message: " + clientMessage);
                server.notifyObservers(client.getLogin() + ": " + clientMessage);
            }
        }
    }

    @SneakyThrows
    @Override
    public void notifyObserver(String message) {
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.println(message);
        writer.flush();
    }
}
