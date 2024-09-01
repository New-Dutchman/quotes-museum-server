package com.quotes_museum.backend.models.quotes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Random;

@RequiredArgsConstructor
@Component
public class QuotesRepository{

    public enum CoreTables {owners, attrs, features}

    @Autowired
    final private DataSource dataSource;

    public List<String> getCoreTable(CoreTables table) throws SQLException {

        String tableName, column;

        switch (table) {
            case owners -> {
                tableName = "owners";
                column = "owner";
            }
            case attrs -> {
                tableName = "attrs";
                column = "attr";
            }
            case features -> {
                tableName = "features";
                column = "feature";
            }
            default -> throw new UnsupportedOperationException("Dont know that type of table: " + table);
        }

        List<String> values = new ArrayList<>();

        String query = "SELECT " + column + " FROM " + tableName + ";";

        Connection connection = dataSource.getConnection();

        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery(query);

        connection.close();

        while(resultSet.next()){
            values.add(resultSet.getString(column));
        }

        return values;
    }
    
    public List<QuotesDTO> getOwnerQuotes(String owner) throws SQLException {

        Connection connection = dataSource.getConnection();

        Statement statement = connection.createStatement();
        assert !owner.contains("'");
        String query = "SELECT * FROM get_list_owner('" + owner + "');";

        ResultSet resultSet = statement.executeQuery(query);
        connection.close();

        return getQuotesDTOS(resultSet);
    }
    
    public List<QuotesDTO> getFavQuotes(String user) throws SQLException {
        Connection connection = dataSource.getConnection();

        Statement statement = connection.createStatement();

        String query = "SELECT * FROM get_favorite_quotes('" + user + "');";

        ResultSet resultSet = statement.executeQuery(query);
        connection.close();

        return getQuotesDTOS(resultSet);
    }
    public List<QuotesDTO> getAddedQuotes(String user) throws SQLException {
        Connection connection = dataSource.getConnection();

        Statement statement = connection.createStatement();

        String query = "SELECT * FROM get_added_quotes('" + user + "');";

        ResultSet resultSet = statement.executeQuery(query);
        connection.close();

        return getQuotesDTOS(resultSet);
    }

