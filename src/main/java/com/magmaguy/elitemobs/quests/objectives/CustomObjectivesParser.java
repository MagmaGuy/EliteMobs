package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WarningMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomObjectivesParser {
    /**
     * Processes the custom objectives as set in the configuration file. These objectives must follow one or more of these formats:
     * KILL_CUSTOM:filename=X.yml:amount=Y
     * FETCH_ITEM:filename=X.yml:amount=Y
     * ARENA:filename=X.yml
     */
    public static List<Objective> processCustomObjectives(CustomQuest customQuest) {
        List<Objective> objectives = new ArrayList<>();

        for (Map<String, Object> maps : customQuest.getCustomQuestsConfigFields().getCustomObjectives().values())
            objectives.add(processObjectiveType(maps, customQuest));
        /*
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
                case "ARENA":
                    objectives.add(processObjectiveType(rawStrings, ObjectiveType.ARENA, customQuest));
                default:
                    new WarningMessage("Entry " + string + " for quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " is not valid! Check the documentation on how to create valid quest objectives on the wiki!");
                    return new ArrayList<>();
            }
        }
         */
        return objectives;
    }

    private static Objective processObjectiveType(Map<String, Object> rawMap, CustomQuest customQuest) {
        ObjectiveType objectiveType = null;
        String filename = null;
        String location = null;
        List<String> dialog = null;
        String name = null;
        Integer amount = 1;
        for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
            switch (entry.getKey()) {
                case "objectiveType":
                    objectiveType = MapListInterpreter.parseEnum(entry.getKey(), entry.getValue(), ObjectiveType.class, customQuest.getConfigurationFilename());
                    break;
                case "filename":
                    filename = MapListInterpreter.parseString(entry.getKey(), entry.getValue(), customQuest.getConfigurationFilename());
                    break;
                case "amount":
                    amount = MapListInterpreter.parseInteger(entry.getKey(), entry.getValue(), customQuest.getConfigurationFilename());
                    if (amount == null) amount = 1;
                    break;
                case "location":
                    location = MapListInterpreter.parseString(entry.getKey(), entry.getValue(), customQuest.getConfigurationFilename());
                    break;
                case "dialog":
                    dialog = ChatColorConverter.convert(MapListInterpreter.parseStringList(entry.getKey(), entry.getValue(), customQuest.getConfigurationFilename()));
                    break;
                case "npcName":
                case "itemName":
                case "name":
                    name = ChatColorConverter.convert(MapListInterpreter.parseString(entry.getKey(), entry.getValue(), customQuest.getConfigurationFilename()));
                    break;
            }
        }
        if (filename == null) {
            new WarningMessage("Invalid filename for entry " + rawMap.toString() + " in Custom Quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " . This objective will not be registered.");
            return null;
        }
        try {
            if (objectiveType.equals(ObjectiveType.KILL_CUSTOM))
                return new CustomKillObjective(filename, amount, customQuest.getQuestLevel());
            else if (objectiveType.equals(ObjectiveType.FETCH_ITEM))
                return new CustomFetchObjective(amount, name, filename);
            else if (objectiveType.equals(ObjectiveType.DIALOG))
                return new DialogObjective(filename, name, location, dialog);
        } catch (Exception ex) {
            new WarningMessage("Failed to register objective type for quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " ! This quest will be skipped");
            new WarningMessage("Invalid entry: " + rawMap.toString());
            ex.printStackTrace();
        }

        return null;

    }

    private enum ObjectiveType {
        KILL_CUSTOM,
        FETCH_ITEM,
        DIALOG,
        ARENA
    }
}
