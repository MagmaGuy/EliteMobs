package com.magmaguy.elitemobs.quests.menus;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.items.customloottable.CurrencyCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import com.magmaguy.elitemobs.config.menus.premade.CustomQuestMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.DynamicQuestMenuConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.objectives.DynamicKillObjective;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.utils.BookMaker;
import com.magmaguy.elitemobs.utils.DialogMaker;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.SpigotMessage;
import com.magmaguy.magmacore.util.VersionChecker;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.magmaguy.elitemobs.quests.menus.QuestInventoryMenu.generateInventoryQuestEntries;


public class QuestMenu {

    private QuestMenu() {
    }

    public static void generateCustomQuestMenu(List<CustomQuest> customQuestList, Player player, NPCEntity npcEntity) {
        generateQuestMenu(customQuestList, player, npcEntity);
    }

    public static void generateDynamicQuestMenu(List<DynamicQuest> dynamicQuests, Player player, NPCEntity npcEntity) {
        generateQuestMenu(dynamicQuests, player, npcEntity);
    }

    public static void generateQuestMenu(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        generateQuestMenu(quests, player, npcEntity, false);
    }

    public static void generateQuestMenu(List<? extends Quest> quests, Player player, NPCEntity npcEntity,
                                         boolean returnToPlayerStatus) {
        if (!PlayerData.getUseBookMenus(player.getUniqueId()) || BedrockChecker.isBedrock(player) || DefaultConfig.isOnlyUseBedrockMenus()) {
            generateInventoryQuestEntries(quests, player, npcEntity, returnToPlayerStatus);
        } else if (VersionChecker.serverVersionOlderThan(21,6)) {
            generateBookQuestEntries(quests, player, npcEntity);
        } else {
            generateDialogMenu(quests, player, npcEntity);
        }
    }

    public static void generateDialogMenu(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        DialogMaker.sendQuestMessage(quests, player, npcEntity);
    }

    public static void generateBookQuestEntries(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        BookMaker.generateBook(player, generateBookQuestEntriesComponents(quests, player, npcEntity));
        QuestScreenSession.openedBook(player);
    }

    public static TextComponent[] generateBookQuestEntriesComponents(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        List<TextComponent[]> textComponents = new ArrayList<>();
        int counter = 0;
        for (Quest quest : quests) {
            TextComponent[] iteratedTextComponent = QuestBookMenu.generateQuestEntry(quest, player, npcEntity);
            counter += iteratedTextComponent.length;
            textComponents.add(iteratedTextComponent);
        }

        TextComponent[] allQuests = new TextComponent[counter];

        int counter2 = 0;
        for (TextComponent[] textComponentsArray : textComponents)
            for (TextComponent textComponent : textComponentsArray) {
                allQuests[counter2] = textComponent;
                counter2++;
            }

        return allQuests;
    }


    static TextComponent generateHeader(Quest quest) {
        if (quest instanceof CustomQuest)
            return SpigotMessage.simpleMessage(CustomQuestMenuConfig.getHeaderTextLines().replace("$questName", quest.getQuestName()));
        else
            return SpigotMessage.simpleMessage(DynamicQuestMenuConfig.getHeaderTextLines().replace("$questName", quest.getQuestName()));
    }

    public static List<TextComponent> generateBody(Quest quest) {
        List<TextComponent> body = new ArrayList<>();
        if (quest instanceof CustomQuest)
            for (String splitString : ((CustomQuest) quest).getCustomQuestsConfigFields().getQuestLore())
                body.add(new TextComponent(ChatColorConverter.convert(splitString)));
        else if (quest instanceof DynamicQuest)
            body.add(new TextComponent(DynamicQuestMenuConfig.getDefaultLoreTextLines()
                    .replace("$amount", quest.getQuestObjectives().getObjectives().get(0).getTargetAmount() + "")
                    .replace("$name", EliteMobProperties.getPluginData(((DynamicKillObjective) quest.getQuestObjectives().getObjectives().get(0)).getEntityType()).getName(((DynamicKillObjective) quest.getQuestObjectives().getObjectives().get(0)).getMinMobLevel()))));
        return body;
    }

