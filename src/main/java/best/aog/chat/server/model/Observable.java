package best.aog.chat.server.model;

import best.aog.chat.server.model.messages.Message;

import java.util.List;

public interface Observable {
    void addObserver(Observer o);
    void stopObserver(Observer o);
    void notifyObservers(Message message);
    //void notifyObservers(Message message, User[] receivers);
}
