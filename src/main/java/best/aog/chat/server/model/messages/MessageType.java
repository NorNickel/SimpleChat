package best.aog.chat.server.model.messages;

public enum MessageType {
    REGISTER_MESSAGE, AUTHORIZE_MESSAGE, REGULAR_MESSAGE, PRIVATE, // clients messages
    RESULT_MESSAGE, ALL_USERS, USER_ADDED, USER_REMOVED; // server messages
}