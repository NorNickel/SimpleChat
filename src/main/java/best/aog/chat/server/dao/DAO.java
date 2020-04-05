package best.aog.chat.server.dao;

import best.aog.chat.server.messages.Message;
import best.aog.chat.server.model.User;

import java.util.List;


public interface DAO {
    boolean saveUser(User user);
    boolean validateUser(String userName, String password);
    User getUserByUserName(String login);
    List<Message> getMessages();
    void saveMessage(Message message);
}
