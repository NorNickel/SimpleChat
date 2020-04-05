package best.aog.chat.server.messages;

import best.aog.chat.server.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RegisterMessageBody extends MessageBody {
    private User user;
}