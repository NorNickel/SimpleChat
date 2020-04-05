package best.aog.chat.server.model;

import best.aog.chat.server.Server;
import best.aog.chat.server.messages.*;
import best.aog.chat.server.net.TCPConnection;
import best.aog.chat.server.net.TCPConnectionListener;
import best.aog.chat.server.dao.DAO;
import best.aog.chat.server.dao.DAOMySQL;

import java.net.Socket;
import java.util.List;

public class UserHandler implements Observer, TCPConnectionListener, Runnable {

    private Server server;
    private Socket socket;
    private TCPConnection connection;
    private static final DAO dao = DAOMySQL.getInstance();

    public UserHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        connection = new TCPConnection(this, socket);
    }

    @Override
    public void onConnectionReady(TCPConnection connection) {
        System.out.println("User connected: " + connection.toString());
    }

    @Override
    public void onAuthorizationAccepted(TCPConnection connection, String userName) {
        connection.setUserName(userName);
        connection.sendMessage(
                new Message(MessageType.RESULT, new ResultMessageBody(MessageResultType.ACCEPT, "")));
        server.notifyObservers(new Message(MessageType.USER_ADDED, new AddUserMessageBody(userName)));
        sendChatHistory();
        server.addObserver(userName, this);
        sendAllConnectedUsers();
    }

    @Override
    public void onAuthorizationDeclined(TCPConnection connection, String reason) {
        notifyObserver(new Message(MessageType.RESULT, new ResultMessageBody(MessageResultType.DECLINE, reason)));
    }

    @Override
    public void onDisconnect(TCPConnection connection) {
        System.out.println("User disconnected: " + connection.toString());
        String userName = connection.getUserName();
        server.stopObserver(userName);
        connection.disconnect();
        if (userName != null) {
            server.notifyObservers(new Message(MessageType.USER_REMOVED, new RemoveUserMessageBody(userName)));
        }
    }

    @Override
    public void onReceiveMessage(TCPConnection connection, String jsonMessage) {
        if (jsonMessage == null) return;

        Message message = Messages.parseMessage(jsonMessage);
        MessageType messageType = message.getType();

        if (messageType == MessageType.REGISTER) {
            RegisterMessageBody regBody = (RegisterMessageBody)message.getBody();
            String userName = regBody.getUser().getUserName();
            User user = dao.getUserByUserName(userName);
            if (user == null) {
                user = regBody.getUser();
                if (dao.saveUser(user)) {
                    connection.authorize(userName);
                } else {
                    String text = "Сбой при регистрации.";
                    notifyObserver(
                            new Message(MessageType.RESULT,
                                    new ResultMessageBody(MessageResultType.DECLINE, text)));
                }
            } else {
                String text = "Такой пользователь уже есть";
                notifyObserver(
                        new Message(MessageType.RESULT,
                                new ResultMessageBody(MessageResultType.DECLINE, text)));
            }

        } else if (messageType == MessageType.AUTHORIZE) {
            AuthorizeMessageBody body = (AuthorizeMessageBody)message.getBody();
            String userName = body.getUserName();

            if (dao.validateUser(userName, body.getPassword())) {
                if (!server.getConnectionMap().containsKey(userName)) {
                    connection.authorize(userName);
                } else {
                    onAuthorizationDeclined(connection, "Вы уже в чате");
                }
            } else {
                onAuthorizationDeclined(connection,"Неверный логин и/или пароль");
            }

        } else if (messageType == MessageType.REGULAR) {
            dao.saveMessage(message);
            server.notifyObservers(message);

        } else if (messageType == MessageType.PRIVATE) {
            UserHandler receiver = server.getConnectionMap().get(
                    ((PrivateMessageBody) message.getBody()).getReceiverUserName());
            TCPConnection receiverConnection = receiver.connection;
            if (receiverConnection != null) {
                sendToOne(receiverConnection, message); // send to receiver
                notifyObserver(message);                // send to sender
            }
        }
    }

    @Override
    public void onException(TCPConnection connection, Exception e) {
        System.out.println("Exception: " + e);
    }

    @Override
    public void notifyObserver(Message message) {
        connection.sendMessage(message);
    }

    private void sendAllConnectedUsers() {
        notifyObserver(new Message
                (MessageType.ALL_USERS,
                        new AllUsersMessageBody(server.getConnectionMap().keySet().toArray(new String[0]))));
    }

    private void sendToOne(TCPConnection connection, Message message){
        connection.sendMessage(message);
    }

    private void sendChatHistory() {
        List<Message> messages = dao.getMessages();
        for (Message message : messages) {
            notifyObserver(message);
        }
    }
}
