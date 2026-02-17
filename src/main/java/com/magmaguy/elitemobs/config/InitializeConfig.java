package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

public class InitializeConfig extends ConfigurationFile {

    // Menu chrome
    @Getter
    private static String menuTitle;
    @Getter
    private static String menuSubtitle;
    @Getter
    private static String separatorLine;

    // Info button
    @Getter
    private static String infoButtonName;
    @Getter
    private static String infoButtonLore1;
    @Getter
    private static String infoButtonLore2;
    @Getter
    private static String infoButtonLore3;
    @Getter
    private static String infoButtonClickSetupLink;
    @Getter
    private static String infoButtonClickContentBrowse;
    @Getter
    private static String infoButtonClickDiscord;

    // Recommended preset
    @Getter
    private static String recommendedPresetName;
    @Getter
    private static String recommendedPresetLore1;
    @Getter
    private static String recommendedPresetLore2;
    @Getter
    private static String recommendedPresetLore3;
    @Getter
    private static String recommendedPresetLore4;
    @Getter
    private static String recommendedPresetLore5;
    @Getter
    private static String recommendedPresetClickConfirm;
    @Getter
    private static String recommendedPresetClickNightbreakLogin;
    @Getter
    private static String recommendedPresetClickDownloadAll;
    @Getter
    private static String recommendedPresetClickSetupGuide;
    @Getter
    private static String recommendedPresetClickDiscord;
    @Getter
    private static String recommendedPresetClickWiki;

    // Content-only preset
    @Getter
    private static String contentOnlyPresetName;
    @Getter
    private static String contentOnlyPresetLore1;
    @Getter
    private static String contentOnlyPresetLore2;
    @Getter
    private static String contentOnlyPresetLore3;
    @Getter
    private static String contentOnlyPresetLore4;
    @Getter
    private static String contentOnlyPresetLore5;
    @Getter
    private static String contentOnlyPresetClickConfirm;
    @Getter
    private static String contentOnlyPresetClickNightbreakLogin;
    @Getter
    private static String contentOnlyPresetClickDownloadAll;
    @Getter
    private static String contentOnlyPresetClickSetupGuide;
    @Getter
    private static String contentOnlyPresetClickDiscord;
    @Getter
    private static String contentOnlyPresetClickWiki;

    // Nothing preset
    @Getter
    private static String nothingPresetName;
    @Getter
    private static String nothingPresetLore1;
    @Getter
    private static String nothingPresetLore2;
    @Getter
    private static String nothingPresetLore3;
    @Getter
    private static String nothingPresetLore4;
    @Getter
    private static String nothingPresetLore5;
    @Getter
    private static String nothingPresetLore6;
    @Getter
    private static String nothingPresetClickConfirm;
    @Getter
    private static String nothingPresetClickWarning;
    @Getter
    private static String nothingPresetClickBetterStructures;
    @Getter
    private static String nothingPresetClickSetupGuide;

    // First-time login listener
    @Getter
    private static String loginHeader;
    @Getter
    private static String loginWelcome;
    @Getter
    private static String loginHelpHint;
    @Getter
    private static String loginDiscord;
    @Getter
    private static String loginDismiss;
    @Getter
    private static String loginLanguagePrefix;
    @Getter
    private static String discordLinkDisplay;
    @Getter
    private static String discordLinkHover;
    @Getter
    private static String accountLinkDisplay;
    @Getter
    private static String accountLinkHover;
    @Getter
    private static String contentLinkDisplay;
    @Getter
    private static String contentLinkHover;
    @Getter
    private static String wikiLinkDisplay;
    @Getter
    private static String wikiLinkHover;

    // Shared clickable command settings
    @Getter
    private static String emInitializeDisplay;
    @Getter
    private static String emInitializeHover;
    @Getter
    private static String emSetupDisplay;
    @Getter
    private static String emSetupHover;
    @Getter
    private static String emCommandDisplay;
    @Getter
    private static String emCommandHover;
    @Getter
    private static String emLootDisplay;
    @Getter
    private static String emLootHover;

    // Suffix fields for messages with embedded commands
    @Getter
    private static String loginHelpHintSuffix;
    @Getter
    private static String infoButtonClickContentBrowseSuffix;
    @Getter
    private static String recommendedPresetClickDownloadAllSuffix;
    @Getter
    private static String contentOnlyPresetClickDownloadAllSuffix;

    public InitializeConfig() {
        super("initialize.yml");
    }

