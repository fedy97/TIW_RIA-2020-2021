
package it.polimi.tiw.utils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryExecutor {

    private static final Logger log = LoggerFactory.getLogger(QueryExecutor.class.getSimpleName());

    private final Connection    con;

    public QueryExecutor(Connection connection) {

        this.con = connection;
    }

    public <O> List<O> select(String query, Map<String, Object> param, Class<O> clazz) throws SQLException {

        AtomicReference<String> finalQuery = new AtomicReference<>(query);
        List<O> extractedEntities = new ArrayList<>();

        if (param.keySet().stream().filter(key -> query.contains(":" + key)).count() != param.size())
            throw new SQLException("No exact match between the provided parameters and query");

        finalQuery.set(query);
        param.forEach((name, value) -> {
            if (value instanceof String) finalQuery.set(finalQuery.get().replace(":" + name, "'" + value + "'"));
            else {
                finalQuery.set(finalQuery.get().replace(":" + name, value.toString()));
            }
        });

        log.debug(finalQuery.get());
        try (PreparedStatement preparedStatement = con.prepareStatement(finalQuery.get());
                ResultSet result = preparedStatement.executeQuery()) {

            while (result.next()) {
                O record = clazz.getDeclaredConstructor().newInstance();
                Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {

                    try {
                        if (!field.isAnnotationPresent(Ignore.class)) {
                            field.setAccessible(true);
                            field.set(record, result.getString(camelToSnake(field.getName())));
                            field.setAccessible(false);
                        }
                    } catch (IllegalAccessException | SQLException e) {
                        log.warn("Could not retrieve value of column {}", field.getName());
                    }
                });
                extractedEntities.add(record);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return extractedEntities;
    }

    public <I> boolean insert(String tableName, I entity) {

        List<Field> fields = extractObjectFields(entity);
        String query = buildInsertQuery(tableName, fields);

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            prepareUpdateQuery(fields, entity, preparedStatement);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }

        return false;
    }

    public <I> boolean update(String tableName, I entity) throws IllegalAccessException {

        List<Field> fields = extractObjectFields(entity);
        String query = buildUpdateQuery(tableName, entity);
        Field idField = extractIdField(entity);

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            AtomicInteger count = prepareUpdateQuery(fields, entity, preparedStatement);
            preparedStatement.setString(count.incrementAndGet(), (String) idField.get(entity));
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private <I> AtomicInteger prepareUpdateQuery(List<Field> fields, I entity, PreparedStatement preparedStatement) {

        AtomicInteger count = new AtomicInteger();

        fields.forEach(field -> {
            try {
                field.setAccessible(true);
                preparedStatement.setString(count.incrementAndGet(), (String) field.get(entity));
                field.setAccessible(false);
            } catch (IllegalAccessException | SQLException e) {
                e.printStackTrace();
            }
        });
        return count;
    }

    private String buildInsertQuery(String tableName, List<Field> fields) {

        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tableName).append(" (");

        fields.forEach(field -> sb.append(camelToSnake(field.getName())).append(","));
        sb.deleteCharAt(sb.length() - 1).append(") VALUES (");

        fields.forEach(field -> sb.append("?,"));
        sb.deleteCharAt(sb.length() - 1).append(");");
        return sb.toString();
    }

    private <I> String buildUpdateQuery(String tableName, I newRecord) {

        List<Field> fields = extractObjectFields(newRecord);
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(tableName).append(" SET ");

        String idFieldName = extractIdField(newRecord).getName();

        fields.stream().filter(field -> !idFieldName.equals(field.getName())).forEach(field -> {
            try {
                field.setAccessible(true);
                if (field.get(newRecord) != null) sb.append(camelToSnake(field.getName())).append(" = ?, ");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        sb.deleteCharAt(sb.length() - 1).append(" WHERE ").append(camelToSnake(idFieldName)).append(" = ?;");

        return sb.toString();
    }

    private <I> Field extractIdField(I entity) {

        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.getName().toLowerCase().contains("id")).findFirst().get();
    }

    private List<Field> extractObjectFields(Object o) {

        return Arrays.asList(o.getClass().getDeclaredFields());
    }

    private String camelToSnake(String str) {

        // Empty String
        String result = "";

        // Append first character(in lower case)
        // to result string
        char c = str.charAt(0);
        result = result + Character.toLowerCase(c);

        // Tarverse the string from
        // ist index to last index
        for (int i = 1; i < str.length(); i++) {

            char ch = str.charAt(i);

            // Check if the character is upper case
            // then append '_' and such character
            // (in lower case) to result string
            if (Character.isUpperCase(ch)) {
                result = result + '_';
                result = result + Character.toLowerCase(ch);
            }

            // If the character is lower case then
            // add such character into result string
            else {
                result = result + ch;
            }
        }

        return result;
    }

}
