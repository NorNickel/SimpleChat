package best.aog.chat.server.model;

import best.aog.chat.server.model.messages.Message;

import java.io.IOException;

public interface Observer {
    void notifyObserver(Message message) throws IOException;
}
