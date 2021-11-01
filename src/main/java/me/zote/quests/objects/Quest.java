package me.zote.quests.objects;

import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public record Quest(String name, QuestType type, String target,
                    int amount, List<String> rewards) {

    public void giveRewards(Player who) {
        for (String reward : rewards) {
            String cmd = StrSubstitutor.replace(reward, Map.of("player", who.getName()), "%", "%");
            if (cmd.startsWith("/"))
                cmd = cmd.substring(1);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

}
