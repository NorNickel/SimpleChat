package best.aog.chat.server.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {
    private final static int NUMBER_OF_CONNECTIONS_INITIAL = 5;
    private final static int MAX_CONNECTIONS = 50;
    private static final String url = "jdbc:MySQL://localhost:3306/chatdb?serverTimezone=UTC";
    private static final String user = "root";
    private static final String password = "12345";

    private List<Connection> connections;
    private List<Connection> usedConnections = new ArrayList<>();

    public void createConnectionPool() {
        connections = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CONNECTIONS_INITIAL; i++) {
            connections.add(openConnection());
        }
    }

    private Connection openConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized Connection get() {
        if (connections.size() > 0) {
            Connection result = connections.remove(connections.size() - 1);
            usedConnections.add(result);
            return result;
        } else {
            if (usedConnections.size() <= MAX_CONNECTIONS) {
                Connection newConnection = openConnection();
                usedConnections.add(newConnection);
                return newConnection;
            } else {
                throw new RuntimeException("Невозможно создать Connection");
            }
        }
    }

    public synchronized void put(Connection connection) {
        usedConnections.remove(connection);
        connections.add(connection);
    }
}
