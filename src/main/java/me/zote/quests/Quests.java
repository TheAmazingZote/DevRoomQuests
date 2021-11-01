package me.zote.quests;

import co.aikar.idb.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.zote.quests.objects.ActiveQuest;
import me.zote.quests.objects.Quest;
import me.zote.quests.objects.QuestType;
import me.zote.quests.trackers.QuestTracker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;

public class Quests extends JavaPlugin implements Listener {

    private final List<ActiveQuest> activeQuests = Lists.newArrayList();
    private final List<Quest> quests = Lists.newArrayList();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        load();
        activeQuests();

        Bukkit.getOnlinePlayers().forEach(this::loadData);
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(this::saveData);
        DB.close();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loadData(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        saveData(event.getPlayer());
    }

    private void loadData(Player player) {
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
                activeQuests.stream()
                        .filter(aq -> aq.quest().name().equals(name))
                        .findFirst()
                        .ifPresent(activeQuest -> activeQuest.tracker().setProgression(player, progress));
            }

        } catch (SQLException throwables) {
            getLogger().severe("Error saving data of: " + player.getName());
        }
    }

    private void saveData(Player player) {
        String id = player.getUniqueId().toString();
        StringJoiner joiner = new StringJoiner(",");

        for (ActiveQuest activeQuest : activeQuests) {
            double progress = activeQuest.tracker().getProgress(player);
            String value = activeQuest.quest().name() + "=" + progress;
            joiner.add(value);
        }

        try {
            DB.executeUpdate("INSERT INTO quests (uuid, trackers) VALUES (?, ?) ON DUPLICATE KEY UPDATE trackers = VALUES(trackers);", id, joiner.toString());
        } catch (SQLException throwables) {
            getLogger().severe("Error saving data of: " + player.getName());
        }

    }

    private void configureDB() {
        Configuration config = getConfig();

        String user = config.getString("user", "root");
        String pass = config.getString("pass", "admin123");
        String host = config.getString("host", "127.0.0.1");
        String db = config.getString("db", "quests");
        int port = config.getInt("port", 3306);

        DatabaseOptions options = DatabaseOptions
                .builder()
                .poolName(getDescription().getName() + " DB")
                .logger(getLogger())
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

    private void load() {
        saveDefaultConfig();
        configureDB();

        File questsFolder = new File(getDataFolder(), "quests");
        if (!questsFolder.exists())
            questsFolder.mkdirs();

        File[] questFiles = questsFolder.listFiles();

        if (questFiles != null) {
            for (File questFile : questFiles) {
                YamlConfiguration questCfg = YamlConfiguration.loadConfiguration(questFile);
                String name = questCfg.getString("name", "Quest");
                String typeName = questCfg.getString("type", "KILL");
                String target = questCfg.getString("target", "BEDROCK");
                int amount = questCfg.getInt("amount", Integer.MAX_VALUE);
                List<String> rewards = questCfg.getStringList("rewards");

                Quest quest = new Quest(name, QuestType.valueOf(typeName), target, amount, rewards);
                List<Quest> registered = Lists.newArrayList();
                registered.add(quest);
                quests.addAll(registered);
            }
        }

    }

    private void activeQuests() {

        for (Quest selected : quests) {
            QuestTracker tracker = createTracker(selected);

            if (tracker == null)
                continue;

            ActiveQuest activated = new ActiveQuest(selected, tracker, Sets.newHashSet());
            getServer().getPluginManager().registerEvents(activated.tracker(), this);
            activeQuests.add(activated);
        }

    }

    private QuestTracker createTracker(Quest quest) {
        try {
            return quest.type()
                    .trackerClass()
                    .getConstructor(Quest.class)
                    .newInstance(quest);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

}
