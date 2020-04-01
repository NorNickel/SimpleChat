package best.aog.chat.server.service;

import best.aog.chat.server.model.User;
import best.aog.chat.server.model.messages.Message;
import best.aog.chat.server.model.messages.MessageType;
import best.aog.chat.server.model.messages.client.RegularMessageBody;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDao implements UserService {

    private final static int NUMBER_OF_CONNECTIONS_START = 5;
    private final static String BSCRYPT_SALT = BCrypt.gensalt(10);

    // JDBC URL, username and password of MySQL server
    private static final String url = "jdbc:MySQL://localhost:3306/chatdb?serverTimezone=UTC";
    private static final String user = "root";
    private static final String password = "12345";

    private static volatile UserDao instance;

    private ConnectionPool connectionPool = new ConnectionPool();

    private Logger logger;

    private UserDao() {
        logger = Logger.getLogger(UserDao.class.getName());
        logger.log(Level.INFO, "Try to connect to DB: " + url);
        connectionPool.createConnectionPool();
        logger.log(Level.INFO, "Connected to DB: " + url);
    }

    public static UserDao getInstance() {
        if (instance == null) {
            synchronized (UserDao.class) {
                if (instance == null) {
                    instance = new UserDao();
                }
            }
        }
        return instance;
    }

    @Override
    public synchronized boolean addUser(User user) {
        boolean result = false;
        try (Connection connection = connectionPool.get()) {
            String insert = "INSERT INTO chatdb.user (login, hashPassword) VALUES (?, ?)";
            PreparedStatement preparedInsert = connection.prepareStatement(insert);
            preparedInsert.setString(1, user.getLogin());
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
    public synchronized boolean removeUser(User user) {
        return false;
    }

    @Override
    public User getUserByLogin(String login) {
        User user = null;
        Connection connection = connectionPool.get();
        try {
            String select = "SELECT login, hashPassword FROM chatdb.user WHERE login = ?";
            PreparedStatement preparedSelect = connection.prepareStatement(select);
            preparedSelect.setString(1, login);
            ResultSet rs = preparedSelect.executeQuery();
            while (rs.next()) {
                user = new User();
                user.setLogin(rs.getString(1));
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
                "SELECT user.login, message.text " +
                "FROM user, message " +
                "WHERE user.userId = message.senderUserId " +
                "ORDER BY date, time DESC " +
                "LIMIT 10";
        Connection connection = connectionPool.get();
        try {
            PreparedStatement preparedSelect = connection.prepareStatement(select);
            ResultSet resultSet = preparedSelect.executeQuery();
            while (resultSet.next()) {
                //???!!! create messages
                String login = resultSet.getString(1);
                User newUser = new User(login, "");
                String text = resultSet.getString(2);
                RegularMessageBody regBody = new RegularMessageBody(newUser, text);
                result.add(new Message(MessageType.REGULAR_MESSAGE, regBody));
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
                    " VALUES (?, ?, (SELECT userId FROM chatdb.user WHERE login = ?), ?, ?)";
            PreparedStatement preparedUpdate = connection.prepareStatement(update);

            java.util.Date now = new java.util.Date();
            preparedUpdate.setDate(1, new java.sql.Date(now.getTime()));
            preparedUpdate.setTime(2, new java.sql.Time(now.getTime()));

            RegularMessageBody messageBody = (RegularMessageBody) message.getMessageBody(); //???!!!
            preparedUpdate.setString(3, messageBody.getUser().getLogin()); //???!!!
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
    public boolean validateUser(User user) {
        User foundUser = getUserByLogin(user.getLogin());
        if (foundUser == null) {
            return false;
        }
        return BCrypt.checkpw(user.getPassword(), foundUser.getPassword());
    }

}
