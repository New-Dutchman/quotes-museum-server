package com.quotes_museum.backend.models.users;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class UsersRepository {

    @Autowired
    final private DataSource dataSource;

    public boolean auth(String user, String password) throws SQLException {

        Connection connection = dataSource.getConnection();

        String query = "SELECT check_auth(?, ?);";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, user);
        preparedStatement.setString(2, password);

        ResultSet resultSet = preparedStatement.executeQuery();

        resultSet.next();

        String res = resultSet.getString(1);

        connection.close();

        return Objects.equals(res, "yep");
    }

    public UserDTO findByName(String name) throws SQLException {

        Connection connection = dataSource.getConnection();

        String query = """
                SELECT internal.users.user_name, internal.users.passwd, internal.roles.role FROM internal.users
                NATURAL JOIN internal.roles
                WHERE internal.users.user_name = ?;\s
                """;

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, name);

        ResultSet resultSet = preparedStatement.executeQuery();

        resultSet.next();

        String username = resultSet.getString("user_name");
        String hashedPassword = resultSet.getString("passwd");
        String role = resultSet.getString("role");

        UserDTO user = new UserDTO(username,hashedPassword, role);

        connection.close();

        return user;

    }

    public int addUser(UserDTO user) throws SQLException {
        Connection connection = dataSource.getConnection();

        String query = """
                INSERT INTO internal.users (user_name, passwd, role_id)
                VALUES (?, ?, (SELECT role_id FROM internal.roles WHERE role = ?));""";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getHashedPassword());
        preparedStatement.setString(3, user.getRole());


        int res = preparedStatement.executeUpdate();

        connection.close();

        return res;
    }
}
