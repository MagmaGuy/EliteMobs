package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class QuestGiverConfig extends NPCsConfigFields {
    public QuestGiverConfig() {
        super("quest_giver",
                true,
                "Qel'Thuzad",
                "<Quest Giver>",
                Villager.Profession.FLETCHER,
                "em_adventurers_guild,278.5,91,215.5,0,0",
                new ArrayList<>(List.of(
                        "Greetings, adventurer!\\nFancy a quest?",
                        "You! I've got a quest!",
                        "Feeling... adventurous?")),
                new ArrayList<>(List.of(
                        "Complete guild quests\\nfor cool rewards!",
                        "Higher tier quests have\\nbetter rewards!",
                        "Higher tier quests make\\nyou hunt higher level mobs!",
                        "Want a harder challenge?\\nIncrease your guild rank!",
                        "Make sure you're well equipped\\nfor these quests!")),
                new ArrayList<>(List.of(
                        "Safe travels, friend.",
                        "Happy hunting!",
                        "Live long and prosper!",
                        "Come back with your shield,\\n or on it.",
                        "Life before death!",
                        "Strength before weakness!",
                        "Journey before destination!")),
                true,
                3,
                NPCInteractions.NPCInteractionType.QUEST_GIVER);
        setDisguise("custom:ag_quest_giver");
        setCustomDisguiseData("player ag_quest_giver setskin {\"id\":\"f376b40d-e47a-4b75-9b4e-cde815b00ee7\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3ODEyNjEwNjU1MiwKICAicHJvZmlsZUlkIiA6ICJlZDU0NDIxMDI0MTM0YjQ5YWQ5MjRmZDEzYWE2YWNiMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXJrdXpfRGlheiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lYzc5NDJhYzExN2ViOTBmMTU5OTk1NWUzZjE1YjVmZTY4ODA0MWRlZWZmZmFkZDA5NDU2ZWZkNmFkNDkzNzBhIgogICAgfQogIH0KfQ==\",\"signature\":\"nHC4UYgMKgLiigQiEji57aSvQM+2vbeJmlKVf+WxnypQjh8c7+JhW1GjCPGK+d0H43wuT4V6G6pwps65rphnUAW0jhuf7G0wwJ8U/wYTeaDN+lNau0ssrBfufWmkAOGywpUHjrS4xfSxKRdBU4sKmjFoeCwOfRDdYPmgd7i7cSZqoWTuycFcwK9tfxuHGTflLFSfwKP5kul+TllapaQtcxm+tnYRWAwlSD0g6w4tbBqKvjKBF/hNm6sLEFc1Epuj7xXvmcOkXzmR3tDiA7Hnpam8YJHQmTFLhtUbNQ/H4bWVnWEeKAf1t8RbD96mdg56/ETl1+cQP3vjK6PPByqLLsEE+Rcwe0OgO3akMfiAkUDsyTKwCafs0exmjFTZqU5DlUpRCxXqw7QBwqajaBXqTCvazKxW9+00uWIGrdC7DJcpkATQF/BPeyDa/5n7UPCXjWIU/Qs3qHFKUGyTk+1H1CyBBS6djJynNJAPJQnogH5isMRjYw2JPwuCdzeQHvWpKxBtW9mxNCxvvpluVHwZsTthGNP3aM76D35hnm/xfcNnhkSKUbrEFVYAWGNlnOq1GVsLPbjuE+73t9rJ1w8PK+6nqOCqbw3cpALXZcL4NadeZWURlZ5M/+1HNwzhIj4Cm8nXvyJv1+NHZXL3EL33xkjBkJkwvXhO2dSiMdeDhQQ=\"}],\"legacy\":false}");
    }
}
