package best.aog.chat.server.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RegularMessageBody extends MessageBody {
    private String userName;
    private String message;
}