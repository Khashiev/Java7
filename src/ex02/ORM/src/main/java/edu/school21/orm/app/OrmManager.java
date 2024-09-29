package edu.school21.orm.app;

import edu.school21.orm.annotations.OrmColumn;
import edu.school21.orm.annotations.OrmColumnId;
import edu.school21.orm.annotations.OrmEntity;
import org.reflections.Reflections;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.StringJoiner;

public class OrmManager {
    private final DataSource dataSource;

    public OrmManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void init() throws SQLException, IllegalArgumentException {
        Reflections reflections = new Reflections("edu.school21.orm.models");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(OrmEntity.class);

        for (Class<?> clazz : classes) {
            String table = clazz.getAnnotation(OrmEntity.class).table();
            String drop = "DROP TABLE IF EXISTS " + table + ';';

            StringJoiner query = new StringJoiner(", ", "CREATE TABLE "
                    + table + "( ", ");");
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(OrmColumnId.class)) {
                    query.add("id BIGSERIAL PRIMARY KEY");
                } else if (field.isAnnotationPresent(OrmColumn.class)) {
                    query.add(getColumn(field));
                }
            }

            Connection connection = dataSource.getConnection();
            connection.createStatement().execute(drop);
            System.out.println(drop);

            connection.createStatement().execute(query.toString());
            System.out.println(query);
        }
    }

    private String getColumn(Field field) throws IllegalArgumentException {
        OrmColumn column = field.getAnnotation(OrmColumn.class);

        if (field.getType().equals(String.class)) {
            return String.format("%s VARCHAR(%d)", column.name(), column.length());
        } else if (field.getType().equals(Integer.class)) {
            return column.name() + " INTEGER";
        } else if (field.getType().equals(Long.class)) {
            return column.name() + " BIGINT";
        } else if (field.getType().equals(Double.class)) {
            return column.name() + " NUMERIC";
        } else if (field.getType().equals(Boolean.class)) {
            return column.name() + " BOOLEAN";
        }

        throw new IllegalArgumentException(field.getType().getSimpleName()
                + " is not supported");
    }

    public void save(Object entity) throws SQLException {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(OrmEntity.class)) {
            throw new IllegalArgumentException();
        }

        String table = clazz.getAnnotation(OrmEntity.class).table();
        StringJoiner columns = new StringJoiner(", ", "INSERT INTO "
                + table + "( ", ") ");
        StringJoiner values = new StringJoiner(", ", "VALUES (", ");");

        Field[] fields = clazz.getDeclaredFields();

        try {
            for (Field field : fields) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(OrmColumnId.class)) {
                    columns.add("id");
                    values.add(String.format("'%s'", field.get(entity)));
                } else if (field.isAnnotationPresent(OrmColumn.class)) {
                    OrmColumn column = field.getAnnotation(OrmColumn.class);
                    columns.add(String.format("%s", column.name()));
                    values.add(String.format("'%s'", field.get(entity)));
                }
            }
        } catch (IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }

        Connection connection = dataSource.getConnection();
        connection.createStatement().execute(columns + values.toString());
        System.out.println(columns + values.toString());
    }

    public void update(Object entity) throws SQLException, IllegalAccessException {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(OrmEntity.class)) {
            throw new IllegalArgumentException();
        }

        String table = clazz.getAnnotation(OrmEntity.class).table();
        StringJoiner query = new StringJoiner(", ", "UPDATE " +
                table + " SET ", " WHERE id = ?;");
        Long id = 0L;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(OrmColumnId.class)) {
                id = (Long) field.get(entity);
                query.add(String.format("id = '%s'", id));
            } else if (field.isAnnotationPresent(OrmColumn.class)) {
                OrmColumn column = field.getAnnotation(OrmColumn.class);
                query.add(String.format("%s = '%s'", column.name(), field.get(entity)));
            }
        }

        Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(query.toString());
        System.out.println(query);
        ps.setLong(1, id);
        if (ps.executeUpdate() != 1) {
            throw new SQLException("unable to update entity");
        }
    }

    public <T> T findById(Long id, Class<T> clazz) throws SQLException {
        ResultSet set = getResultSet(id, clazz);
        Field[] fields = clazz.getDeclaredFields();

        try {
            T object = clazz.newInstance();

            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(OrmColumnId.class)) {
                    field.set(object, id);
                } else if (field.isAnnotationPresent(OrmColumn.class)) {
                    field.set(object, getValue(field, set));
                }
            }

            return object;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }
    }

    private <T> ResultSet getResultSet(Long id, Class<T> clazz)
            throws SQLException, IllegalArgumentException {
        if (!clazz.isAnnotationPresent(OrmEntity.class)) {
            throw new IllegalArgumentException();
        }

        String table = clazz.getAnnotation(OrmEntity.class).table();
        Connection connection = dataSource.getConnection();
        String query = String.format("SELECT * FROM %s WHERE id = '%d'", table, id);
        PreparedStatement ps = connection.prepareStatement(query);
        ResultSet set = ps.executeQuery();
        System.out.println(query);

        if (!set.next()) {
            throw new SQLException("id not found");
        }

        return set;
    }

    private Object getValue(Field field, ResultSet set)
            throws SQLException, IllegalArgumentException {
        String fieldName = field.getAnnotation(OrmColumn.class).name();

        if (field.getType().equals(String.class)) {
            return set.getString(fieldName);
        } else if (field.getType().equals(Integer.class)) {
            return set.getInt(fieldName);
        } else if (field.getType().equals(Long.class)) {
            return set.getLong(fieldName);
        } else if (field.getType().equals(Double.class)) {
            return set.getDouble(fieldName);
        } else if (field.getType().equals(Boolean.class)) {
            return set.getBoolean(fieldName);
        }

        throw new IllegalArgumentException(field.getType().getSimpleName()
                + " is not supported");
    }
}
