package best.aog.chat.server.model;

import best.aog.chat.server.controller.UserController;
import best.aog.chat.server.model.messages.Message;
import best.aog.chat.server.service.UserDao;
import best.aog.chat.server.service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Observable {
    public final static int PORT = 8290;
    private volatile List<Observer> users = new ArrayList<>();
    private UserService userService = UserDao.getInstance();

    public void start() {
        System.out.println("===Server started==="); //???!!! remove
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket socket = null;
            while (true) {
                if (socket == null) {
                    socket = serverSocket.accept();
                } else {
                    startUserController(socket);
                    socket = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Observer> getAllObservers() {
        return users;
    }

    private void startUserController(Socket socket) {
        UserController userController = new UserController(socket, this);
        new Thread(userController).start();
    }

    @Override
    public void addObserver(Observer o) {
        users.add(o);
    }

    @Override
    public void stopObserver(Observer o) {
        users.remove(o);
    }

    @Override
    public void notifyObservers(Message message) {
        for (Observer observer : users) {
            try {
                observer.notifyObserver(message);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
