package me.zote.quests.trackers;

import me.zote.quests.objects.Quest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakTracker extends QuestTracker {

    public BreakTracker(Quest quest) {
        super(quest);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (hasCompleted(player))
            return;

        Block block = event.getBlock();
        Material target = getTarget(Material.class);

        if (block.getType() == target)
            increaseProgression(player, 1);

        if (hasCompleted(player))
            giveRewards(player);

    }

}
