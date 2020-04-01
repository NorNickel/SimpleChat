package best.aog.chat.server.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Message {
    private MessageType messageType;
    private MessageBody messageBody;
}