    @Override
    public void initializeValues() {
        // Menu chrome — kept raw (no color conversion) because FirstTimeSetupMenu.animateTitle()
        // needs raw gradient tags to count visible characters and convert per-frame
        fileConfiguration.addDefault("menuTitle", "<g:#228B22:#32CD32>EliteMobs</g>");
        fileConfiguration.setComments("menuTitle", List.of("Title shown in the /em initialize menu"));
        menuTitle = fileConfiguration.getString("menuTitle");
        fileConfiguration.addDefault("menuSubtitle", "<g:#CD7F32:#FFD700>Events</g>&7, <g:#2E8B57:#3CB371>Dungeons</g>&7, <g:#4A7A9A:#6A9ABA>Arenas</g> &7& <g:#7B2FBE:#A855F7>More</g>&7!");
        fileConfiguration.setComments("menuSubtitle", List.of("Subtitle shown in the /em initialize menu"));
        menuSubtitle = fileConfiguration.getString("menuSubtitle");
        separatorLine = ConfigurationEngine.setString(
                List.of("Separator line used in chat messages"),
                file, fileConfiguration, "separatorLine", "<g:#8B0000:#CC4400:#DAA520>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</g>", true);

        // Info button
        infoButtonName = ConfigurationEngine.setString(
                List.of("Display name for the info button"),
                file, fileConfiguration, "infoButtonName", "&fWelcome to <g:#228B22:#32CD32>EliteMobs</g>&f, by <g:#CC4400:#DAA520>MagmaGuy</g>&f!", true);
        infoButtonLore1 = ConfigurationEngine.setString(
                List.of("First lore line for the info button"),
                file, fileConfiguration, "infoButtonLore1", "&7Click to get setup links and resources.", true);
        infoButtonLore2 = ConfigurationEngine.setString(
                List.of("Second lore line for the info button"),
                file, fileConfiguration, "infoButtonLore2", "&7Choose a preset below to configure your server.", true);
        infoButtonLore3 = ConfigurationEngine.setString(
                List.of("Third lore line for the info button"),
                file, fileConfiguration, "infoButtonLore3", "&6We recommend the &aRecommended preset&6!", true);
        infoButtonClickSetupLink = ConfigurationEngine.setString(
                List.of("Setup guide link sent when info button is clicked"),
                file, fileConfiguration, "infoButtonClickSetupLink", "&7Setup guide: ", true);
        infoButtonClickContentBrowse = ConfigurationEngine.setString(
                List.of("Content browse message sent when info button is clicked"),
                file, fileConfiguration, "infoButtonClickContentBrowse", "&7Browse content: ", true);
        infoButtonClickContentBrowseSuffix = ConfigurationEngine.setString(
                List.of("Text between the /em setup command and the content link"),
                file, fileConfiguration, "infoButtonClickContentBrowseSuffix", " &8or ", true);
        infoButtonClickDiscord = ConfigurationEngine.setString(
                List.of("Discord link sent when info button is clicked"),
                file, fileConfiguration, "infoButtonClickDiscord", "&7Discord: ", true);

        // Recommended preset
        recommendedPresetName = ConfigurationEngine.setString(
                List.of("Display name for the recommended preset button"),
                file, fileConfiguration, "recommendedPresetName", "<g:#228B22:#32CD32>Recommended Preset</g> &7- Full Install", true);
        recommendedPresetLore1 = ConfigurationEngine.setString(
                List.of("First lore line for the recommended preset"),
                file, fileConfiguration, "recommendedPresetLore1", "&7Enables all EliteMobs features.", true);
        recommendedPresetLore2 = ConfigurationEngine.setString(
                List.of("Second lore line for the recommended preset"),
                file, fileConfiguration, "recommendedPresetLore2", "&7Custom items, MMORPG progression", true);
        recommendedPresetLore3 = ConfigurationEngine.setString(
                List.of("Third lore line for the recommended preset"),
                file, fileConfiguration, "recommendedPresetLore3", "&7& naturally spawning elites.", true);
        recommendedPresetLore4 = ConfigurationEngine.setString(
                List.of("Fourth lore line for the recommended preset"),
                file, fileConfiguration, "recommendedPresetLore4", "&7Works with &fBetterStructures&7.", true);
        recommendedPresetLore5 = ConfigurationEngine.setString(
                List.of("Fifth lore line for the recommended preset"),
                file, fileConfiguration, "recommendedPresetLore5", "&aClick to select!", true);
        recommendedPresetClickConfirm = ConfigurationEngine.setString(
                List.of("Confirmation message sent after selecting recommended preset"),
                file, fileConfiguration, "recommendedPresetClickConfirm", "<g:#228B22:#32CD32>Recommended installation</g> &7activated!", true);
        recommendedPresetClickNightbreakLogin = ConfigurationEngine.setString(
                List.of("Nightbreak login prompt sent after selecting recommended preset"),
                file, fileConfiguration, "recommendedPresetClickNightbreakLogin", "<g:#B8860B:#DAA520>Step 1:</g> &7Link your Nightbreak account: ", true);
        recommendedPresetClickDownloadAll = ConfigurationEngine.setString(
                List.of("In-game content management message sent after selecting recommended preset"),
                file, fileConfiguration, "recommendedPresetClickDownloadAll", "<g:#B8860B:#DAA520>Step 2:</g> &7Install & manage content in-game with ", true);
        recommendedPresetClickDownloadAllSuffix = ConfigurationEngine.setString(
                List.of("Text after the /em setup command in the recommended preset"),
                file, fileConfiguration, "recommendedPresetClickDownloadAllSuffix", "&7!", true);
        recommendedPresetClickSetupGuide = ConfigurationEngine.setString(
                List.of("Browse content link sent after selecting recommended preset"),
                file, fileConfiguration, "recommendedPresetClickSetupGuide", "&7Browse all content: ", true);
        recommendedPresetClickDiscord = ConfigurationEngine.setString(
                List.of("Manual install note sent after selecting recommended preset"),
                file, fileConfiguration, "recommendedPresetClickDiscord", "&8Or install manually (less convenient): ", true);
        recommendedPresetClickWiki = ConfigurationEngine.setString(
                List.of("Discord link sent after selecting recommended preset"),
                file, fileConfiguration, "recommendedPresetClickWiki", "&7Need help? ", true);

        // Content-only preset
        contentOnlyPresetName = ConfigurationEngine.setString(
                List.of("Display name for the content-only preset button"),
                file, fileConfiguration, "contentOnlyPresetName", "<g:#DAA520:#F0C040>Content-Only Preset</g>", true);
        contentOnlyPresetLore1 = ConfigurationEngine.setString(
                List.of("First lore line for the content-only preset"),
                file, fileConfiguration, "contentOnlyPresetLore1", "<g:#8B0000:#CC0000>Disables natural elite spawns.</g>", true);
        contentOnlyPresetLore2 = ConfigurationEngine.setString(
                List.of("Second lore line for the content-only preset"),
                file, fileConfiguration, "contentOnlyPresetLore2", "&7Keeps installed content and", true);
        contentOnlyPresetLore3 = ConfigurationEngine.setString(
                List.of("Third lore line for the content-only preset"),
                file, fileConfiguration, "contentOnlyPresetLore3", "&7the MMORPG progression system.", true);
        contentOnlyPresetLore4 = ConfigurationEngine.setString(
                List.of("Fourth lore line for the content-only preset"),
                file, fileConfiguration, "contentOnlyPresetLore4", "&7Works with &fBetterStructures&7.", true);
        contentOnlyPresetLore5 = ConfigurationEngine.setString(
                List.of("Fifth lore line for the content-only preset"),
                file, fileConfiguration, "contentOnlyPresetLore5", "&eClick to select!", true);
        contentOnlyPresetClickConfirm = ConfigurationEngine.setString(
                List.of("Confirmation message sent after selecting content-only preset"),
                file, fileConfiguration, "contentOnlyPresetClickConfirm", "<g:#DAA520:#F0C040>Content-only installation</g> &7activated!", true);
        contentOnlyPresetClickNightbreakLogin = ConfigurationEngine.setString(
                List.of("Nightbreak login prompt sent after selecting content-only preset"),
                file, fileConfiguration, "contentOnlyPresetClickNightbreakLogin", "<g:#B8860B:#DAA520>Step 1:</g> &7Link your Nightbreak account: ", true);
        contentOnlyPresetClickDownloadAll = ConfigurationEngine.setString(
                List.of("In-game content management message sent after selecting content-only preset"),
                file, fileConfiguration, "contentOnlyPresetClickDownloadAll", "<g:#B8860B:#DAA520>Step 2:</g> &7Install & manage content in-game with ", true);
        contentOnlyPresetClickDownloadAllSuffix = ConfigurationEngine.setString(
                List.of("Text after the /em setup command in the content-only preset"),
                file, fileConfiguration, "contentOnlyPresetClickDownloadAllSuffix", "&7!", true);
        contentOnlyPresetClickSetupGuide = ConfigurationEngine.setString(
                List.of("Browse content link sent after selecting content-only preset"),
                file, fileConfiguration, "contentOnlyPresetClickSetupGuide", "&7Browse all content: ", true);
        contentOnlyPresetClickDiscord = ConfigurationEngine.setString(
                List.of("Manual install note sent after selecting content-only preset"),
                file, fileConfiguration, "contentOnlyPresetClickDiscord", "&8Or install manually (less convenient): ", true);
        contentOnlyPresetClickWiki = ConfigurationEngine.setString(
                List.of("Discord link sent after selecting content-only preset"),
                file, fileConfiguration, "contentOnlyPresetClickWiki", "&7Need help? ", true);

        // Nothing preset
        nothingPresetName = ConfigurationEngine.setString(
                List.of("Display name for the nothing preset button"),
                file, fileConfiguration, "nothingPresetName", "<g:#8B0000:#CC0000>No Progression Preset</g> &8- Not Recommended", true);
        nothingPresetLore1 = ConfigurationEngine.setString(
                List.of("First lore line for the nothing preset"),
                file, fileConfiguration, "nothingPresetLore1", "&cDisables elite spawns, loot drops,", true);
        nothingPresetLore2 = ConfigurationEngine.setString(
                List.of("Second lore line for the nothing preset"),
                file, fileConfiguration, "nothingPresetLore2", "&cthe skill system, and leveling.", true);
        nothingPresetLore3 = ConfigurationEngine.setString(
                List.of("Third lore line for the nothing preset"),
                file, fileConfiguration, "nothingPresetLore3", "&4&lNot recommended! &cMost features disabled.", true);
        nothingPresetLore4 = ConfigurationEngine.setString(
                List.of("Fourth lore line for the nothing preset"),
                file, fileConfiguration, "nothingPresetLore4", "&7Works with &fBetterStructures&7.", true);
        nothingPresetLore5 = ConfigurationEngine.setString(
                List.of("Fifth lore line for the nothing preset"),
                file, fileConfiguration, "nothingPresetLore5", "&7Click to select.", true);
        nothingPresetLore6 = ConfigurationEngine.setString(
                List.of("Sixth lore line for the nothing preset"),
                file, fileConfiguration, "nothingPresetLore6", "", true);
        nothingPresetClickConfirm = ConfigurationEngine.setString(
                List.of("Confirmation message sent after selecting nothing preset"),
                file, fileConfiguration, "nothingPresetClickConfirm", "<g:#8B0000:#CC0000>No progression</g> &7mode activated.", true);
        nothingPresetClickWarning = ConfigurationEngine.setString(
                List.of("Warning message sent after selecting nothing preset"),
                file, fileConfiguration, "nothingPresetClickWarning", "&c&lWarning: &7Elite spawns, loot, skills, and leveling are all &cdisabled&7. Most EliteMobs features will not work.", true);
        nothingPresetClickBetterStructures = ConfigurationEngine.setString(
                List.of("BetterStructures note sent after selecting nothing preset"),
                file, fileConfiguration, "nothingPresetClickBetterStructures", "&7Content from &fBetterStructures &7will still function normally.", true);
        nothingPresetClickSetupGuide = ConfigurationEngine.setString(
                List.of("Setup guide link sent after selecting nothing preset"),
                file, fileConfiguration, "nothingPresetClickSetupGuide", "&7Need help? ", true);

        // First-time login listener
        loginHeader = ConfigurationEngine.setString(
                List.of("Header for the first-time login message"),
                file, fileConfiguration, "loginHeader", "&fWelcome to <g:#228B22:#32CD32>EliteMobs</g>&f, by <g:#CC4400:#DAA520>MagmaGuy</g>&f!", true);
        loginWelcome = ConfigurationEngine.setString(
                List.of("Welcome message for the first-time login"),
                file, fileConfiguration, "loginWelcome", "<g:#2E8B57:#3CB371>Dungeons</g>&7, <g:#CD7F32:#FFD700>events</g>&7, <g:#4A7A9A:#6A9ABA>arenas</g> &7& <g:#7B2FBE:#A855F7>more</g>&7!", true);
        loginHelpHint = ConfigurationEngine.setString(
                List.of("Help hint for the first-time login"),
                file, fileConfiguration, "loginHelpHint", "&7Setup required! Run ", true);
        loginHelpHintSuffix = ConfigurationEngine.setString(
                List.of("Text after the /em initialize command in the help hint"),
                file, fileConfiguration, "loginHelpHintSuffix", " &7to get started.", true);
        loginDiscord = ConfigurationEngine.setString(
                List.of("Discord link for the first-time login"),
                file, fileConfiguration, "loginDiscord", "&7Need help? Reach out on ", true);
        loginDismiss = ConfigurationEngine.setString(
                List.of("Dismiss instruction for the first-time login"),
                file, fileConfiguration, "loginDismiss", "&8This message will stop appearing after setup.", true);
        loginLanguagePrefix = ConfigurationEngine.setString(
                List.of("Prefix label before the language selector in the first-time login message"),
                file, fileConfiguration, "loginLanguagePrefix", "&7Language: ", true);

        // Shared clickable link settings
        discordLinkDisplay = ConfigurationEngine.setString(
                List.of("Display text for the clickable Discord link"),
                file, fileConfiguration, "discordLinkDisplay", "&9&nDiscord", true);
        discordLinkHover = ConfigurationEngine.setString(
                List.of("Hover tooltip for the Discord link"),
                file, fileConfiguration, "discordLinkHover", "&7Click to join the Discord!", true);
        accountLinkDisplay = ConfigurationEngine.setString(
                List.of("Display text for the clickable Nightbreak account link"),
                file, fileConfiguration, "accountLinkDisplay", "&9&nnightbreak.io/account", true);
        accountLinkHover = ConfigurationEngine.setString(
                List.of("Hover tooltip for the Nightbreak account link"),
                file, fileConfiguration, "accountLinkHover", "&7Click to open the Nightbreak account page", true);
        contentLinkDisplay = ConfigurationEngine.setString(
                List.of("Display text for the clickable content browse link"),
                file, fileConfiguration, "contentLinkDisplay", "&9&nnightbreak.io/elitemobs", true);
        contentLinkHover = ConfigurationEngine.setString(
                List.of("Hover tooltip for the content browse link"),
                file, fileConfiguration, "contentLinkHover", "&7Click to browse EliteMobs content", true);
        wikiLinkDisplay = ConfigurationEngine.setString(
                List.of("Display text for the clickable wiki link"),
                file, fileConfiguration, "wikiLinkDisplay", "&8&nwiki.nightbreak.io", true);
        wikiLinkHover = ConfigurationEngine.setString(
                List.of("Hover tooltip for the wiki link"),
                file, fileConfiguration, "wikiLinkHover", "&7Click to open the setup wiki", true);

        // Shared clickable command settings
        emInitializeDisplay = ConfigurationEngine.setString(
                List.of("Display text for the clickable /em initialize command"),
                file, fileConfiguration, "emInitializeDisplay", "&a&n/em initialize", true);
        emInitializeHover = ConfigurationEngine.setString(
                List.of("Hover tooltip for the /em initialize command"),
                file, fileConfiguration, "emInitializeHover", "&7Click to run /em initialize", true);
        emSetupDisplay = ConfigurationEngine.setString(
                List.of("Display text for the clickable /em setup command"),
                file, fileConfiguration, "emSetupDisplay", "&a&n/em setup", true);
        emSetupHover = ConfigurationEngine.setString(
                List.of("Hover tooltip for the /em setup command"),
                file, fileConfiguration, "emSetupHover", "&7Click to run /em setup", true);
        emCommandDisplay = ConfigurationEngine.setString(
                List.of("Display text for the clickable /em command"),
                file, fileConfiguration, "emCommandDisplay", "&a&n/em", true);
        emCommandHover = ConfigurationEngine.setString(
                List.of("Hover tooltip for the /em command"),
                file, fileConfiguration, "emCommandHover", "&7Click to open the EliteMobs menu", true);
        emLootDisplay = ConfigurationEngine.setString(
                List.of("Display text for the clickable /em loot command"),
                file, fileConfiguration, "emLootDisplay", "&a&n/em loot", true);
        emLootHover = ConfigurationEngine.setString(
                List.of("Hover tooltip for the /em loot command"),
                file, fileConfiguration, "emLootHover", "&7Click to vote on loot", true);
    }
}
