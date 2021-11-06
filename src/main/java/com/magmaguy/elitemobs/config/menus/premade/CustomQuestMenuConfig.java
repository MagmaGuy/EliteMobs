package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;

public class CustomQuestMenuConfig extends MenusConfigFields {
    public static String headerTextLines;
    public static String acceptTextLines, acceptHoverLines, acceptCommandLines;
    public static String acceptedTextLines, acceptedHoverLines, acceptedCommandLines;
    public static String ongoingColorCode, completedColorCode;
    private static String killQuestDefaultSummaryLine, fetchQuestDefaultSummaryLine;

    public CustomQuestMenuConfig() {
        super("custom_quest_screen", true);
    }

    public static String getKillQuestDefaultSummaryLine(Objective objective) {
        String newString = killQuestDefaultSummaryLine;
        newString = newString.replace("$name", ((KillObjective) objective).getEntityName());
        newString = newString.replace("$current", ((KillObjective) objective).getCurrentAmount() + "");
        newString = newString.replace("$target", ((KillObjective) objective).getTargetAmount() + "");
        if (!objective.isObjectiveCompleted())
            return newString.replace("$color", ongoingColorCode);
        else
            return newString.replace("$color", completedColorCode);
    }

    @Override
    public void processAdditionalFields() {
        headerTextLines = ConfigurationEngine.setString(fileConfiguration, "headerTextLines",
                ChatColorConverter.convert("&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n&c&l$questName\n&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n"));

        acceptTextLines = ConfigurationEngine.setString(fileConfiguration, "acceptTextLines", "&a&lAccept!");
        acceptHoverLines = ConfigurationEngine.setString(fileConfiguration, "acceptHoverLines", "&aClick to \n&aaccept quest!");
        acceptCommandLines = ConfigurationEngine.setString(fileConfiguration, "acceptCommandLines", "/em quest accept $questID");

        acceptedTextLines = ConfigurationEngine.setString(fileConfiguration, "acceptedTextLines", "&2&lAccepted! &4[Abandon]");
        acceptedHoverLines = ConfigurationEngine.setString(fileConfiguration, "acceptedHoverLines", "&aClick to abandon quest!");
        acceptedCommandLines = ConfigurationEngine.setString(fileConfiguration, "acceptedCommandLines", "/em quest leave $questID");

        killQuestDefaultSummaryLine = ConfigurationEngine.setString(fileConfiguration, "killQuestDefaultSummaryLine", ">&cKill $name:$color$current&0/$color$target");
        fetchQuestDefaultSummaryLine = ConfigurationEngine.setString(fileConfiguration, "fetchQuestDefaultSummaryLine", ">&cGet $name:$color&$current&0/$color$target");
        ongoingColorCode = ConfigurationEngine.setString(fileConfiguration, "ongoingQuestColorCode", "&c");
        completedColorCode = ConfigurationEngine.setString(fileConfiguration, "ongoingQuestColorCode", "&2");

    }

}
