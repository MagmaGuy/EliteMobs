package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import com.magmaguy.magmacore.nightbreak.NightbreakChangelogMessages;
import com.magmaguy.magmacore.nightbreak.NightbreakLogoutMessages;
import lombok.Getter;

import java.util.List;

/**
 * Configurable and translation-aware copy surrounding remote Nightbreak
 * changelogs. The release-note bodies themselves remain remote literal text.
 */
public class ChangelogsConfig extends ConfigurationFile {
    @Getter
    private static NightbreakChangelogMessages messages = NightbreakChangelogMessages.defaults();
    @Getter
    private static NightbreakLogoutMessages logoutMessages = NightbreakLogoutMessages.defaults();

    public ChangelogsConfig() {
        super("Changelogs.yml");
    }

    @Override
    public void initializeValues() {
        migrateWipVisualDefaults();
        messages = new NightbreakChangelogMessages(
                message("prefix", "&8[<g:#8B0000:#CC4400:#DAA520>◆</g> &f$plugin&8] &f",
                        "Prefix used by changelog chat messages. Placeholder: $plugin."),
                message("pendingNotice", "$prefix<g:#B8860B:#F0C040>New update notes are waiting.</g> ",
                        "Login reminder shown to administrators. Placeholder: $prefix."),
                message("viewButton", "<g:#267A78:#58B8A9>[VIEW CHANGELOG]</g>",
                        "Clickable button appended to the login reminder."),
                message("viewHover", "&7See what changed since your last review",
                        "Hover text for the view-changelog button."),
                message("noUnread", "$prefix&7There are no unread changelogs.",
                        "Shown when an administrator opens the inbox with nothing unread. Placeholder: $prefix."),
                message("dialogTitle", "<g:#8B0000:#CC4400:#DAA520>Update Notes</g> &f— $plugin",
                        "Title shown inside the changelog dialog. Placeholder: $plugin."),
                message("dialogExternalTitle", "$plugin Changelog",
                        "Title shown outside the changelog dialog. Placeholder: $plugin."),
                message("omittedDialog", "&7$count older tracked release(s) were omitted to keep this digest readable.",
                        "Dialog notice when more releases exist than fit on one page. Placeholder: $count."),
                message("releaseHeader", "$component $version\n",
                        "Text layout for each release heading. Placeholders: $component and $version.",
                        "The dialog applies its own safe heading style; color codes in this value are ignored."),
                message("dismissButton", "<g:#2E7D4F:#69C56F>Dismiss These Updates</g>",
                        "Dialog button that marks the displayed updates as read."),
                message("disableButton", "<g:#7A1F2B:#C2414A>Never Show Changelogs</g>",
                        "Dialog button that permanently disables reminders for that administrator."),
                message("disableTooltip", "&7Permanently hide update-note reminders for this plugin",
                        "Hover text for the permanent-disable button."),
                message("chatHeader", "&8&m---------------- <g:#8B0000:#CC4400:#DAA520>Update Notes</g> &f$plugin &8&m----------------",
                        "Header used by the chat fallback. Placeholder: $plugin."),
                message("omittedChat", "&7$count older release(s) were omitted from this digest.",
                        "Chat notice when more releases exist than fit on one page. Placeholder: $count."),
                message("chatInstructions", "&7Use &f/$command dismiss &7to dismiss these notes, or &f/$command disable &7to permanently hide reminders.",
                        "Controls shown at the end of the chat fallback. Placeholder: $command."),
                message("dismissedPage", "$prefix&7These update notes were dismissed. Older notes remain. ",
                        "Shown after dismissing one page when older unread pages remain. Placeholder: $prefix."),
                message("viewNextButton", "<g:#267A78:#58B8A9>[VIEW NEXT]</g>",
                        "Clickable button used to open the next unread page."),
                message("viewNextHover", "&7Open the next page of unread update notes",
                        "Hover text for the next-page button."),
                message("dismissedAll", "$prefix&7The currently available update notes were dismissed.",
                        "Shown after the final unread page is dismissed. Placeholder: $prefix."),
                message("disabled", "$prefix&7Changelog reminders have been permanently disabled for you.",
                        "Confirmation after an administrator permanently disables reminders. Placeholder: $prefix."),
                message("unavailable", "&cThe changelog service is not currently available.",
                        "Shown if the changelog command remains registered while its service is unavailable."),
                message("adminOnly", "&cThis command is only available to plugin administrators in-game.",
                        "Shown when a non-administrator or the console tries to open the changelog inbox."),
                message("commandDescription", "Views unread $plugin changelogs.",
                        "Description registered for the changelog command. Placeholder: $plugin."),
                message("commandUsage", "/$command [dismiss|disable]",
                        "Usage shown after an invalid changelog command. Placeholder: $command."));

        logoutMessages = new NightbreakLogoutMessages(
                message("logoutCommandDescription", "Remove the registered Nightbreak account token",
                        "Description registered for the shared /nightbreaklogout command."),
                message("logoutPermissionDenied", "&cYou don't have permission to use this command.",
                        "Shown when someone without nightbreak.login uses /nightbreaklogout."),
                message("logoutSuccess", "&aThis server is no longer connected to a Nightbreak account.",
                        "Shown after the shared Nightbreak account token is removed."),
                message("logoutReconnect", "&7Use &a/nightbreaklogin <token> &7to connect it again.",
                        "Shown after logout to explain how an administrator can reconnect the server."),
                message("logoutFailure", "&cThe shared Nightbreak account token could not be removed.",
                        "Shown when the shared Nightbreak account token could not be removed."));
    }

    /** Updates only stock values from this unreleased feature and leaves customized copy untouched. */
    private void migrateWipVisualDefaults() {
        migrateDefault("prefix", "&8[&6$plugin&8] &f", "&8[<g:#8B0000:#CC4400:#DAA520>◆</g> &f$plugin&8] &f");
        migrateDefault("pendingNotice", "$prefix&eNew update notes are waiting. ",
                "$prefix<g:#B8860B:#F0C040>New update notes are waiting.</g> ");
        migrateDefault("viewButton", "&a&l[VIEW CHANGELOG]", "<g:#267A78:#58B8A9>[VIEW CHANGELOG]</g>");
        migrateDefault("dialogTitle", "&6&l$plugin Update Notes", "<g:#8B0000:#CC4400:#DAA520>Update Notes</g> &f— $plugin");
        migrateDefault("dismissButton", "&aDismiss These Updates", "<g:#2E7D4F:#69C56F>Dismiss These Updates</g>");
        migrateDefault("disableButton", "&8Never Show Changelogs", "<g:#7A1F2B:#C2414A>Never Show Changelogs</g>");
        migrateDefault("chatHeader", "&8&m---------------- &6&l$plugin Update Notes &8&m----------------",
                "&8&m---------------- <g:#8B0000:#CC4400:#DAA520>Update Notes</g> &f$plugin &8&m----------------");
        migrateDefault("viewNextButton", "&a&l[VIEW NEXT]", "<g:#267A78:#58B8A9>[VIEW NEXT]</g>");
    }

    private void migrateDefault(String key, String previousDefault, String currentDefault) {
        if (previousDefault.equals(fileConfiguration.getString(key))) fileConfiguration.set(key, currentDefault);
    }

    private String message(String key, String defaultValue, String... comments) {
        return ConfigurationEngine.setString(List.of(comments), file, fileConfiguration,
                key, defaultValue, true);
    }
}
