package best.aog.chat.server.controller;

import best.aog.chat.server.model.*;
import best.aog.chat.server.model.messages.MessageResultType;
import best.aog.chat.server.model.messages.Message;
import best.aog.chat.server.model.messages.MessageType;
import best.aog.chat.server.model.messages.client.AuthorizeMessageBody;
import best.aog.chat.server.model.messages.client.RegisterMessageBody;
import best.aog.chat.server.model.messages.client.RegularMessageBody;
import best.aog.chat.server.model.messages.server.ResultMessageBody;
import best.aog.chat.server.service.UserDao;
import best.aog.chat.server.service.UserService;
import com.google.gson.*;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserController implements Runnable, Observer {
    private final static User serverBot = new User("serverBot", "");
    private User user;
    private boolean isAuthorized = false;
    private Server server;
    private Socket socket;
    private BufferedReader serverReader;
    private PrintWriter serverWriter;

    private UserService userService;
    private static Gson gson = new Gson();

    public UserController(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        user = new User();
        userService = UserDao.getInstance();
    }

    @Override
    public void run() {
        try {
            // 1 - Регистрация
            // 2 - Авторизация
            // 3 - Обычные сообщения в чат
            serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverWriter = new PrintWriter(socket.getOutputStream());

            while (true) {
                String messageFromClientJson = serverReader.readLine();
                processJsonMessage(messageFromClientJson);
            }
        } catch (IOException e) {
            if (user != null) {
                RegularMessageBody regBody = new RegularMessageBody(serverBot, user.getLogin() + " покинул нас", null); //???!!!
                Message messageFromServer = new Message(MessageType.REGULAR_MESSAGE, regBody);
                server.notifyObservers(messageFromServer);
            }
            // e.printStackTrace();
        }
    }

    public void processJsonMessage(String messageFromClientJson) {
        JsonObject jsonObject = JsonParser.parseString(messageFromClientJson).getAsJsonObject();
        MessageType messageType = gson.fromJson(jsonObject.get("messageType"), MessageType.class);

        if (messageType == MessageType.REGISTER_MESSAGE) {
            RegisterMessageBody regBody = gson.fromJson(jsonObject.get("messageBody"), RegisterMessageBody.class);
            User user = userService.getUserByLogin(regBody.getUser().getLogin());
            Message messageFromServer;
            if (user == null) {//???!!!
                this.user = regBody.getUser();
                userService.addUser(this.user);
                ResultMessageBody resultMessageBody = new ResultMessageBody(MessageResultType.ACCEPT, "");
                messageFromServer = new Message(MessageType.RESULT_MESSAGE, resultMessageBody);
                notifyObserver(messageFromServer);
                sendChatHistory();
                server.addObserver(this);
            } else {
                String text = "Такой пользователь уже есть в БД";
                ResultMessageBody resultMessageBody = new ResultMessageBody(MessageResultType.DECLINE, text);
                messageFromServer = new Message(MessageType.RESULT_MESSAGE, resultMessageBody);
                notifyObserver(messageFromServer);
            }

        } else if (messageType == MessageType.AUTHORIZE_MESSAGE) {
            AuthorizeMessageBody authBody = gson.fromJson(jsonObject.get("messageBody"), AuthorizeMessageBody.class);
            User user = new User(authBody.getLogin(), authBody.getPassword());
            Message messageFromServer;
            if (userService.validateUser(user)) {
                this.user = user;
                ResultMessageBody resultMessageBody = new ResultMessageBody(MessageResultType.ACCEPT, "");
                messageFromServer = new Message(MessageType.RESULT_MESSAGE, resultMessageBody);
                notifyObserver(messageFromServer);
                sendChatHistory();
                server.addObserver(this);
            } else {
                String text = "Неверный логин и/или пароль";
                ResultMessageBody resultMessageBody = new ResultMessageBody(MessageResultType.DECLINE, text);
                messageFromServer = new Message(MessageType.RESULT_MESSAGE, resultMessageBody);
                notifyObserver(messageFromServer);
            }
        } else if (messageType == MessageType.REGULAR_MESSAGE) {
            RegularMessageBody regBody = gson.fromJson(jsonObject.get("messageBody"), RegularMessageBody.class);
            Message messageFromServer = new Message(MessageType.REGULAR_MESSAGE, regBody);

            User[] receivers = regBody.getReceivers();

            //if (regBody.getReceivers() == null) {
                userService.saveMessage(messageFromServer);
                server.notifyObservers(messageFromServer);
            //} else {
            //    server.notifyObservers(messageFromServer, receivers);
            //}
        }
    }

    private void sendChatHistory() {
        List<Message> messages = userService.getMessages();
        for (Message message : messages) {
            notifyObserver(message);
        }
    }

    @Override
    public void notifyObserver(Message message) {
        serverWriter.println(gson.toJson(message));
        serverWriter.flush();
    }
}