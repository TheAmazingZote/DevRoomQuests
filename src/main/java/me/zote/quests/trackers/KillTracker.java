package me.zote.quests.trackers;

import me.zote.quests.objects.Quest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillTracker extends QuestTracker {

    public KillTracker(Quest quest) {
        super(quest);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();

        if (entity instanceof Player)
            return;

        Player killer = entity.getKiller();

        if (killer == null)
            return;

        if (hasCompleted(killer))
            return;

        EntityType target = getTarget(EntityType.class);

        if (entity.getType() == target)
            increaseProgression(killer, 1);

        if (hasCompleted(killer))
            giveRewards(killer);

    }

}
