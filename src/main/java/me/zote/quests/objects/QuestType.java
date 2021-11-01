package me.zote.quests.objects;

import me.zote.quests.trackers.*;

public enum QuestType {

    KILL(KillTracker.class),
    MOVE(MoveTracker.class),
    BREAK(BreakTracker.class),
    PLACE(PlaceTracker.class),
    COMMAND(CommandTracker.class);

    private final Class<? extends QuestTracker> trackerClass;

    QuestType(Class<? extends QuestTracker> trackerClass) {
        this.trackerClass = trackerClass;
    }

    public Class<? extends QuestTracker> trackerClass() {
        return trackerClass;
    }

}
