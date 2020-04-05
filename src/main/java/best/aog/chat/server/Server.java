package best.aog.chat.server;

import best.aog.chat.server.model.Config;
import best.aog.chat.server.model.Observable;
import best.aog.chat.server.model.Observer;
import best.aog.chat.server.messages.*;
import best.aog.chat.server.model.UserHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server implements Observable {

    private Map<String, UserHandler> connectionMap = new HashMap<>();

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        System.out.println("===Server started===");
        try {
            ServerSocket serverSocket = new ServerSocket(Config.PORT);
            Socket socket = null;
            while (!Thread.currentThread().isInterrupted()) {
                if (socket == null) {
                    socket = serverSocket.accept();
                } else {
                    new Thread(new UserHandler(this, socket)).start();
                    socket = null;
                }
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e);
        }
    }

    public Map<String, UserHandler> getConnectionMap() {
        return connectionMap;
    }

    @Override
    public void addObserver(String userName, Observer o) {
        connectionMap.put(userName, (UserHandler)o);
    }

    @Override
    public void stopObserver(String userName) {
        connectionMap.remove(userName);
    }

    @Override
    public void notifyObservers(Message message) {
        for (Map.Entry<String, UserHandler> entry : connectionMap.entrySet()) {
            entry.getValue().notifyObserver(message);
        }
    }

}