package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.dungeons.CombatContent;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.magmacore.dialog.DialogManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;

public class PlayerStatusScreenDialog {
    private static final int DIALOG_WIDTH = 300;

    private PlayerStatusScreenDialog() {
    }

    /**
     * Main entry point - shows the cover page with navigation to all sections
     */
    public static void showPlayerStatusDialog(Player player) {
        DialogManager.DialogListDialogBuilder listBuilder = new DialogManager.DialogListDialogBuilder();

        listBuilder.title("Player Status Menu");

        // Add each page as a dialog reference
        if (PlayerStatusMenuConfig.isDoStatsPage()) {
            listBuilder.addDialog(DialogManager.DialogReference.inline(buildStatsDialog(player)));
        }

        if (PlayerStatusMenuConfig.isDoGearPage()) {
            listBuilder.addDialog(DialogManager.DialogReference.inline(buildGearDialog(player)));
        }

        if (PlayerStatusMenuConfig.isDoTeleportsPage()) {
            listBuilder.addDialog(DialogManager.DialogReference.inline(buildTeleportsDialog(player)));
        }

        if (PlayerStatusMenuConfig.isDoCommandsPage()) {
            listBuilder.addDialog(DialogManager.DialogReference.inline(buildCommandsDialog(player)));
        }

        if (PlayerStatusMenuConfig.isDoQuestTrackingPage()) {
            listBuilder.addDialog(DialogManager.DialogReference.inline(buildQuestsDialog(player)));
        }

        if (PlayerStatusMenuConfig.isDoBossTrackingPage()) {
            listBuilder.addDialog(DialogManager.DialogReference.inline(buildBossTrackingDialog(player)));
        }

        listBuilder.columns(1);
        listBuilder.buttonWidth(DIALOG_WIDTH);

        DialogManager.sendDialog(player, listBuilder);

//        if (!PlayerData.getDismissEMStatusScreenMessage(player.getUniqueId())) {
//            player.sendMessage(PlayerStatusMenuConfig.getDismissEMMessage());
//        }
    }

    /**
     * Stats Page Dialog
     */
    private static DialogManager.MultiActionDialogBuilder buildStatsDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        builder.title("Stats");

        // Build stats body
        StringBuilder statsText = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            if (PlayerStatusMenuConfig.getStatsTextLines()[i] == null) continue;

            String line = PlayerStatusMenuConfig.getStatsTextLines()[i]
                    .replace("$money", EconomyHandler.checkCurrency(player.getUniqueId()) + "")
                    .replace("$guildtier", "N/A")
                    .replace("$kills", PlayerData.getKills(player.getUniqueId()) + "")
                    .replace("$highestkill", PlayerData.getHighestLevelKilled(player.getUniqueId()) + "")
                    .replace("$deaths", PlayerData.getDeaths(player.getUniqueId()) + "")
                    .replace("$quests", PlayerData.getQuestsCompleted(player.getUniqueId()) + "")
                    .replace("$score", PlayerData.getScore(player.getUniqueId()) + "");

