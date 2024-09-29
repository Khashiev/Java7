package edu.school21.orm.app;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edu.school21.orm.models.User;

import java.sql.SQLException;

public class Program {
    public static void main(String[] args) {
        HikariConfig config = new HikariConfig("/db.properties");
        OrmManager manager = new OrmManager(new HikariDataSource(config));

        try {
            manager.init();
            User user1 = new User(1L, "Leo", "Messi", 25);
            manager.save(user1);
            user1.setAge(36);
            manager.update(user1);

            User user2 = manager.findById(1L, User.class);
            System.out.println(user2);
        } catch (SQLException | IllegalAccessException e) {
            System.out.println(e.getMessage());
        }
    }
}
