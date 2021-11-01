package me.zote.quests.trackers;

import me.zote.quests.objects.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandTracker extends QuestTracker {

    public CommandTracker(Quest quest) {
        super(quest);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (hasCompleted(player))
            return;

        String target = getTarget(s -> s);
        String cmd = event.getMessage();

        if (cmd.equalsIgnoreCase(target))
            increaseProgression(player, 1);

        if (hasCompleted(player))
            giveRewards(player);

    }

}
