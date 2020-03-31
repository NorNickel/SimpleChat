package best.aog.chat.server.model.messages;

public enum MessageType {
    REGISTER_MESSAGE, AUTHORIZE_MESSAGE, REGULAR_MESSAGE, // clients messages
    RESULT_MESSAGE; // server messages
}
