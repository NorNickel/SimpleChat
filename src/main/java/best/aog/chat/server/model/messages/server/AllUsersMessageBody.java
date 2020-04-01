package best.aog.chat.server.model.messages.server;

import best.aog.chat.server.model.Observer;
import best.aog.chat.server.model.User;
import best.aog.chat.server.model.messages.MessageBody;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AllUsersMessageBody extends MessageBody {
    private String[] login;
}
