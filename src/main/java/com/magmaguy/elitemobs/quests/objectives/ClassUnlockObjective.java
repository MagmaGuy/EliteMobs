package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.io.Serial;

/** Observes the existing class profile; only the class trial awards the unlock. */
public final class ClassUnlockObjective extends Objective {
    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final String classId;
    @Getter
    private final String npcFilename;

    public ClassUnlockObjective(String classId, String className, String npcFilename) {
        super(1, className);
        this.classId = classId;
        this.npcFilename = npcFilename;
    }

    private boolean isUnlocked(Player player) {
        if (classId == null || classId.isBlank() || !ExperimentalCombatModule.isInitialized()) return false;
        return ExperimentalCombatModule.get().profile(player.getUniqueId())
                .map(profile -> profile.forms().get(classId))
                .map(form -> form.unlocked()).orElse(false);
    }

    @Override
    public void progressNonlinearObjective(QuestObjectives objectives, Player player) {
        if (!isObjectiveCompleted() && isUnlocked(player)) progressObjective(objectives);
    }

    private static void refresh(Quest quest, Player player) {
        for (Objective objective : quest.getQuestObjectives().getObjectives())
            if (objective instanceof ClassUnlockObjective)
                objective.progressNonlinearObjective(quest.getQuestObjectives(), player);
    }

    /** Also reconciles quests accepted before profile loading or restored after reconnecting. */
    public static void refresh(Player player) {
        if (!PlayerData.isInMemory(player.getUniqueId())) return;
        for (Quest quest : List.copyOf(PlayerData.getQuests(player.getUniqueId())))
            refresh(quest, player);
    }

    public static final class Events implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onAccept(QuestAcceptEvent event) {
            refresh(event.getQuest(), event.getPlayer());
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onComplete(QuestCompleteEvent event) {
            for (Objective objective : event.getQuest().getQuestObjectives().getObjectives())
                if (objective instanceof ClassUnlockObjective unlock && !unlock.isUnlocked(event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }
        }
    }
}
