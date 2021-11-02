package me.zote.quests;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final QuestDAO quests;

    public PlayerListener(QuestDAO quests) {
        this.quests = quests;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        quests.loadData(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        quests.saveData(event.getPlayer());
    }

}
