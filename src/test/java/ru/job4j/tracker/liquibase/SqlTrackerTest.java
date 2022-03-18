package ru.job4j.tracker.liquibase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.job4j.tracker.model.Item;
import ru.job4j.tracker.store.SqlTracker;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * test
 */
public class SqlTrackerTest {

    public static Connection connection;

    @BeforeClass
    public static void initConnection() {
        try (InputStream in = SqlTrackerTest.class.getClassLoader().getResourceAsStream("test.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterClass
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @After
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from items")) {
            statement.execute();
        }
    }

    @Test
    public void whenSaveItemAndFindByGeneratedIdThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item =  tracker.add(new Item("item"));
        assertThat(tracker.findById(item.getId()), is(item));
    }

    @Test
    public void whenSaveItemAndFindByNameThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item =  tracker.add(new Item("item"));
        Item item1 = tracker.add(new Item("item"));
        assertThat(tracker.findByName("item"), is(List.of(item, item1)));
    }

    @Test
    public void whenReplaceItemAndFindByNameThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item =  tracker.add(new Item("item"));
        Item item1 = new Item("test");
        tracker.replace(item.getId(), item1);
        assertThat(tracker.findByName("test").get(0).getName(), is("test"));
    }

    @Test
    public void whenFindAllItemAndFindSizeThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item =  tracker.add(new Item("item"));
        Item item1 = tracker.add(new Item("item"));
        assertThat(tracker.findAll().size(), is(2));
        assertThat(tracker.findAll(), is(List.of(item, item1)));
    }

    @Test
    public void whenDellItemAndFindSizeThenMustBeOne() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item =  tracker.add(new Item("item"));
        Item item1 = tracker.add(new Item("test"));
        assertThat(tracker.delete(item1.getId()), is(true));
        assertThat(tracker.findAll().size(), is(1));
        assertThat(tracker.findByName("item").get(0).getName(), is("item"));
        assertThat(tracker.findAll(), is(List.of(item)));
    }
}
