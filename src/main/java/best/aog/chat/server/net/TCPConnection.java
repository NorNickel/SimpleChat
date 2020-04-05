package best.aog.chat.server.net;

import best.aog.chat.server.messages.Message;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

public class TCPConnection implements Closeable {

    private String userName = null;
    private Socket socket;
    private Thread receiveThread;
    private TCPConnectionListener eventListener;

    private BufferedReader in;
    private PrintWriter out;


    private static Gson gson = new Gson();

    public TCPConnection(TCPConnectionListener eventListener, String host, int port) throws IOException {
        this(eventListener, new Socket(host, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) {
        this.eventListener = eventListener;
        try {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        eventListener.onConnectionReady(TCPConnection.this);
                        while (!receiveThread.isInterrupted()) {
                            eventListener.onReceiveMessage(TCPConnection.this, in.readLine());
                        }
                    } catch (IOException e) {
                        eventListener.onException(TCPConnection.this, e);
                    } finally {
                        eventListener.onDisconnect(TCPConnection.this);
                    }
                }
            });
            receiveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public synchronized void sendMessage(Message message) {
        out.println(gson.toJson(message));
        out.flush();
    }

    public synchronized void disconnect() {
        receiveThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return socket.getInetAddress() + ": " + socket.getPort();
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public void authorize(String userName) {
        eventListener.onAuthorizationAccepted(this, userName);
    }
}