package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.utils.WarningMessage;

import java.util.ArrayList;
import java.util.List;

public class CustomObjectivesParser {
    /**
     * Processes the custom objectives as set in the configuration file. These objectives must follow one or more of these formats:
     * KILL_CUSTOM:filename=X.yml:amount=Y
     * FETCH_ITEM:filename=X.yml:amount=Y
     */
    public static List<Objective> processCustomObjectives(CustomQuest customQuest) {
        List<Objective> objectives = new ArrayList<>();
        for (String string : customQuest.getCustomQuestsConfigFields().getCustomObjectivesList()) {
            String[] rawStrings = string.split(":");
            switch (rawStrings[0]) {
                case "KILL_CUSTOM":
                    objectives.add(processObjectiveType(rawStrings, ObjectiveType.KILL_CUSTOM, customQuest));
                    break;
                case "FETCH_ITEM":
                    objectives.add(processObjectiveType(rawStrings, ObjectiveType.FETCH_ITEM, customQuest));
                    break;
                case "DIALOG":
                    objectives.add(processObjectiveType(rawStrings, ObjectiveType.DIALOG, customQuest));
                    break;
                default:
                    new WarningMessage("Entry " + string + " for quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " is not valid! Check the documentation on how to create valid quest objectives on the wiki!");
                    return new ArrayList<>();
            }
        }
        return objectives;
    }

    private static Objective processObjectiveType(String[] rawStrings, ObjectiveType objectiveType, CustomQuest customQuest) {
        String filename = null;
        String location = null;
        String dialog = null;
        String npcName = null;
        String key = null;
        String keyName = null;
        int amount = 1;
        for (String rawString : rawStrings) {
            String[] processedStrings = rawString.split("=");
            switch (processedStrings[0]) {
                case "filename":
                    filename = processedStrings[1];
                    break;
                case "amount":
                    try {
                        amount = Integer.parseInt(processedStrings[1]);
                    } catch (Exception ex) {
                        new WarningMessage("Invalid amount " + amount + " in entry " + rawString + " for Custom Quest " + filename + " . Defaulting to 1.");
                    }
                    break;
                case "location":
                    location = ChatColorConverter.convert(processedStrings[1]);
                    break;
                case "dialog":
                    dialog = ChatColorConverter.convert(processedStrings[1]);
                    break;
                case "npcName":
                    npcName = ChatColorConverter.convert(processedStrings[1]);
                    break;
                case "key":
                    key = processedStrings[1];
                    break;
                case "keyName":
                    keyName = processedStrings[1];
                    break;
            }
        }
        if (filename == null) {
            new WarningMessage("Invalid filename for entry " + rawStrings.toString() + " in Custom Quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " . This objective will not be registered.");
            return null;
        }
        try {
            if (objectiveType.equals(ObjectiveType.KILL_CUSTOM))
                return new CustomKillObjective(filename, amount, customQuest.getQuestLevel());
            else if (objectiveType.equals(ObjectiveType.FETCH_ITEM))
                return new CustomFetchObjective(amount, keyName, key);
            else if (objectiveType.equals(ObjectiveType.DIALOG))
                return new DialogObjective(filename, npcName, location, dialog);
        } catch (Exception ex) {
            new WarningMessage("Failed to register objective type for quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " ! This quest will be skipped");
        }

        return null;

    }

    private enum ObjectiveType {
        KILL_CUSTOM,
        FETCH_ITEM,
        DIALOG
    }
}
