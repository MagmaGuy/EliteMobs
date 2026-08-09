package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.dungeons.CombatContent;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.magmacore.dialog.DialogManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;

public class PlayerStatusScreenDialog {
    private static final int DIALOG_WIDTH = 300;
    private static final int NAVIGATION_BUTTON_WIDTH = 150;

    private PlayerStatusScreenDialog() {
    }

    /**
     * Main entry point - shows the cover page with navigation to all sections
     */
    public static void showPlayerStatusDialog(Player player) {
        DialogManager.DialogListDialogBuilder listBuilder = new DialogManager.DialogListDialogBuilder();

        listBuilder.title(PlayerStatusMenuConfig.getDialogTitlePlayerStatus());
        String overviewText = PlayerStatusOverview.text(player);
        if (!overviewText.isBlank())
            listBuilder.addBody(DialogManager.PlainMessageBody.of(processText(overviewText)).width(DIALOG_WIDTH));

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

        if (SkillsConfig.isSkillSystemEnabled()) {
            listBuilder.addDialog(DialogManager.DialogReference.inline(buildSkillsDialog(player)));
        }

        if (PartyConfig.isEnabled() && player.hasPermission("elitemobs.party")) {
            listBuilder.addDialog(DialogManager.DialogReference.inline(buildPartyDialog(player)));
        }

        listBuilder.columns(2);
        listBuilder.buttonWidth(NAVIGATION_BUTTON_WIDTH);

        DialogManager.sendDialog(player, listBuilder);

//        if (!PlayerData.getDismissEMStatusScreenMessage(player.getUniqueId())) {
//            player.sendMessage(PlayerStatusMenuConfig.getDismissEMMessage());
//        }
    }

    public static void showTeleportsDialog(Player player) {
        DialogManager.sendDialog(player, buildTeleportsDialog(player));
    }

    /**
     * Stats Page Dialog
     */
    private static DialogManager.MultiActionDialogBuilder buildStatsDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder()
                .columns(1);

        builder.title(PlayerStatusMenuConfig.getDialogTitleStats());

        // Build stats body
        StringBuilder statsText = new StringBuilder();
        StatsPage.StatsSnapshot stats = StatsPage.StatsSnapshot.capture(player);
        // Lines 0-3 are book-only separator/title scaffolding; the dialog already has its own title.
        for (int i = 4; i < 13; i++) {
            if (PlayerStatusMenuConfig.getStatsTextLines()[i] == null) continue;
            if (PlayerStatusMenuConfig.getStatsTextLines()[i].contains("$guildtier")) continue;

            String line = stats.replacePlaceholders(PlayerStatusMenuConfig.getStatsTextLines()[i]);

            statsText.append(processText(line)).append("\n");
        }

        if (statsText.length() > 0) {
            builder.addBody(DialogManager.PlainMessageBody.of(statsText.toString().trim()).width(DIALOG_WIDTH));
        }

