package best.aog.chat.server.model;

import best.aog.chat.server.Config;
import best.aog.chat.server.controller.TCPConnection;
import best.aog.chat.server.controller.TCPConnectionListener;
import best.aog.chat.server.model.messages.Message;
import best.aog.chat.server.model.messages.MessageResultType;
import best.aog.chat.server.model.messages.MessageType;
import best.aog.chat.server.model.messages.Messages;
import best.aog.chat.server.model.messages.client.AuthorizeMessageBody;
import best.aog.chat.server.model.messages.client.PrivateMessageBody;
import best.aog.chat.server.model.messages.client.RegisterMessageBody;
import best.aog.chat.server.model.messages.client.RegularMessageBody;
import best.aog.chat.server.model.messages.server.AllUsersMessageBody;
import best.aog.chat.server.model.messages.server.ResultMessageBody;
import best.aog.chat.server.service.UserDao;
import best.aog.chat.server.service.UserService;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server implements Observable, TCPConnectionListener {

    public static Map<String, TCPConnection> connectionMap = new HashMap<>();

    private static Gson gson = new Gson(); //???!!!

    private volatile List<Observer> users = new ArrayList<>();
    private final UserService userService = UserDao.getInstance();

    public void start() {
        System.out.println("===Server started===");//???!!! logger
        try {
            ServerSocket serverSocket = new ServerSocket(Config.PORT);
            Socket socket = null;
            while (true) {
                if (socket == null) {
                    socket = serverSocket.accept();
                } else {
                    new TCPConnection(this, socket);
                    socket = null;
                }
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e);
        }
    }

    public void addConnection(String userName, TCPConnection connection) {
        connectionMap.put(userName, connection);
    }

    public void removeConnection(String userName) {
        connectionMap.remove(userName);
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

    @Override
    public void onConnectionReady(TCPConnection connection) {
        System.out.println("User connected: " + connection.toString());
    }

    @Override
    public void onAuthorizationAccepted(TCPConnection connection) {

    }

    @Override
    public void onDisconnect(TCPConnection connection) {
        System.out.println("User disconnected: " + connection.toString());
        stopObserver(connection);
        sendRemoveUser(connection, connection.getLogin());
    }

    @Override
    public void onReceiveMessage(TCPConnection connection, String jsonMessage) {
        Message message = Messages.parseMessage(jsonMessage);
        if (message.getMessageType() == MessageType.REGISTER_MESSAGE) {
            RegisterMessageBody regBody = (RegisterMessageBody)message.getMessageBody();
            User user = userService.getUserByLogin(regBody.getUser().getLogin());

            Message messageFromServer;
            if (user == null) {//???!!!
                user = regBody.getUser();
                userService.addUser(user);
                ResultMessageBody resultMessageBody = new ResultMessageBody(MessageResultType.ACCEPT, "");
                messageFromServer = new Message(MessageType.RESULT_MESSAGE, resultMessageBody);
                connection.sendMessage(gson.toJson(messageFromServer));
                //sendChatHistory(connection);
                connection.setLogin(user.getLogin());
                notifyObservers(Messages.createAddUserMessage(user.getLogin()));
                addObserver(connection);
                addConnection(user.getLogin(), connection);
                sendAllConnectedUsers(connection);
            } else {
                String text = "Такой пользователь уже есть в БД";
                ResultMessageBody resultMessageBody = new ResultMessageBody(MessageResultType.DECLINE, text);
                messageFromServer = new Message(MessageType.RESULT_MESSAGE, resultMessageBody);
                connection.notifyObserver(messageFromServer);
            }
        } else if (message.getMessageType() == MessageType.AUTHORIZE_MESSAGE) {
            AuthorizeMessageBody authBody = (AuthorizeMessageBody)message.getMessageBody();
            User user = new User(authBody.getLogin(), authBody.getPassword());
            Message messageFromServer;
            if (userService.validateUser(user)) {
                ResultMessageBody resultMessageBody = new ResultMessageBody(MessageResultType.ACCEPT, "");
                messageFromServer = new Message(MessageType.RESULT_MESSAGE, resultMessageBody);
                connection.notifyObserver(messageFromServer);
                //sendChatHistory(connection);
                connection.setLogin(user.getLogin());
                notifyObservers(Messages.createAddUserMessage(user.getLogin()));
                addObserver(connection);
                addConnection(user.getLogin(), connection);
                sendAllConnectedUsers(connection);
            } else {
                String text = "Неверный логин и/или пароль";
                ResultMessageBody resultMessageBody = new ResultMessageBody(MessageResultType.DECLINE, text);
                messageFromServer = new Message(MessageType.RESULT_MESSAGE, resultMessageBody);
                connection.notifyObserver(messageFromServer);
            }
        } else if (message.getMessageType() == MessageType.REGULAR_MESSAGE) {
            RegularMessageBody regBody = (RegularMessageBody) message.getMessageBody();
            Message messageFromServer = new Message(MessageType.REGULAR_MESSAGE, regBody);
            userService.saveMessage(messageFromServer);
            notifyObservers(messageFromServer);
        } else if (message.getMessageType() == MessageType.PRIVATE) {
            PrivateMessageBody body = (PrivateMessageBody) message.getMessageBody();
            Message messageFromServer = new Message(MessageType.PRIVATE, body);
            TCPConnection receiver;
            receiver = connectionMap.get(body.getReceiverUserName());
            if (receiver != null) {
                sendPrivateMessage(receiver, body.getUserName(), body.getMessage(), body.getReceiverUserName());
                sendPrivateMessage(connection, body.getUserName(), body.getMessage(), body.getReceiverUserName());
            }
        }
    }

    @Override
    public void onException(TCPConnection connection, Exception e) {
        System.out.println("Exception: " + e);
    }

    private void sendChatHistory(TCPConnection connection) {
        List<Message> messages = userService.getMessages();
        for (Message message : messages) {
            connection.notifyObserver(message);
        }
    }

    private void sendAllConnectedUsers(TCPConnection connection) {
        String[] usersList = new String[users.size()];
        int i = 0;
        for (Observer observer : users) {
            TCPConnection con = (TCPConnection) observer;
            usersList[i] = con.getLogin();
            i++;
        }
        AllUsersMessageBody allUsersMessageBody = new AllUsersMessageBody(usersList);
        Message messageFromServer = new Message(MessageType.ALL_USERS, allUsersMessageBody);
        connection.notifyObserver(messageFromServer);
    }


    private void sendAddUser(TCPConnection connection, String userName) {
        notifyObservers(Messages.createAddUserMessage(userName));
    }

    private void sendRemoveUser(TCPConnection connection, String userName) {
        notifyObservers(Messages.createRemovedUserMessage(userName));
    }

    private void sendPrivateMessage(TCPConnection connection, String userNameFrom, String text , String userNameTo) {
        connection.notifyObserver(Messages.createPrivateMessage(userNameFrom, text, userNameTo) );
    }
}