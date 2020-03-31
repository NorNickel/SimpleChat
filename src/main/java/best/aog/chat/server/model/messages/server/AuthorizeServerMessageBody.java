package best.aog.chat.server.model.messages.server;

import best.aog.chat.server.model.User;
import best.aog.chat.server.model.messages.MessageBody;
import best.aog.chat.server.model.messages.MessageResultType;

public class AuthorizeServerMessageBody extends MessageBody {
    private MessageResultType result;
    private User user;
    private String errorMessage;
}
