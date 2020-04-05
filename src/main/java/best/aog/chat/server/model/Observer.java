package best.aog.chat.server.model;

import best.aog.chat.server.messages.Message;


public interface Observer {
    void notifyObserver(Message message);
}