    public boolean addFavouriteQuote(String owner, int quoteId){
        try {
            Connection connection = dataSource.getConnection();

            String query = "SELECT add_favorite_quote(?, ?);";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, owner);
            preparedStatement.setInt(2, quoteId);

            preparedStatement.executeQuery();
            connection.close();

            return true;
        } catch (SQLException e) {
            System.out.println("exception in sql while adding cite: " + e.getMessage());
            return false;
        }
    }

    public int removeFavouriteQuote(String owner, int quoteId){
        try {
            Connection connection = dataSource.getConnection();

            String query = """
                            DELETE FROM internal.favorite_quotes\s
                            WHERE quote_id = ? AND user_id =\s
                                                      (SELECT user_id FROM internal.users WHERE user_name = ?);
                            """;

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, quoteId);
            preparedStatement.setString(2, owner);

            int res = preparedStatement.executeUpdate();
            connection.close();

            return res;
        } catch (SQLException e) {
            System.out.println("exception in sql while adding cite: " + e.getMessage());
            return -1;
        }
    }

    public boolean isUpdatePermitted(QuotesDTO quote, String username){
        try {
            Connection connection = dataSource.getConnection();
            String query = "SELECT user_name from internal.users " +
            "WHERE user_id IN(SELECT who_added FROM quotes WHERE quote_id = " + quote.getId() + ");";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            if (!resultSet.next()) {
                query = "SELECT role_id FROM internal.users\n" +
                        "WHERE user_name = '" + username + "';";

                resultSet = statement.executeQuery(query);
                resultSet.next();

                return resultSet.getInt("role_id") == 3;
            }

            connection.close();
            return resultSet.getString("user_name").equals(username);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean isUpdatePermitted(int id, String username){
        try {
            Connection connection = dataSource.getConnection();
            String query = "SELECT user_name from internal.users " +
                    "WHERE user_id IN(SELECT who_added FROM quotes WHERE quote_id = " + id + ");";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            resultSet.next();
            connection.close();

            System.out.println(resultSet.getString("user_name"));
            System.out.println(resultSet.getString("user_name") == null);

            return resultSet.getString("user_name").equals(username);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean updateQuote(QuotesDTO quote) {
        try {
            Connection connection = dataSource.getConnection();

            String query;
            ResultSet resultSet;
            query = "SELECT update_quote(?, ?, ?, ?)";
            PreparedStatement updateQuoteStatement = connection.prepareStatement(query);

            updateQuoteStatement.setInt(1, quote.getId());
            updateQuoteStatement.setString(2, quote.getQuote());
            updateQuoteStatement.setBoolean(3, quote.isObscene());
            updateQuoteStatement.setString(4, quote.getOwner());

            resultSet = updateQuoteStatement.executeQuery();
            connection.close();

            resultSet.next();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean updateAttrs(QuotesDTO quote) {
        try {
            Connection connection = dataSource.getConnection();
            ResultSet resultSet;

            String query;
            query = "SELECT update_attrs(?, ?)";
            PreparedStatement updateAttrsStatement = connection.prepareStatement(query);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonAttrs = mapper.createObjectNode();
            for (Map.Entry<String, String> entry : quote.getAttrs().entrySet()) {
                jsonAttrs.put(entry.getKey(), entry.getValue());
            }

            updateAttrsStatement.setInt(1, quote.getId());
            updateAttrsStatement.setObject(2, jsonAttrs.toString(), java.sql.Types.OTHER);

            resultSet = updateAttrsStatement.executeQuery();
            connection.close();

            resultSet.next();
            return resultSet.getBoolean(1);

        }catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean updateFeatures(QuotesDTO quote) {
        try {
            ResultSet resultSet;
            String query;
            query = "SELECT update_features(?, ?)";
            Connection connection = dataSource.getConnection();
            PreparedStatement updateFeaturesStatement = connection.prepareStatement(query);

            String[] featuresArray = quote.getFeatures();
            Array featuresSqlArray = connection.createArrayOf("text", featuresArray);


            updateFeaturesStatement.setInt(1, quote.getId());
            updateFeaturesStatement.setArray(2, featuresSqlArray);

            resultSet = updateFeaturesStatement.executeQuery();
            connection.close();

            resultSet.next();
            return resultSet.getBoolean(1);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

    }

    public boolean insertQuote(QuotesDTO quote, String username) {
        try {
            Connection connection = dataSource.getConnection();

            String query = "SELECT insert_quote(?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            String[] featuresArray = quote.getFeatures();
            Array featuresSqlArray = connection.createArrayOf("text", featuresArray);

            // attrs to JSON
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonAttrs = mapper.createObjectNode();
            for (Map.Entry<String, String> entry : quote.getAttrs().entrySet()) {
                jsonAttrs.put(entry.getKey(), entry.getValue());
            }

            preparedStatement.setString(1, quote.getQuote());
            preparedStatement.setBoolean(2, quote.isObscene());
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, quote.getOwner());
            preparedStatement.setArray(5, featuresSqlArray);
            preparedStatement.setObject(6, jsonAttrs.toString(), java.sql.Types.OTHER);

            ResultSet resultSet = preparedStatement.executeQuery();
            connection.close();

            resultSet.next();
            if (resultSet.getString(1).equals("process ended good")) return true;

            System.out.println(resultSet.getString(1));
            throw new SQLException("Strange answer: " + resultSet.getString(1));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean deleteQuote(int id){
        try{
            Connection connection = dataSource.getConnection();

            String query = "DELETE FROM quotes WHERE quote_id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, id);

            int res = preparedStatement.executeUpdate();

            connection.close();

            return res == 1;
        }catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean addOwner(String owner, String description, String username){
        try {
            Connection connection = dataSource.getConnection();

            String query = """
                    INSERT INTO owners (owner, description, who_added)
                    VALUES
                    (?, ?, (SELECT user_id AS who_added FROM internal.users WHERE user_name = ?));
                    """;

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, owner);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, username);

            int res = preparedStatement.executeUpdate();
            connection.close();
            return res == 1;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<QuotesDTO> searchQuote(String quote) throws SQLException {
        Connection connection = dataSource.getConnection();

        String query = "SELECT * FROM search_quote_by_quotation(?);";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, quote);

        ResultSet resultSet = preparedStatement.executeQuery();
        connection.close();

        return getQuotesDTOS(resultSet);
    }

    public List<QuotesDTO> searchQuote(String quote, List<String> features) throws SQLException {
        Connection connection = dataSource.getConnection();

        Array featuresSqlArray = connection.createArrayOf("text", features.toArray());

        String query = "SELECT * FROM search_quote_by_filter(?, ?);";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, quote);
        preparedStatement.setArray(2, featuresSqlArray);

        ResultSet resultSet = preparedStatement.executeQuery();
        connection.close();

        return getQuotesDTOS(resultSet);
    }

    public List<QuotesDTO> searchQuoteNegative(String quote, List<String> features) throws SQLException {
        Connection connection = dataSource.getConnection();

        Array featuresSqlArray = connection.createArrayOf("text", features.toArray());

        String query = "SELECT * FROM search_quote_by_filter_negative(?, ?);";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, quote);
        preparedStatement.setArray(2, featuresSqlArray);

        ResultSet resultSet = preparedStatement.executeQuery();
        connection.close();

        return getQuotesDTOS(resultSet);
    }

    public QuotesDTO getRandomQuote() throws SQLException {
        Connection connection = dataSource.getConnection();

        String getCount = "SELECT COUNT(quotation) FROM quotes;";
        Statement statement = connection.createStatement();

        ResultSet countSet = statement.executeQuery(getCount);
        countSet.next();
        int count = countSet.getInt(1);
        Random rand = new Random();
        int n = rand.nextInt(count) + 1;

        String query = "SELECT * FROM get_quote_by_number(?);";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setInt(1, n);

        ResultSet resultSet = preparedStatement.executeQuery();
        connection.close();

        return getQuotesDTOS(resultSet).get(0);
    }

    private List<QuotesDTO> getQuotesDTOS(ResultSet resultSet) throws SQLException {
        List<QuotesDTO> quotes = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        while(resultSet.next()) {
            int id = resultSet.getInt("id");
            String quote = resultSet.getString("quotation");
            boolean isObscene = resultSet.getBoolean("is_obscene");
            int cites = resultSet.getInt("cites");
            java.sql.Array f = resultSet.getArray("features");
            String[] features = (String[])f.getArray();
            String attributesJson = resultSet.getString("atributes");
            Map<String, String> attrs;

            try {
                attrs = objectMapper.readValue(attributesJson, new TypeReference<>() {
                });
            }catch (JsonProcessingException e) {
                throw new SQLException("yo, there's error with retrieving attributes column, most likely its not json");
            }
            String owner = resultSet.getString("owner");

            quotes.add( new QuotesDTO(id, quote, isObscene, cites, features, attrs, owner));

        }
        return quotes;
    }

}
