package best.aog.chat.server.model.messages.client;

import best.aog.chat.server.model.messages.MessageBody;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PrivateMessageBody extends MessageBody {
    private String userName;
    private String message;
    private String receiverUserName;
}