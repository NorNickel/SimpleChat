package best.aog.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
    public final static int PORT = 8290;

    public void start() {
        System.out.println("===Server started===");
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket socket = serverSocket.accept();
            while (true) {
                if (socket != null) {
                    /*ObjectInputStream inputStream =
                            new ObjectInputStream(socket.getInputStream());
                    best.aog.chat.server.Client client = (best.aog.chat.server.Client) inputStream.readObject();
                    inputStream.close();
                    System.out.println("best.aog.chat.server.Client with login \'"
                            + client.getLogin() + '\''
                            + " has connected");*/
                    System.out.println(socket.isConnected());
                    BufferedReader clientReader =
                            new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String clientMessage = clientReader.readLine();
                    System.out.println(clientMessage);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
