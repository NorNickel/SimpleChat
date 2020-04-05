package best.aog.chat.server.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Message {
    private MessageType type;
    private MessageBody body;
}