        // Add action buttons if commands are configured
        for (int i = 4; i < 13; i++) {
            if (PlayerStatusMenuConfig.getStatsTextLines()[i] != null &&
                    PlayerStatusMenuConfig.getStatsTextLines()[i].contains("$guildtier")) continue;
            if (PlayerStatusMenuConfig.getStatsCommandLines() != null &&
                    PlayerStatusMenuConfig.getStatsCommandLines()[i] != null &&
                    !PlayerStatusMenuConfig.getStatsCommandLines()[i].isEmpty()) {

                String buttonLabel = stats.replacePlaceholders(PlayerStatusMenuConfig.getStatsTextLines()[i]);
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
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder()
                .columns(1);

        builder.title(PlayerStatusMenuConfig.getDialogTitleGear());

        StringBuilder gearText = new StringBuilder();
        GearProfile.Snapshot gearProfile = GearProfile.capture(player);
        for (String configuredLine : PlayerStatusMenuConfig.getDialogGearSummaryLines()) {
            if (configuredLine == null || configuredLine.isBlank()) continue;
            gearText.append(processText(GearProfile.resolve(configuredLine, gearProfile))).append("\n");
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

        builder.title(PlayerStatusMenuConfig.getDialogTitleTeleports());

        // Add configured non-action text and turn configured teleport commands into buttons.
        StringBuilder teleportText = new StringBuilder();
        // Lines 0-2 are book-only separator/title scaffolding.
        for (int i = 3; i < PlayerStatusMenuConfig.getTeleportTextLines().length; i++) {
            String line = PlayerStatusMenuConfig.getTeleportTextLines()[i];
            if (line == null || line.equals("null")) continue;
            String command = PlayerStatusMenuConfig.getTeleportCommandLines()[i];
            if (command == null || command.isBlank()) {
                teleportText.append(processText(line)).append("\n");
                continue;
            }

            DialogManager.ActionButton button = DialogManager.ActionButton.of(
                    processText(line), new DialogManager.RunCommandAction(command)).width(DIALOG_WIDTH);
            String hover = PlayerStatusMenuConfig.getTeleportHoverLines()[i];
            if (hover != null && !hover.isBlank()) button.tooltip(processText(hover));
            builder.addAction(button);
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
                hoverInfo = PlayerStatusMenuConfig.getOnTeleportHover() + "\n" +
                                playerInfo
                                        .replace("$bossCount", emPackage.getCustomBossEntityList().size() + "")
                                        .replace("$lowestTier", ((CombatContent) emPackage).getLowestLevel() + "")
                                        .replace("$highestTier", ((CombatContent) emPackage).getHighestLevel() + "");
            } else {
                hoverInfo = PlayerStatusMenuConfig.getOnTeleportHover();
            }

            DialogManager.ActionButton button = DialogManager.ActionButton.of(
                    dungeonName,
                    new DialogManager.RunCommandAction("/elitemobs dungeontpdialog " +
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

        builder.title(PlayerStatusMenuConfig.getDialogTitleCommands());

        // Keep headings as body text and expose configured commands as the actual actions below.
        StringBuilder commandsText = new StringBuilder();
        // Lines 0-3 are book-only separator/title scaffolding.
        for (int i = 4; i < 13; i++) {
            if (PlayerStatusMenuConfig.getCommandsTextLines()[i] == null) continue;
            String command = CommandsPage.normalizeCommand(PlayerStatusMenuConfig.getCommandsCommandLines()[i]);
            if (command != null && !command.isBlank()) continue;

            String line = PlayerStatusMenuConfig.getCommandsTextLines()[i];
            commandsText.append(processText(line)).append("\n");
        }

        if (commandsText.length() > 0) {
            builder.addBody(DialogManager.PlainMessageBody.of(commandsText.toString().trim()).width(DIALOG_WIDTH));
        }

        // Add command buttons
        for (int i = 4; i < 13; i++) {
            String command = CommandsPage.normalizeCommand(PlayerStatusMenuConfig.getCommandsCommandLines()[i]);
            if (command == null || command.isBlank()) continue;

            String buttonLabel = PlayerStatusMenuConfig.getCommandsTextLines()[i];
            if (buttonLabel != null && !buttonLabel.isEmpty()) {
                DialogManager.ActionButton button = DialogManager.ActionButton.of(
                        processText(buttonLabel),
                        new DialogManager.RunCommandAction(command)
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

        builder.title(PlayerStatusMenuConfig.getDialogTitleQuests());

        List<Quest> quests = PlayerData.getQuests(player.getUniqueId());

        if (quests == null || quests.isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of(PlayerStatusMenuConfig.getDialogNoActiveQuests()).width(DIALOG_WIDTH));
        } else {
            builder.addBody(DialogManager.PlainMessageBody.of(
                    PlayerStatusMenuConfig.getDialogActiveQuestsFormat().replace("$amount", String.valueOf(quests.size()))
            ).width(DIALOG_WIDTH));

            builder.columns(1);
            for (Quest quest : quests) {
                String questName = quest.getQuestName() != null ?
                        processText(quest.getQuestName()) :
                        processText(PlayerStatusMenuConfig.getDialogQuestFallbackFormat()
                                .replace("$id", String.valueOf(quest.getQuestID())));

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
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder()
                .columns(1);

        builder.title(PlayerStatusMenuConfig.getDialogTitleBossTracking());

        // Add config text
        StringBuilder trackingText = new StringBuilder();
        // Lines 0-2 are book-only separator/title scaffolding.
        for (int i = 3; i < PlayerStatusMenuConfig.getBossTrackerTextLines().length; i++) {
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
        int trackableBossCount = 0;
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
                trackableBossCount++;
            } catch (Exception ex) {
                // Skip problematic bosses
            }
        }
        if (trackableBossCount == 0)
            builder.addBody(DialogManager.PlainMessageBody.of(
                    processText(PlayerStatusMenuConfig.getDialogNoTrackableBosses())).width(DIALOG_WIDTH));

        // Back button
        addBackButton(builder);

        return builder;
    }

    /**
     * Skills Page Dialog
     */
    private static DialogManager.MultiActionDialogBuilder buildSkillsDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder()
                .columns(1);

        builder.title(PlayerStatusMenuConfig.getDialogTitleSkills());

        StringBuilder skillsText = new StringBuilder();

        for (SkillType skillType : SkillType.values()) {
            long totalXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
            int level = SkillXPCalculator.levelFromTotalXP(totalXP);
            long xpInLevel = SkillXPCalculator.xpProgressInCurrentLevel(totalXP);
            long xpForNext = SkillXPCalculator.xpToNextLevel(level);
            double progress = SkillXPCalculator.levelProgress(totalXP);

            String progressBar = createProgressBar(progress);

            String levelLine = PlayerStatusMenuConfig.getSkillsPageLevelFormat()
                    .replace("$skillName", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
                    .replace("$level", String.valueOf(level));
            String xpLine = PlayerStatusMenuConfig.getSkillsPageXpFormat()
                    .replace("$progressBar", progressBar)
                    .replace("$currentXp", formatNumber(xpInLevel))
                    .replace("$nextXp", formatNumber(xpForNext));

            skillsText.append(processText(levelLine)).append("\n");
            skillsText.append(processText(xpLine)).append("\n\n");
        }

        builder.addBody(DialogManager.PlainMessageBody.of(skillsText.toString().trim()).width(DIALOG_WIDTH));

        addBackButton(builder);

        return builder;
    }

    private static DialogManager.MultiActionDialogBuilder buildPartyDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder()
                .title(PlayerStatusMenuConfig.getDialogTitleParty())
                .columns(1)
                .addBody(DialogManager.PlainMessageBody.of(
                        processText(PlayerStatusMenuConfig.getDialogPartyDescription())).width(DIALOG_WIDTH));

        builder.addAction(DialogManager.ActionButton.of(
                processText(PlayerStatusMenuConfig.getDialogPartyInviteButton()),
                new DialogManager.ShowDialogAction(
                        DialogManager.DialogReference.inline(buildPartyInviteDialog(player)))).width(DIALOG_WIDTH));
        if (PartyManager.isInParty(player.getUniqueId())) {
            builder.addAction(DialogManager.ActionButton.of(
                    processText(PlayerStatusMenuConfig.getDialogPartyLeaveButton()),
                    new DialogManager.RunCommandAction("/em party leave")).width(DIALOG_WIDTH));
        } else {
            builder.addAction(DialogManager.ActionButton.of(
                    processText(PlayerStatusMenuConfig.getDialogPartyCreateButton()),
                    new DialogManager.RunCommandAction("/em party create")).width(DIALOG_WIDTH));
        }
        addBackButton(builder);
        return builder;
    }

    private static DialogManager.MultiActionDialogBuilder buildPartyInviteDialog(Player player) {
        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder()
                .title(processText(PlayerStatusMenuConfig.getDialogPartyInviteTitle()))
                .columns(1);
        List<Player> targets = PartyManager.getInvitablePlayers(player);
        if (targets.isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of(
                    processText(PlayerStatusMenuConfig.getDialogPartyInviteNoPlayers())).width(DIALOG_WIDTH));
        } else {
            for (Player target : targets) {
                String playerName = target.getName();
                builder.addAction(DialogManager.ActionButton.of(
                                processText(PlayerStatusMenuConfig.getDialogPartyInvitePlayerButton()
                                        .replace("$player", playerName)),
                                new DialogManager.RunCommandAction("/em party invite " + playerName))
                        .tooltip(processText(PlayerStatusMenuConfig.getDialogPartyInvitePlayerTooltip()
                                .replace("$player", playerName)))
                        .width(DIALOG_WIDTH));
            }
        }
        addBackButton(builder);
        return builder;
    }

    /**
     * Creates a visual progress bar for skills.
     */
    private static String createProgressBar(double progress) {
        int totalBars = 20;
        int filledBars = (int) (progress * totalBars);
        int emptyBars = totalBars - filledBars;

        StringBuilder bar = new StringBuilder("\u00A7a");
        for (int i = 0; i < filledBars; i++) {
            bar.append("|");
        }
        bar.append("\u00A77");
        for (int i = 0; i < emptyBars; i++) {
            bar.append("|");
        }

        return bar.toString();
    }

    /**
     * Formats large numbers with K, M suffixes.
     */
    private static String formatNumber(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.1fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        } else {
            return String.valueOf(number);
        }
    }

    /**
     * Helper method to add equipped items as ItemBody elements
     */
    private static void addEquippedItems(DialogManager.MultiActionDialogBuilder builder, Player player) {
        // Helmet
        ItemStack helmet = player.getInventory().getHelmet();
        if (hasItem(helmet)) {
            addItemBody(builder, helmet, PlayerStatusMenuConfig.getDialogGearHelmetLabel());
        }

        // Chestplate
        ItemStack chestplate = player.getInventory().getChestplate();
        if (hasItem(chestplate)) {
            addItemBody(builder, chestplate, PlayerStatusMenuConfig.getDialogGearChestplateLabel());
        }

        // Leggings
        ItemStack leggings = player.getInventory().getLeggings();
        if (hasItem(leggings)) {
            addItemBody(builder, leggings, PlayerStatusMenuConfig.getDialogGearLeggingsLabel());
        }

        // Boots
        ItemStack boots = player.getInventory().getBoots();
        if (hasItem(boots)) {
            addItemBody(builder, boots, PlayerStatusMenuConfig.getDialogGearBootsLabel());
        }

        // Main hand
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (hasItem(mainHand)) {
            addItemBody(builder, mainHand, PlayerStatusMenuConfig.getDialogGearMainHandLabel());
        }

        // Off hand
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (hasItem(offHand)) {
            addItemBody(builder, offHand, PlayerStatusMenuConfig.getDialogGearOffHandLabel());
        }
    }

    private static boolean hasItem(ItemStack itemStack) {
        return itemStack != null && !itemStack.getType().isAir();
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
     * Helper method to add back button
     */
    private static void addBackButton(DialogManager.MultiActionDialogBuilder builder) {
        builder.addAction(DialogManager.ActionButton.of(
                PlayerStatusMenuConfig.getDialogBackButton(),
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