    private static TextComponent generateFixedSummary(Quest quest) {
        TextComponent fixedSummary = new TextComponent();
        if (quest instanceof CustomQuest)
            fixedSummary.setText(CustomQuestMenuConfig.getObjectivesLine());
        else if (quest instanceof DynamicQuest)
            fixedSummary.setText(DynamicQuestMenuConfig.getObjectivesLine());
        return fixedSummary;
    }

    private static List<TextComponent> generateSummary(Quest quest) {
        List<TextComponent> textComponents = new ArrayList<>();
        if (quest instanceof CustomQuest) {
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                textComponents.add(new TextComponent(SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.getObjectiveLine(objective), "", "")));
        } else if (quest instanceof DynamicQuest) {
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (objective instanceof KillObjective) {
                    //todo: hover should show more boss details, command string should allow players to try to track the boss
                    textComponents.add(new TextComponent(SpigotMessage.commandHoverMessage(DynamicQuestMenuConfig.getKillQuestDefaultSummaryLine(objective), "", "")));
                }
        }
        return textComponents;
    }

    private static TextComponent generateFixedRewards(Quest quest) {
        TextComponent questRewards = new TextComponent();
        if (quest instanceof CustomQuest)
            questRewards.setText(CustomQuestMenuConfig.getRewardsLine());
        else if (quest instanceof DynamicQuest)
            questRewards.setText(DynamicQuestMenuConfig.getRewardsLine());
        return questRewards;
    }

    private static List<TextComponent> generateRewards(Quest quest) {
        if (quest instanceof CustomQuest)
            return CustomQuestMenuConfig.getRewardsDefaultSummaryLine(quest.getQuestObjectives().getQuestReward());
        else
            return DynamicQuestMenuConfig.getRewardsDefaultSummaryLine(quest.getQuestObjectives().getQuestReward());
    }

    public static TextComponent describeRewardPreview(Quest quest, QuestReward.PreviewEntry preview) {
        var entry = preview.source();
        var item = preview.item();
        String name;
        if (entry instanceof CurrencyCustomLootEntry currency)
            name = currency.getCurrencyAmount() + " " + EconomySettingsConfig.getCurrencyName();
        else if (item != null) {
            var meta = item.getItemMeta();
            name = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : item.getType().toString().replace('_', ' ');
        } else if (entry instanceof EliteCustomLootEntry custom) name = custom.getFilename();
        else name = "?";
        String template = quest instanceof CustomQuest ? CustomQuestMenuConfig.getRewardsDefaultSummaryLine()
                : DynamicQuestMenuConfig.getRewardsDefaultSummaryLine();
        return new TextComponent(template.replace("$amount", String.valueOf(entry == null ? item.getAmount() : entry.getAmount()))
                .replace("$rewardName", name == null ? "?" : name)
                .replace("$chance", String.valueOf(entry == null ? 100 : (int) (entry.getChance() * 100))));
    }

    private static TextComponent generateAccept(Quest quest, NPCEntity npcEntity) {
        //Before quest was accepted
        if (!quest.isAccepted())
            return initialQuestAccept(quest);

        //Quest is complete, turn in placeholder
        if (npcEntity != null &&
                quest.getQuestObjectives().isOver() &&
                (quest instanceof DynamicQuest ||
                        quest instanceof CustomQuest &&
                                quest.getQuestTaker().equals(npcEntity.getNPCsConfigFields().getFilename())))
            return questAcceptComplete(quest);

        //Quest has begun but is either not over or the player is not talking to the turn in npc
        return questAcceptAlreadyAccepted(quest);
    }

    //Appears when the player hasn't accepted the quest yet
    private static TextComponent initialQuestAccept(Quest quest) {
        if (quest instanceof CustomQuest) {
            return SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.getAcceptTextLines(),
                    CustomQuestMenuConfig.getAcceptHoverLines(),
                    CustomQuestMenuConfig.getAcceptCommandLines().replace("$questID", quest.getQuestID().toString()));
        } else {
            return SpigotMessage.commandHoverMessage(DynamicQuestMenuConfig.getAcceptTextLines(),
                    DynamicQuestMenuConfig.getAcceptHoverLines(),
                    DynamicQuestMenuConfig.getAcceptCommandLines().replace("$questID", quest.getQuestID().toString()));
        }
    }

    //Appears when the player has accepted the quest but has not completed it yet, so it is not ready for turn in
    private static TextComponent questAcceptAlreadyAccepted(Quest quest) {
        if (quest instanceof CustomQuest) {
            String npcName = "";
            if (CustomQuestMenuConfig.getAcceptedTextLines().contains("$npcName")) {
                for (NPCEntity npcEntity : EntityTracker.getNpcEntities().values())
                    if (npcEntity.getNPCsConfigFields().getFilename().equals(quest.getQuestTaker())) {
                        npcName = npcEntity.getNPCsConfigFields().getName();
                        break;
                    }
            }
            return SpigotMessage.commandHoverMessage(
                    CustomQuestMenuConfig.getAcceptedTextLines().replace("$npcName", npcName),
                    CustomQuestMenuConfig.getAcceptedHoverLines(),
                    CustomQuestMenuConfig.getAcceptedCommandLines()
                            .replace("$questID", quest.getQuestID().toString()));
        } else {
            return SpigotMessage.commandHoverMessage(DynamicQuestMenuConfig.getAcceptedTextLines(),
                    DynamicQuestMenuConfig.getAcceptedHoverLines(),
                    DynamicQuestMenuConfig.getAcceptedCommandLines().replace("$questID", quest.getQuestID().toString()));
        }
    }

    private static TextComponent generateTrack(Player player, Quest quest) {
        if (!((CustomQuest) quest).getCustomQuestsConfigFields().isTrackable()) return new TextComponent();
        if (!CustomQuestMenuConfig.isUseQuestTracking()) return new TextComponent();
        if (!quest.isAccepted()) return new TextComponent();
        if (!QuestTracking.isTracking(player))
            return SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.getTrackTextLines(),
                    CustomQuestMenuConfig.getTrackHoverLines(), CustomQuestMenuConfig.getTrackCommandLines().replace("$questID", quest.getQuestID() + ""));
        else
            return SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.getUntrackTextLines(),
                    CustomQuestMenuConfig.getUntrackHoverLines(), CustomQuestMenuConfig.getUntrackCommandLines().replace("$questID", quest.getQuestID() + ""));
    }

    //Appears when the player has completed the quest
    private static TextComponent questAcceptComplete(Quest quest) {
        if (quest instanceof CustomQuest) {
            return SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.getCompletedTextLines(),
                    CustomQuestMenuConfig.getCompletedHoverLines(),
                    CustomQuestMenuConfig.getCompletedCommandLines().replace("$questID", quest.getQuestID().toString()));
        } else {
            return SpigotMessage.commandHoverMessage(DynamicQuestMenuConfig.getCompletedTextLines(),
                    DynamicQuestMenuConfig.getCompletedHoverLines(),
                    DynamicQuestMenuConfig.getCompletedCommandLines().replace("$questID", quest.getQuestID().toString()));
        }
    }

    public static TextComponent[] generateQuestEntry(Player player, NPCEntity npcEntity) {
        if (PlayerData.getQuests(player.getUniqueId()) == null) return new TextComponent[0];
        return generateBookQuestEntriesComponents(PlayerData.getQuests(player.getUniqueId()), player, npcEntity);
    }

    public static class QuestText {
        private final Quest quest;
        @Getter
        private final TextComponent header;
        @Getter
        private final List<TextComponent> body;
        @Getter
        private final TextComponent fixedSummary;
        @Getter
        private final List<TextComponent> summary;
        @Getter
        private final TextComponent fixedRewards;
        private List<TextComponent> rewards;
        @Getter
        private final TextComponent accept;
        @Getter
        private TextComponent track;

        public QuestText(Quest quest, NPCEntity npcEntity, Player player) {
            this.quest = quest;
            header = generateHeader(quest);
            body = generateBody(quest);
            fixedSummary = generateFixedSummary(quest);
            summary = generateSummary(quest);
            fixedRewards = generateFixedRewards(quest);
            accept = generateAccept(quest, npcEntity);
            track = null;
            if (quest instanceof CustomQuest)
                track = generateTrack(player, quest);
        }

        public List<TextComponent> getRewards() {
            if (rewards == null) rewards = generateRewards(quest);
            return rewards;
        }
    }

}
