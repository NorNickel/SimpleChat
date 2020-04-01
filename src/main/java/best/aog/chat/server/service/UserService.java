package best.aog.chat.server.service;

import best.aog.chat.server.model.User;
import best.aog.chat.server.model.messages.Message;

import java.util.List;

public interface UserService {
    boolean addUser(User user);
    boolean removeUser(User user);
    boolean validateUser(User user);
    User getUserByLogin(String login);
    List<Message> getMessages();
    void saveMessage(Message message);
}
