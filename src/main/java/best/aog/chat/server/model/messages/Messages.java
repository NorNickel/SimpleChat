package best.aog.chat.server.model.messages;

import best.aog.chat.server.model.User;
import best.aog.chat.server.model.messages.client.AuthorizeMessageBody;
import best.aog.chat.server.model.messages.client.PrivateMessageBody;
import best.aog.chat.server.model.messages.client.RegisterMessageBody;
import best.aog.chat.server.model.messages.client.RegularMessageBody;
import best.aog.chat.server.model.messages.server.AddUserMessageBody;
import best.aog.chat.server.model.messages.server.AllUsersMessageBody;
import best.aog.chat.server.model.messages.server.RemoveUserMessageBody;
import best.aog.chat.server.model.messages.server.ResultMessageBody;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Messages {
    private final static Gson gson = new Gson();

    public static String createRegularMessage(User user, String message) {
        RegularMessageBody regBody = new RegularMessageBody(user, message);
        Message regularMessage = new Message(MessageType.REGULAR_MESSAGE, regBody);
        return gson.toJson(regularMessage);
    }

    public static Message createPrivateMessage(String userNameFrom, String message, String userNameTo) {
        PrivateMessageBody body = new PrivateMessageBody(userNameFrom, message, userNameTo);
        return new Message(MessageType.PRIVATE, body);
    }

    public static String createRegisterMessage(User user) {
        RegisterMessageBody registerMessageBody = new RegisterMessageBody(user);
        Message registerMessage = new Message(MessageType.REGISTER_MESSAGE, registerMessageBody);
        return gson.toJson(registerMessage);
    }

    public static String createAuthorizeMessage(String login, String password) {
        AuthorizeMessageBody authorizeMessageBody = new AuthorizeMessageBody(login, password);
        Message registerMessage = new Message(MessageType.AUTHORIZE_MESSAGE, authorizeMessageBody);
        return gson.toJson(registerMessage);
    }

    public static Message createAddUserMessage(String userName) {
        AddUserMessageBody body = new AddUserMessageBody(userName);
        Message message = new Message(MessageType.USER_ADDED, body);
        return message;
    }

    public static Message createRemovedUserMessage(String userName) {
        AddUserMessageBody body = new AddUserMessageBody(userName);
        Message message = new Message(MessageType.USER_REMOVED, body);
        return message;
    }



    public static Message parseMessage(String message) {
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        MessageType messageType = gson.fromJson(jsonObject.get("messageType"), MessageType.class);

        if (messageType == MessageType.RESULT_MESSAGE) {
            ResultMessageBody body = gson.fromJson(jsonObject.get("messageBody"), ResultMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.REGULAR_MESSAGE) {
            RegularMessageBody body = gson.fromJson(jsonObject.get("messageBody"), RegularMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.AUTHORIZE_MESSAGE) {
            AuthorizeMessageBody body = gson.fromJson(jsonObject.get("messageBody"), AuthorizeMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.REGISTER_MESSAGE) {
            RegisterMessageBody body = gson.fromJson(jsonObject.get("messageBody"), RegisterMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.USER_ADDED) {
            AddUserMessageBody body = gson.fromJson(jsonObject.get("messageBody"), AddUserMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.USER_REMOVED) {
            RemoveUserMessageBody body = gson.fromJson(jsonObject.get("messageBody"), RemoveUserMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.ALL_USERS) {
            AllUsersMessageBody body = gson.fromJson(jsonObject.get("messageBody"), AllUsersMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.PRIVATE) {
            PrivateMessageBody body = gson.fromJson(jsonObject.get("messageBody"), PrivateMessageBody.class);
            return new Message(messageType, body);
        }
        return null;
    }
}
