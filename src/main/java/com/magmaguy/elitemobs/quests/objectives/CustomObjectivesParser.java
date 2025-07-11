package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;

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

        if (objectiveType== ObjectiveType.FETCH_ITEM && name == null) {
            try{
                name = CustomItem.getCustomItem(filename).getCustomItemsConfigFields().getName();
            } catch (Exception ex){
                Logger.warn("Failed to get name for custom item " + filename + " in Custom Quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " . This objective will not display the item name.");
            }
        }

        if (filename == null) {
            Logger.warn("Invalid filename for entry " + rawMap + " in Custom Quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " . This objective will not be registered.");
            return null;
        }
        try {
            if (objectiveType.equals(ObjectiveType.KILL_CUSTOM))
                return new CustomKillObjective(filename, amount, customQuest.getQuestLevel());
            else if (objectiveType.equals(ObjectiveType.FETCH_ITEM))
                return new CustomFetchObjective(amount, name, filename);
            else if (objectiveType.equals(ObjectiveType.DIALOG))
                return new DialogObjective(filename, name, location, dialog);
            else if (objectiveType.equals(ObjectiveType.ARENA))
                return new ArenaObjective(name, filename);
        } catch (Exception ex) {
            Logger.warn("Failed to register objective type for quest " + customQuest.getCustomQuestsConfigFields().getFilename() + " ! This quest will be skipped");
            Logger.warn("Invalid entry: " + rawMap);
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
