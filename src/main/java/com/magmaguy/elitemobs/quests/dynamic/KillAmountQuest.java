package com.magmaguy.elitemobs.quests.dynamic;

import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.quests.QuestReward;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.PlayerHeads;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class KillAmountQuest {

    private static HashMap<Player, KillAmountQuest> activeKillAmountQuests = new HashMap<>();

    public static HashMap<Player, KillAmountQuest> getActiveKillAmountQuests() {
        return activeKillAmountQuests;
    }

    public static boolean hasKillAmountQuest(Player player) {
        return activeKillAmountQuests.containsKey(player);
    }

    public static KillAmountQuest getKillAmountQuest(Player player) {
        if (activeKillAmountQuests.containsKey(player))
            return activeKillAmountQuests.get(player);
        return null;
    }

    private static void addKillAmountQuest(Player player, KillAmountQuest killAmountQuest) {
        activeKillAmountQuests.put(player, killAmountQuest);
    }

    private int questTier;
    private int minimumMobTier;
    private EntityType entityType;
    private int currentKills = 0;
    private int objectiveKills;
    private double questDifficulty;
    private QuestReward questReward;
    private boolean isComplete = false;

    public KillAmountQuest(int questTier) {
        setQuestTier(questTier);
        randomizeEntityType();
        setMinimumTier(questTier);
        setObjectiveKills();
        setQuestReward();
    }

    private void setQuestTier(int questTier) {
        this.questTier = questTier;
    }

    public int getQuestTier() {
        return this.questTier;
    }

    private EntityType getEntityType() {
        return this.entityType;
    }

    private void randomizeEntityType() {

        ArrayList<EntityType> validEntities = new ArrayList<>();

        if (EliteMobProperties.isValidEliteMobType(EntityType.BLAZE))
            validEntities.add(EntityType.BLAZE);
        if (EliteMobProperties.isValidEliteMobType(EntityType.CAVE_SPIDER))
            validEntities.add(EntityType.CAVE_SPIDER);
        if (EliteMobProperties.isValidEliteMobType(EntityType.CREEPER))
            validEntities.add(EntityType.CREEPER);
        if (EliteMobProperties.isValidEliteMobType(EntityType.ENDERMAN))
            validEntities.add(EntityType.ENDERMAN);
        if (EliteMobProperties.isValidEliteMobType(EntityType.PIG_ZOMBIE))
            validEntities.add(EntityType.PIG_ZOMBIE);
        if (EliteMobProperties.isValidEliteMobType(EntityType.SKELETON))
            validEntities.add(EntityType.SKELETON);
        if (EliteMobProperties.isValidEliteMobType(EntityType.SPIDER))
            validEntities.add(EntityType.SPIDER);
        if (EliteMobProperties.isValidEliteMobType(EntityType.WITHER_SKELETON))
            validEntities.add(EntityType.WITHER_SKELETON);
        if (EliteMobProperties.isValidEliteMobType(EntityType.ZOMBIE))
            validEntities.add(EntityType.ZOMBIE);
        if (EliteMobProperties.isValidEliteMobType(EntityType.STRAY))
            validEntities.add(EntityType.STRAY);
        if (EliteMobProperties.isValidEliteMobType(EntityType.HUSK))
            validEntities.add(EntityType.HUSK);

        this.entityType = validEntities.get(ThreadLocalRandom.current().nextInt(validEntities.size()));

    }

    private int getMinimumTier() {
        return this.minimumMobTier;
    }

    private void setMinimumTier(int questTier) {
        this.minimumMobTier = questTier * 10;
    }

    public int getCurrentKills() {
        return this.currentKills;
    }

    public void incrementCurrentKills() {
        this.currentKills++;
        if (this.objectiveKills <= currentKills)
            isComplete = true;
    }

    private int getObjectiveKills() {
        return this.objectiveKills;
    }

    private void setObjectiveKills() {
        this.objectiveKills = ThreadLocalRandom.current().nextInt(10) * questTier + 5;
        this.questDifficulty = this.objectiveKills / 10 * questTier + 5;
    }

    private double getQuestDifficulty() {
        return this.questDifficulty;
    }

    public QuestReward getQuestReward() {
        return this.questReward;
    }

    private void setQuestReward() {
        this.questReward = new QuestReward(getQuestTier(), getQuestDifficulty());
    }

    public boolean getIsComplete() {
        return this.isComplete;
    }

    public ItemStack generateQuestItemStack() {
        return ItemStackGenerator.generateItemStack(
                PlayerHeads.exclamation(),
                "&aKill " + getObjectiveKills() + " " +
                        EliteMobProperties.getPluginData(getEntityType()).getName()
                                .replace("$level", MobTierCalculator.findMobLevel(getMinimumTier()) + ""),
                Arrays.asList("&fYou must kill &a" + getObjectiveKills() + " &flevel &a",
                        "&a" + MobTierCalculator.findMobLevel(getMinimumTier()) + "&e+ &fmobs.",
                        ("&fProgress: &a" + getCurrentKills() + "&f/&c" + getObjectiveKills()),
                        "&aReward: &e" + getQuestReward().getRewardMessage()));
    }

}
