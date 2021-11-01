package me.zote.quests.trackers;

import me.zote.quests.objects.Quest;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveTracker extends QuestTracker {

    public MoveTracker(Quest quest) {
        super(quest);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (hasCompleted(player))
            return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ())
            return;

        increaseProgression(player, from.distance(to));

        if (hasCompleted(player))
            giveRewards(player);

    }

}
