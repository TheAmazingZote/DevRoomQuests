package me.zote.quests.objects;

import me.zote.quests.trackers.QuestTracker;

import java.util.Set;
import java.util.UUID;

public record ActiveQuest(Quest quest, QuestTracker tracker, Set<UUID> completed) {

}
