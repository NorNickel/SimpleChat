package best.aog.chat.server.controller;

import best.aog.chat.server.model.Observer;
import best.aog.chat.server.model.messages.Message;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPConnection implements Observer {

    private Socket socket;
    private Thread receiveThread;
    private TCPConnectionListener eventListener;
    private BufferedReader in;
    private PrintWriter out;
    private String login;

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

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public synchronized void sendMessage(String message) {
        out.println(message);
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
    public void notifyObserver(Message message) {
        Gson gson = new Gson();
        sendMessage(gson.toJson(message));
    }

    @Override
    public String toString() {
        return socket.getInetAddress() + ": " + socket.getPort();
    }
} //???!!!