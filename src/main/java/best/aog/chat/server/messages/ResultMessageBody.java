package best.aog.chat.server.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResultMessageBody extends MessageBody {
    private MessageResultType result;
    private String message;
}
