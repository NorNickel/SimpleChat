package best.aog.chat.server.dao;

import best.aog.chat.server.messages.Message;
import best.aog.chat.server.messages.MessageType;
import best.aog.chat.server.messages.RegularMessageBody;
import best.aog.chat.server.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DAOMySQL implements DAO {

    private final static String BSCRYPT_SALT = BCrypt.gensalt(10);

    // JDBC URL, username and password of MySQL server
    private static final String url = "jdbc:MySQL://localhost:3306/chatdb?serverTimezone=UTC";
    private static final String user = "root";
    private static final String password = "12345";

    private static volatile DAOMySQL instance;

    private ConnectionPool connectionPool = new ConnectionPool();

    private Logger logger;

    private DAOMySQL() {
        logger = Logger.getLogger(DAOMySQL.class.getName());
        logger.log(Level.INFO, "Try to connect to DB: " + url);
        connectionPool.createConnectionPool();
        logger.log(Level.INFO, "Connected to DB: " + url);
    }

    public static DAOMySQL getInstance() {
        if (instance == null) {
            synchronized (DAOMySQL.class) {
                if (instance == null) {
                    instance = new DAOMySQL();
                }
            }
        }
        return instance;
    }

    @Override
    public synchronized boolean saveUser(User user) {
        boolean result = false;
        try (Connection connection = connectionPool.get()) {
            String insert = "INSERT INTO chatdb.user (userName, hashPassword) VALUES (?, ?)";
            PreparedStatement preparedInsert = connection.prepareStatement(insert);
            preparedInsert.setString(1, user.getUserName());
            String hashPassword = BCrypt.hashpw(user.getPassword(), BSCRYPT_SALT);
            preparedInsert.setString(2, hashPassword);
            int count = preparedInsert.executeUpdate();
            result = count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public User getUserByUserName(String userName) {
        User user = null;
        Connection connection = connectionPool.get();
        try {
            String select = "SELECT userName, hashPassword FROM chatdb.user WHERE userName = ?";
            PreparedStatement preparedSelect = connection.prepareStatement(select);
            preparedSelect.setString(1, userName);
            ResultSet rs = preparedSelect.executeQuery();
            while (rs.next()) {
                user = new User();
                user.setUserName(rs.getString(1));
                user.setPassword(rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionPool.put(connection);
        }
        return user;
    }

    @Override
    public List<Message> getMessages() {
        List<Message> result = new ArrayList<>();
        String select =
                "SELECT user.userName, message.text " +
                "FROM user, message " +
                "WHERE user.userId = message.senderUserId " +
                "ORDER BY date DESC, time DESC " +
                "LIMIT 15";
        Connection connection = connectionPool.get();
        try {
            PreparedStatement preparedSelect = connection.prepareStatement(select);
            ResultSet resultSet = preparedSelect.executeQuery();
            while (resultSet.next()) {
                //???!!! create messages
                String userName = resultSet.getString(1);
                String text = resultSet.getString(2);
                RegularMessageBody regBody = new RegularMessageBody(userName, text);
                result.add(new Message(MessageType.REGULAR, regBody));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionPool.put(connection);
        }
        Collections.reverse(result);
        return result;
    }

    @Override
    public void saveMessage(Message message) {
        Connection connection = connectionPool.get();
        try {
            String update = "INSERT INTO chatdb.message" +
                    "(date, time, senderUserId, isForAll, text)" +
                    " VALUES (?, ?, (SELECT userId FROM chatdb.user WHERE userName = ?), ?, ?)";
            PreparedStatement preparedUpdate = connection.prepareStatement(update);

            java.util.Date now = new java.util.Date();
            preparedUpdate.setDate(1, new java.sql.Date(now.getTime()));
            preparedUpdate.setTime(2, new java.sql.Time(now.getTime()));

            RegularMessageBody messageBody = (RegularMessageBody) message.getBody(); //???!!!
            preparedUpdate.setString(3, messageBody.getUserName()); //???!!!
            preparedUpdate.setString(5, messageBody.getMessage());
            preparedUpdate.setBoolean(4, true);

            preparedUpdate.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionPool.put(connection);
        }
    }

    @Override
    public boolean validateUser(String userName, String password) {
        User foundUser = getUserByUserName(userName);
        if (foundUser == null) {
            return false;
        }
        return BCrypt.checkpw(password, foundUser.getPassword());
    }

}
