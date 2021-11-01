package me.zote.quests.trackers;

import com.google.common.collect.Maps;
import me.zote.quests.objects.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class QuestTracker implements Listener {

    private final Map<UUID, Double> trackerMap = Maps.newHashMap();
    private final String target;
    private final double amount;
    private final Quest quest;

    public QuestTracker(Quest quest) {
        this.target = quest.target();
        this.amount = quest.amount();
        this.quest = quest;
    }

    protected <T extends Enum<T>> T getTarget(Class<T> enumClass) {
        return Enum.valueOf(enumClass, target);
    }

    protected <T> T getTarget(Function<String, T> converter) {
        return converter.apply(target);
    }

    protected void increaseProgression(Player player, double amount) {
        UUID id = player.getUniqueId();
        double current = getProgress(player);
        trackerMap.put(id, current + amount);
    }

    public void setProgression(Player player, double amount) {
        UUID id = player.getUniqueId();
        trackerMap.put(id, amount);
    }

    public boolean hasCompleted(Player player) {
        return getProgress(player) >= amount;
    }

    protected void giveRewards(Player player) {
        quest.giveRewards(player);
    }

    public double getProgress(Player player) {
        return trackerMap.getOrDefault(player.getUniqueId(), 0D);
    }

}
