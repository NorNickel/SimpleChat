package best.aog.chat.server.model.messages.server;

import best.aog.chat.server.model.messages.MessageBody;
import best.aog.chat.server.model.messages.MessageResultType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ResultMessageBody extends MessageBody {
    private MessageResultType result;
    private String message;
}
