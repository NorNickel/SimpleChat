package best.aog.chat.server.model;

import best.aog.chat.server.messages.Message;


public interface Observable {
    void addObserver(String userName, Observer o);
    void stopObserver(String userName);
    void notifyObservers(Message message);
}
