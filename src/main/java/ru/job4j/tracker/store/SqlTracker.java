package ru.job4j.tracker.store;

import ru.job4j.tracker.model.Item;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SqlTracker implements Store, AutoCloseable {

    private Connection cn;

    public void init() {
        try (InputStream in = SqlTracker.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            cn = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Item getItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        Timestamp timestamp = rs.getTimestamp("created");
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setCreated(timestamp.toLocalDateTime());
        return item;
    }

    @Override
    public void close() throws Exception {
        if (cn != null) {
            cn.close();
        }
    }

    @Override
    public Item add(Item item) {
        try (PreparedStatement statement =
                     this.cn.prepareStatement("insert into items(name, created) values (?, ?)",
                             Statement.RETURN_GENERATED_KEYS)) {

            Timestamp timestampFromLDT = Timestamp.valueOf(LocalDateTime.now());
            statement.setString(1, item.getName());
            statement.setTimestamp(2, timestampFromLDT);
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public boolean replace(int id, Item item) {
        boolean res = false;
        try (PreparedStatement st =
                     this.cn.prepareStatement("update items set name = (?) , created = (?) where  id = (?)")) {

            Timestamp timestampFromLDT = Timestamp.valueOf(LocalDateTime.now());
            st.setString(1, item.getName());
            st.setTimestamp(2, timestampFromLDT);
            st.setInt(3, id);
            res = st.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return res;
    }

    @Override
    public boolean delete(int id) {
        boolean res = false;
        try (PreparedStatement st =
                     this.cn.prepareStatement("delete from items where id = (?)")) {
            st.setInt(1, id);
            res = st.executeUpdate() > 0;
        } catch (Exception e) {
        e.printStackTrace();
    }
        return res;
    }

    @Override
    public List<Item> findAll() {
        Item item;
        List<Item> items = new ArrayList<>();
        try (PreparedStatement st =
                     this.cn.prepareStatement("select * from items")) {
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    item = getItem(rs);
                    items.add(findById(item.getId()));
                }
                st.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return items;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return items;
    }

    @Override
    public List<Item> findByName(String key) {
        Item item;
        List<Item> items = new ArrayList<>();
        try (PreparedStatement st =
                     this.cn.prepareStatement("select * from items where name = (?)")) {
            st.setString(1, key);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    item = getItem(rs);
                    items.add(findById(item.getId()));
                }
                st.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return items;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return items;
    }

    @Override
    public Item findById(int id) {
        Item item = new Item();
        try (PreparedStatement st =
                     this.cn.prepareStatement("select * from items where id = (?)")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        item = getItem(rs);
                    } else {
                        item = null;
                    }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return item;
    }
}
