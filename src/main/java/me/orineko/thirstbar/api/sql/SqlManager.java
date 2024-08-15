package me.orineko.thirstbar.api.sql;

import me.orineko.thirstbar.ThirstBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SqlManager {

    private final Connection connection;

    public SqlManager(){
        FileConfiguration file = ThirstBar.getInstance().getConfig();
        boolean enable = file.getBoolean("Sql.Enable", false);
        if(!enable){
            connection = null;
            return;
        }
        String host = file.getString("Sql.Host", "");
        String port = file.getString("Sql.Port", "");
        String username = file.getString("Sql.Username", "");
        String password = file.getString("Sql.Password", "");
        String database = file.getString("Sql.Database", "");

        String urlDB = String.format("jdbc:mysql://%s:%s/%s?useSSL=false", host, port, database);
        try {
            this.connection = DriverManager.getConnection(urlDB, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(getConnection() == null) return;
        createTables();
    }

    public void createTables(){
        List<HashMap<String, Object>> tablePlayer =
                executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_NAME = 'player'");
        List<HashMap<String, Object>> tableItem =
                executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_NAME = 'items'");
        if(tablePlayer != null && tablePlayer.isEmpty()){
            execute("CREATE TABLE `player` (" +
                    "`name` VARCHAR(255) NOT NULL , `disable` INT NOT NULL , " +
                    "`thirst` DOUBLE NOT NULL , `max` DOUBLE NOT NULL ,  " +
                    "PRIMARY KEY (`name`))");
        }
        if(tableItem != null && tableItem.isEmpty()){
            execute("CREATE TABLE `items` (" +
                    "`name` VARCHAR(255) NOT NULL , " +
                    "`item` VARCHAR(2000) NOT NULL , `value` DOUBLE NOT NULL ,  `value_percent` DOUBLE NOT NULL , " +
                    "PRIMARY KEY (`name`))");
        }
    }

    public List<HashMap<String, Object>> runGetItems(){
        return executeQuery("SELECT * FROM items");
    }

    public List<HashMap<String, Object>> runGetPlayer(){
        return executeQuery("SELECT * FROM player");
    }

    public int runRemoveAllPlayer(){
        return executeUpdate("DELETE FROM player");
    }

    public double runGetThirstCurrentPlayer(@Nonnull String name){
        List<HashMap<String, Object>> list = executeQuery("SELECT * FROM player WHERE name = ?", name);
        if(list == null || list.isEmpty()) return -1;
        HashMap<String, Object> map = list.stream().filter(v -> v.getOrDefault("thirst", null) != null).findAny().orElse(null);
        if(map == null) return -1;
        return (double) map.getOrDefault("thirst", -1);
    }

    public int runSetDisablePlayer(@Nonnull String name, int disable){
        String sql = "INSERT INTO player (name, disable, thirst, max) " +
                "VALUES (?, ?, 0, 0) " +
                "ON DUPLICATE KEY UPDATE disable = VALUES(disable)";

        return executeUpdate(sql, name, disable);
    }

    public int runSetThirstPlayer(@Nonnull String name, double thirst){
        String sql = "INSERT INTO player (name, disable, thirst, max) " +
                "VALUES (?, 0, ?, 0) " +
                "ON DUPLICATE KEY UPDATE thirst = VALUES(thirst)";

        return executeUpdate(sql, name, thirst);
    }

    public int runSetMaxPlayer(@Nonnull String name, double max){
        String sql = "INSERT INTO player (name, disable, thirst, max) " +
                "VALUES (?, 0, 0, ?) " +
                "ON DUPLICATE KEY UPDATE max = VALUES(max)";

        return executeUpdate(sql, name, max);
    }

    public int runAddItems(@Nonnull String name, @Nonnull ItemStack itemStack, double value, double valuePercent) {
        String serializedItem = ItemSerialization.itemStackArrayToBase64(new ItemStack[]{itemStack});

        String sql = "INSERT INTO items (name, item, value, value_percent) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "item = VALUES(item), " +
                "value = VALUES(value), " +
                "value_percent = VALUES(value_percent)";

        return executeUpdate(sql, name, serializedItem, value, valuePercent);
    }

    public int executeUpdate(String sql, Object... params) {
        if(getConnection() == null) return -1;
        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++)
                statement.setObject(i + 1, params[i]);
            int row = statement.executeUpdate();
            statement.close();
            return row;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

    public boolean execute(String sql, Object... params) {
        if(getConnection() == null) return false;
        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++)
                statement.setObject(i + 1, params[i]);
            boolean check = statement.execute();
            statement.close();
            return check;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    @Nullable
    public List<HashMap<String, Object>> executeQuery(String sql, Object... params) {
        if(getConnection() == null) return null;
        try {
            List<HashMap<String, Object>> resultList = new ArrayList<>();
            PreparedStatement statement = getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++)
                statement.setObject(i + 1, params[i]);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                HashMap<String, Object> row = new HashMap<>();

                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    HashMap<String, Object> column = new HashMap<>();
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    Object columnValue = resultSet.getObject(i);
                    column.put(columnName, columnValue);
                    row.put(columnName, columnValue);
                }

                resultList.add(row);
            }
            resultSet.close();
            return resultList;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public Connection getConnection() {
        return connection;
    }

    public static class ItemSerialization {

        @Nullable
        public static String itemStackArrayToBase64(ItemStack[] items) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

                // Write the size of the inventory
                dataOutput.writeInt(items.length);

                // Save every element in the list
                for (ItemStack item : items) {
                    dataOutput.writeObject(item);
                }

                // Serialize that array
                dataOutput.close();
                return Base64Coder.encodeLines(outputStream.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Nullable
        public static ItemStack[] itemStackArrayFromBase64(String data)  {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                ItemStack[] items = new ItemStack[dataInput.readInt()];

                // Read the serialized inventory
                for (int i = 0; i < items.length; i++) {
                    items[i] = (ItemStack) dataInput.readObject();
                }

                dataInput.close();
                return items;
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
