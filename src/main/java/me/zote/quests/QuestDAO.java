package me.zote.quests;

import co.aikar.idb.*;
import me.zote.quests.objects.ActiveQuest;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.StringJoiner;

public class QuestDAO {

    private final Quests quests;

    public QuestDAO(Quests quests) {
        this.quests = quests;
        configureDB();
    }

    private void configureDB() {
        Configuration config = quests.getConfig();

        String user = config.getString("user", "root");
        String pass = config.getString("pass", "admin123");
        String host = config.getString("host", "127.0.0.1");
        String db = config.getString("db", "quests");
        int port = config.getInt("port", 3306);

        DatabaseOptions options = DatabaseOptions
                .builder()
                .poolName(quests.getDescription().getName() + " DB")
                .logger(quests.getLogger())
                .mysql(user, pass, db, host + ":" + port)
                // Data Source for paper
                .dataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource")
                .build();
        PooledDatabaseOptions poolOptions = PooledDatabaseOptions
                .builder()
                .options(options)
                .build();

        HikariPooledDatabase hdb = new HikariPooledDatabase(poolOptions);
        DB.setGlobalDatabase(hdb);

        try {
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS quests (" +
                    "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                    "trackers MEDIUMTEXT" +
                    ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadData(Player player) {
        String id = player.getUniqueId().toString();

        try {
            DbRow result = DB.getFirstRow("SELECT * FROM quests WHERE uuid = ?;", id);

            if (result == null)
                return;

            String trackers = result.getString("trackers", null);

            if (trackers == null)
                return;

            String[] arr = trackers.split(",");

            for (String tracker : arr) {
                String[] info = tracker.split("=");
                String name = info[0];
                double progress = Double.parseDouble(info[1]);
                quests.activeQuests().stream()
                        .filter(aq -> aq.quest().name().equals(name))
                        .findFirst()
                        .ifPresent(activeQuest -> activeQuest.tracker().setProgression(player, progress));
            }

        } catch (SQLException throwables) {
            quests.getLogger().severe("Error saving data of: " + player.getName());
        }
    }

    public void saveData(Player player) {
        String id = player.getUniqueId().toString();
        StringJoiner joiner = new StringJoiner(",");

        for (ActiveQuest activeQuest : quests.activeQuests()) {
            double progress = activeQuest.tracker().getProgress(player);
            String value = activeQuest.quest().name() + "=" + progress;
            joiner.add(value);
        }

        try {
            DB.executeUpdate("INSERT INTO quests (uuid, trackers) VALUES (?, ?) ON DUPLICATE KEY UPDATE trackers = VALUES(trackers);", id, joiner.toString());
        } catch (SQLException throwables) {
            quests.getLogger().severe("Error saving data of: " + player.getName());
        }
    }

}
