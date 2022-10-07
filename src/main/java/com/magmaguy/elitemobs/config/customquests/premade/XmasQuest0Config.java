package com.magmaguy.elitemobs.config.customquests.premade;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class XmasQuest0Config extends CustomQuestsConfigFields {
    public XmasQuest0Config() {
        super("xmas_quest_0",
                true,
                Map.of("Objective1",Map.of("objectiveType", "FETCH_ITEM", "filename", "xmas_lost_present.yml", "amount", 10, "itemName", "Lost Present")),
                Arrays.asList(
                        "filename=xmas_treat.yml:amount=10:chance=.3",
                        "filename=xmas_treat_v2.yml:amount=1:chance=.3",
                        "filename=xmas_and_gold.yml:amount=1:chance=.2",
                        "filename=xmas_and_goooooold.yml:amount=1:chance=.2",
                        "filename=xmas_silver.yml:amount=1:chance=.2",
                        "filename=xmas_siiiiilver.yml:amount=1:chance=.2",
                        "filename=xmas_peppermint.yml:amount=1:chance=.1",
                        "filename=xmas_cornelius_pick.yml:amount=1:chance=.1",
                        "filename=xmas_substitute_elf_practice.yml:amount=1:chance=.1",
                        "currencyAmount=300:amount=1:chance=.3",
                        "currencyAmount=300:amount=1:chance=.3",
                        "currencyAmount=300:amount=1:chance=.3"
                ),
                10,
                "&2Find the lost gifts!",
                Arrays.asList("I seem to have lost some Christmas gifts along the way, would you help a poor old man find them again?",
                        "I will let you open one of them in return!"));
        super.setQuestAcceptDialog(List.of("&8[&cSaint Nick&8]&f Thank you! I will be waiting here for you!"));
        super.setQuestCompleteDialog(List.of("&8[&cSaint Nick&8]&f Thank you for your help! " +
                "There are still some lost gifts out there, talk to me again if you want to help out more!"));
        super.setTemporaryPermissions(List.of("elitequest.xmas_quest.yml"));
    }
}
