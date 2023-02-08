package service;

import main.config.ConfigData;
import model.SiteStatus;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.TreeMap;

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


    public static void executeMultiInsertIntoLemma(String query) throws SQLException {
        String sql = "INSERT INTO lemma(lemma, site_id, frequency)" +
                "VALUES" + query +
                "ON DUPLICATE KEY UPDATE frequency = frequency + 1";
        DBConnection.getConnection().createStatement().executeUpdate(sql);

    }

    public static void executeMultiInsertIntoIndex(String query) throws SQLException {
        String sql = "INSERT INTO `index`(lemma_id, page_id, `rank`)" +
                "VALUES" + query;
        DBConnection.getConnection().createStatement().executeUpdate(sql);

    }

    public static HashMap<String, Long> getLemmasFrequencyForAllSites(TreeMap<String, String> lemmas)
            throws SQLException {

        HashMap<String, Long> lemmasFrequency = new HashMap<>();
        for (String word : lemmas.keySet()) {
            if (!lemmasFrequency.containsKey(lemmas.get(word))) {
                ResultSet rs;
                rs = DBConnection.getConnection().createStatement()
                        .executeQuery("SELECT sum(frequency) as freq_sum FROM lemma WHERE lemma = '"
                                + lemmas.get(word) + "'");
                while (rs.next()) {
                    lemmasFrequency.put(lemmas.get(word), rs.getLong("freq_sum"));
                }
                rs.close();
            }
        }
        return lemmasFrequency;
    }

    public static HashMap<String, Long> getLemmasFrequencyForSite(TreeMap<String, String> lemmas, int id)
            throws SQLException {

        HashMap<String, Long> lemmasFrequency = new HashMap<>();
        for (String word : lemmas.keySet()) {
            if (!lemmasFrequency.containsKey(lemmas.get(word))) {
                ResultSet rs;
                rs = DBConnection.getConnection().createStatement()
                        .executeQuery("SELECT frequency FROM lemma WHERE lemma = '" + lemmas.get(word)
                                + "' AND site_id = '" + id + "'");
                while (rs.next()) {
                    lemmasFrequency.put(lemmas.get(word), rs.getLong("frequency"));
                }
                rs.close();
            }
        }
        return lemmasFrequency;
    }

    public static StringBuilder getPages(String lemma, StringBuilder values, StringBuilder sites) throws SQLException {
        StringBuilder newValues = new StringBuilder();
        ResultSet rs = values.isEmpty() ? DBConnection.getConnection().createStatement()
                .executeQuery("SELECT DISTINCT(ind.page_id) FROM `index` ind, lemma l WHERE " +
                        "l.lemma = '" + lemma + "' AND l.id = ind.lemma_id AND l.site_id in (" + sites + ")") :
                DBConnection.getConnection().createStatement()
                .executeQuery("SELECT DISTINCT(ind.page_id) FROM `index` ind, lemma l WHERE " +
                        "l.lemma = '" + lemma + "' AND l.id = ind.lemma_id AND ind.page_id in (" +
                        values.toString() + ")");
        while (rs.next()) {
            newValues.append((newValues.length() == 0 ? "" : ",") + "'" + rs.getInt("page_id") + "'");
        }
        rs.close();
        return newValues;
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
