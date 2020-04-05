package best.aog.chat.server.net;

public interface TCPConnectionListener {
    void onConnectionReady(TCPConnection connection);
    void onAuthorizationAccepted(TCPConnection connection, String userName);
    void onAuthorizationDeclined(TCPConnection connection, String reason);
    void onDisconnect(TCPConnection connection);
    void onReceiveMessage(TCPConnection connection, String jsonMessage);
    void onException(TCPConnection connection, Exception e);
}
