package best.aog.chat.server.model.messages.client;

import best.aog.chat.server.model.messages.MessageBody;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuthorizeMessageBody extends MessageBody {
    private String login;
    private String password;
}
