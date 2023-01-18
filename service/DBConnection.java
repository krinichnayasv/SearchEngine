package service;

import main.config.ConfigData;
import model.SiteStatus;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection;
    private static ConfigData configData = new ConfigData();
    private static String dbUserConfig = configData.getUsername();
    private static String dbPassConfig = configData.getPassword();
    private static String urlConfig = configData.getUrl().concat("?useSSL=false&autoReconnect=true");

    public static Connection getConnection() {

        if (connection == null) {

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try {
                connection = DriverManager.getConnection(urlConfig, dbUserConfig, dbPassConfig);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }


    public static void executeMultiInsertIntoLemma (String query) throws SQLException {
        String sql = "INSERT INTO lemma(lemma, site_id, frequency)" +
                "VALUES" + query +
                "ON DUPLICATE KEY UPDATE frequency = frequency + 1";
        DBConnection.getConnection().createStatement().executeUpdate(sql);

    }

    public static void executeMultiInsertIntoIndex (String query) throws SQLException {
        String sql = "INSERT INTO `index`(lemma_id, page_id, `rank`)" +
                "VALUES" + query;
        DBConnection.getConnection().createStatement().executeUpdate(sql);

    }

    public static void updateSiteOnIndexing(int id) throws SQLException {
        String sql = "UPDATE site set status_time = '" + System.currentTimeMillis()
                + "', status = '" + SiteStatus.INDEXING + "', last_error = '' WHERE id = '" + id + "'";
        System.out.println(sql);
       DBConnection.getConnection().createStatement().executeUpdate(sql);

    }



    public static String getDbUserConfig() {
        return dbUserConfig;
    }

    public static void setDbUserConfig(String dbUserConfig) {
        DBConnection.dbUserConfig = dbUserConfig;
    }

    public static String getDbPassConfig() {
        return dbPassConfig;
    }

    public static void setDbPassConfig(String dbPassConfig) {
        DBConnection.dbPassConfig = dbPassConfig;
    }

    public static String getUrlConfig() {
        return urlConfig;
    }

    public static void setUrlConfig(String urlConfig) {
        DBConnection.urlConfig = urlConfig;
    }

}
