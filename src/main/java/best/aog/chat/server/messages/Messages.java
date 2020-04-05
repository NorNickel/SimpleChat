package best.aog.chat.server.messages;

import best.aog.chat.server.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class Messages {
    private final static Gson gson = new Gson();

    public static Message parseMessage(String message) {
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        MessageType messageType = gson.fromJson(jsonObject.get("type"), MessageType.class);
        JsonElement messageBody = jsonObject.get("body");

        if (messageType == MessageType.RESULT) {
            ResultMessageBody body = gson.fromJson(messageBody, ResultMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.REGULAR) {
            RegularMessageBody body = gson.fromJson(messageBody, RegularMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.AUTHORIZE) {
            AuthorizeMessageBody body = gson.fromJson(messageBody, AuthorizeMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.REGISTER) {
            RegisterMessageBody body = gson.fromJson(messageBody, RegisterMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.USER_ADDED) {
            AddUserMessageBody body = gson.fromJson(messageBody, AddUserMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.USER_REMOVED) {
            RemoveUserMessageBody body = gson.fromJson(messageBody, RemoveUserMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.ALL_USERS) {
            AllUsersMessageBody body = gson.fromJson(messageBody, AllUsersMessageBody.class);
            return new Message(messageType, body);
        } else if (messageType == MessageType.PRIVATE) {
            PrivateMessageBody body = gson.fromJson(messageBody, PrivateMessageBody.class);
            return new Message(messageType, body);
        }

        return null;
    }
}
