package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;

public class DialogObjective extends Objective {

    @Getter
    private final String targetLocation;
    @Getter
    private final String npcFilename;
    @Getter
    private final List<String> dialog;

    public DialogObjective(String npcFilename, String npcName, String targetLocation, List<String> dialog) {
        super(1, npcName);
        this.targetLocation = targetLocation;
        this.npcFilename = npcFilename;
        this.dialog = dialog;
    }

    public boolean checkProgress(Player player, QuestObjectives questObjectives) {
        if (super.currentAmount >= super.targetAmount) return false;
        progressObjective(questObjectives);
        if (dialog != null && !dialog.isEmpty())
            if (!QuestDialogueBossBarManager.showRawDialogue(player, getObjectiveName(), dialog, null))
                dialog.forEach(player::sendMessage);
        return true;
    }

    /** Consumes an NPC interaction when it advances dialogue, before opening the NPC's menu. */
    public static boolean progressAtNPC(Player player, NPCEntity npcEntity) {
        boolean progressed = false;
        for (Quest quest : List.copyOf(PlayerData.getQuests(player.getUniqueId())))
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (objective instanceof DialogObjective dialogue
                        && dialogue.getNpcFilename().equals(npcEntity.getNPCsConfigFields().getFilename()))
                    progressed |= dialogue.checkProgress(player, quest.getQuestObjectives());
        return progressed;
    }

}
