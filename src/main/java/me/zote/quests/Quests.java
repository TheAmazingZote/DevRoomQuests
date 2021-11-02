package me.zote.quests;

import co.aikar.idb.DB;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.zote.quests.objects.ActiveQuest;
import me.zote.quests.objects.Quest;
import me.zote.quests.objects.QuestType;
import me.zote.quests.trackers.QuestTracker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Quests extends JavaPlugin {

    private final List<ActiveQuest> activeQuests = Lists.newArrayList();
    private final List<Quest> quests = Lists.newArrayList();
    private QuestDAO questDAO;

    @Override
    public void onEnable() {
        load();
        activateQuests();

        Bukkit.getOnlinePlayers().forEach(questDAO::loadData);
        getServer().getPluginManager().registerEvents(new PlayerListener(questDAO), this);
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(questDAO::saveData);
        DB.close();
    }

    public List<ActiveQuest> activeQuests() {
        return activeQuests;
    }

    private void load() {
        saveDefaultConfig();
        questDAO = new QuestDAO(this);

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

    private void activateQuests() {

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