            statsText.append(processText(line)).append("\n");
        }

        if (statsText.length() > 0) {
            builder.addBody(DialogManager.PlainMessageBody.of(statsText.toString().trim()).width(DIALOG_WIDTH));
        }

        // Add action buttons if commands are configured
        for (int i = 0; i < 13; i++) {
            if (PlayerStatusMenuConfig.getStatsCommandLines() != null &&
                    PlayerStatusMenuConfig.getStatsCommandLines()[i] != null &&
                    !PlayerStatusMenuConfig.getStatsCommandLines()[i].isEmpty()) {

                String buttonLabel = PlayerStatusMenuConfig.getStatsTextLines()[i];
                if (buttonLabel != null && !buttonLabel.isEmpty()) {
                    builder.addAction(DialogManager.ActionButton.of(
                            processText(buttonLabel),
                            new DialogManager.RunCommandAction(PlayerStatusMenuConfig.getStatsCommandLines()[i])
                    ).width(DIALOG_WIDTH));
                }
            }
        }

        // Back button
        addBackButton(builder);

        return builder;
    }

    /**
     * Gear Page Dialog
     */
    private static DialogManager.MultiActionDialogBuilder buildGearDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        builder.title("Gear");

        // Build gear body
        StringBuilder gearText = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            if (PlayerStatusMenuConfig.getGearTextLines()[i] == null) continue;

            String line = parseGearPlaceholders(PlayerStatusMenuConfig.getGearTextLines()[i], player);
            gearText.append(processText(line)).append("\n");
        }

        if (gearText.length() > 0) {
            builder.addBody(DialogManager.PlainMessageBody.of(gearText.toString().trim()).width(DIALOG_WIDTH));
        }

        // Add equipped items as ItemBody elements
        addEquippedItems(builder, player);

        // Back button
        addBackButton(builder);

        return builder;
    }

    /**
     * Teleports Page Dialog
     */
    private static DialogManager.MultiActionDialogBuilder buildTeleportsDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        builder.title("Teleports");

        // Add config text lines
        StringBuilder teleportText = new StringBuilder();
        for (String line : PlayerStatusMenuConfig.getTeleportTextLines()) {
            if (line == null || line.equals("null")) continue;
            teleportText.append(processText(line)).append("\n");
        }

        if (teleportText.length() > 0) {
            builder.addBody(DialogManager.PlainMessageBody.of(teleportText.toString().trim()).width(DIALOG_WIDTH));
        }

        // Add dungeon teleport buttons
        for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
            if (!emPackage.isInstalled() ||
                    !(emPackage instanceof CombatContent) ||
                    emPackage.getContentPackagesConfigFields().isEnchantmentChallenge()) continue;
            if (!emPackage.getContentPackagesConfigFields().isListedInTeleports()) continue;

            String dungeonName = processText(emPackage.getContentPackagesConfigFields().getName());
            String playerInfo = emPackage.getContentPackagesConfigFields().getPlayerInfo();
            String hoverInfo;
            if (playerInfo != null) {
                hoverInfo = ChatColorConverter.convert(
                        PlayerStatusMenuConfig.getOnTeleportHover() + "\n" +
                                playerInfo
                                        .replace("$bossCount", emPackage.getCustomBossEntityList().size() + "")
                                        .replace("$lowestTier", ((CombatContent) emPackage).getLowestLevel() + "")
                                        .replace("$highestTier", ((CombatContent) emPackage).getHighestLevel() + ""));
            } else {
                hoverInfo = ChatColorConverter.convert(PlayerStatusMenuConfig.getOnTeleportHover());
            }

            DialogManager.ActionButton button = DialogManager.ActionButton.of(
                    dungeonName,
                    new DialogManager.RunCommandAction("/elitemobs dungeontp " +
                            emPackage.getContentPackagesConfigFields().getFilename())
            ).width(DIALOG_WIDTH);

            if (hoverInfo != null && !hoverInfo.isEmpty()) {
                button.tooltip(processText(hoverInfo));
            }

            builder.columns(1);
            builder.addAction(button);
        }

        // Back button
        builder.columns(1);
        addBackButton(builder);

        return builder;
    }

    /**
     * Commands Page Dialog
     */
    private static DialogManager.MultiActionDialogBuilder buildCommandsDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        builder.title("Commands");

        // Build commands body
        StringBuilder commandsText = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            if (PlayerStatusMenuConfig.getCommandsTextLines()[i] == null) continue;

            String line = PlayerStatusMenuConfig.getCommandsTextLines()[i];
            commandsText.append(processText(line)).append("\n");
        }

        if (commandsText.length() > 0) {
            builder.addBody(DialogManager.PlainMessageBody.of(commandsText.toString().trim()).width(DIALOG_WIDTH));
        }

        // Add command buttons
        for (int i = 0; i < 13; i++) {
            if (PlayerStatusMenuConfig.getCommandsCommandLines()[i] == null ||
                    PlayerStatusMenuConfig.getCommandsCommandLines()[i].isEmpty()) continue;

            String buttonLabel = PlayerStatusMenuConfig.getCommandsTextLines()[i];
            if (buttonLabel != null && !buttonLabel.isEmpty()) {
                DialogManager.ActionButton button = DialogManager.ActionButton.of(
                        processText(buttonLabel),
                        new DialogManager.RunCommandAction(PlayerStatusMenuConfig.getCommandsCommandLines()[i])
                ).width(DIALOG_WIDTH);

                if (PlayerStatusMenuConfig.getCommandsHoverLines()[i] != null &&
                        !PlayerStatusMenuConfig.getCommandsHoverLines()[i].isEmpty()) {
                    button.tooltip(processText(PlayerStatusMenuConfig.getCommandsHoverLines()[i]));
                }
                builder.columns(1);
                builder.addAction(button);
            }
        }

        // Back button
        builder.columns(1);
        addBackButton(builder);

        return builder;
    }

    /**
     * Quests Page Dialog
     */
    private static DialogManager.MultiActionDialogBuilder buildQuestsDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        builder.title("Quests");

        List<Quest> quests = PlayerData.getQuests(player.getUniqueId());

        if (quests == null || quests.isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of("No active quests").width(DIALOG_WIDTH));
        } else {
            builder.addBody(DialogManager.PlainMessageBody.of(
                    "You have " + quests.size() + " active quest(s)."
            ).width(DIALOG_WIDTH));

            builder.columns(1);
            for (Quest quest : quests) {
                String questName = quest.getQuestName() != null ?
                        processText(quest.getQuestName()) :
                        "Quest " + quest.getQuestID();

                builder.addAction(DialogManager.ActionButton.of(
                        questName,
                        new DialogManager.RunCommandAction("/elitemobs quest check " + quest.getQuestID())
                ).width(DIALOG_WIDTH));
            }
        }

        builder.columns(1);
        addBackButton(builder);

        return builder;
    }

    /**
     * Boss Tracking Page Dialog
     */
    private static DialogManager.MultiActionDialogBuilder buildBossTrackingDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        builder.title("Boss Tracking");

        // Add config text
        StringBuilder trackingText = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (PlayerStatusMenuConfig.getBossTrackerTextLines()[i] == null) continue;
            trackingText.append(processText(PlayerStatusMenuConfig.getBossTrackerTextLines()[i])).append("\n");
        }

        if (trackingText.length() > 0) {
            builder.addBody(DialogManager.PlainMessageBody.of(trackingText.toString().trim()).width(DIALOG_WIDTH));
        }

        // Clean up stale bosses
        HashSet<CustomBossEntity> tempSet = new HashSet<>(CustomBossEntity.getTrackableCustomBosses());
        tempSet.forEach(customBossEntity -> {
            if (!customBossEntity.exists())
                CustomBossEntity.getTrackableCustomBosses().remove(customBossEntity);
        });

        // Add boss tracking buttons
        for (CustomBossEntity customBossEntity : CustomBossEntity.getTrackableCustomBosses()) {
            try {
                String bossName = customBossEntity.getBossTrackingBar().bossBarMessage(
                        player, customBossEntity.getCustomBossesConfigFields().getLocationMessage());

                DialogManager.ActionButton button = DialogManager.ActionButton.of(
                        processText(bossName),
                        new DialogManager.RunCommandAction("/elitemobs track boss " + customBossEntity.getEliteUUID())
                ).width(DIALOG_WIDTH);

                if (PlayerStatusMenuConfig.getOnBossTrackHover() != null &&
                        !PlayerStatusMenuConfig.getOnBossTrackHover().isEmpty()) {
                    button.tooltip(processText(PlayerStatusMenuConfig.getOnBossTrackHover()));
                }

                builder.addAction(button);
            } catch (Exception ex) {
                // Skip problematic bosses
            }
        }

        // Back button
        addBackButton(builder);

        return builder;
    }

    /**
     * Helper method to add equipped items as ItemBody elements
     */
    private static void addEquippedItems(DialogManager.MultiActionDialogBuilder builder, Player player) {
        // Helmet
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && EliteItemManager.isEliteMobsItem(helmet)) {
            addItemBody(builder, helmet, "Helmet");
        }

        // Chestplate
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && EliteItemManager.isEliteMobsItem(chestplate)) {
            addItemBody(builder, chestplate, "Chestplate");
        }

        // Leggings
        ItemStack leggings = player.getInventory().getLeggings();
        if (leggings != null && EliteItemManager.isEliteMobsItem(leggings)) {
            addItemBody(builder, leggings, "Leggings");
        }

        // Boots
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && EliteItemManager.isEliteMobsItem(boots)) {
            addItemBody(builder, boots, "Boots");
        }

        // Main hand
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && EliteItemManager.isEliteMobsItem(mainHand)) {
            addItemBody(builder, mainHand, "Main Hand");
        }

        // Off hand
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && EliteItemManager.isEliteMobsItem(offHand)) {
            addItemBody(builder, offHand, "Off Hand");
        }
    }

    /**
     * Helper method to add an item as ItemBody
     */
    private static void addItemBody(DialogManager.MultiActionDialogBuilder builder, ItemStack item, String label) {
        String itemId = item.getType().getKey().toString();

        DialogManager.ItemBody itemBody = DialogManager.ItemBody.of(itemId, item.getAmount())
                .showTooltip(true)
                .showDecoration(true)
                .description(label);

        JsonObject components = DialogManager.serializeItemComponents(item);
        if (components != null && !components.entrySet().isEmpty()) {
            // Fix custom_model_data format for 1.21.4+ (changed from int to object)
            fixCustomModelDataFormat(components);
            itemBody.components(components);
        }

        builder.addBody(itemBody);
    }

    /**
     * Fixes the custom_model_data format for Minecraft 1.21.4+
     * Old format: "minecraft:custom_model_data": 36004
     * New format: "minecraft:custom_model_data": {"floats": [36004.0]}
     */
    private static void fixCustomModelDataFormat(JsonObject components) {
        String key = "minecraft:custom_model_data";
        if (components.has(key)) {
            JsonElement element = components.get(key);
            if (element.isJsonPrimitive()) {
                // Convert integer/number to new format
                double value = element.getAsDouble();
                JsonObject newFormat = new JsonObject();
                JsonArray floats = new JsonArray();
                floats.add(value);
                newFormat.add("floats", floats);
                components.add(key, newFormat);
            }
        }
    }

    /**
     * Helper method to parse gear placeholders
     */
    private static String parseGearPlaceholders(String string, Player player) {
        ElitePlayerInventory inventory = ElitePlayerInventory.playerInventories.get(player.getUniqueId());

        if (inventory == null) return string;

        return string
                .replace("$helmettier", inventory.helmet.getTier(player.getInventory().getHelmet(), true) + "")
                .replace("$chestplatetier", inventory.chestplate.getTier(player.getInventory().getChestplate(), true) + "")
                .replace("$leggingstier", inventory.leggings.getTier(player.getInventory().getLeggings(), true) + "")
                .replace("$bootstier", inventory.boots.getTier(player.getInventory().getBoots(), true) + "")
                .replace("$mainhandtier", inventory.mainhand.getTier(player.getInventory().getItemInMainHand(), true) + "")
                .replace("$offhandtier", inventory.offhand.getTier(player.getInventory().getItemInOffHand(), true) + "")
                .replace("$damage", inventory.baseDamage() + "")
                .replace("$armor", inventory.getEliteDefense(false) + "")
                .replace("$threat", inventory.getNaturalMobSpawnLevel(true) + "");
    }

    /**
     * Helper method to add back button
     */
    private static void addBackButton(DialogManager.MultiActionDialogBuilder builder) {
        builder.addAction(DialogManager.ActionButton.of(
                "← Back to Menu",
                new DialogManager.RunCommandAction("/elitemobs")
        ).width(DIALOG_WIDTH));
    }

    /**
     * Processes text by replacing black color codes with white color codes
     * to ensure readability in dialogs
     */
    private static String processText(String text) {
        if (text == null) return null;
        return text.replace("§0", "§f").replace("&0", "&f");
    }
}