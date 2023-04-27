package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class CraftenminesLabSanctumTeleporterConfig  extends NPCsConfigFields {
    public CraftenminesLabSanctumTeleporterConfig() {
        super("craftenmines_lab_sanctum_teleporter",
                true,
                "Sonia the Speedy",
                "<[30] Craftenmine's Laboratory Teleport>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,296.5,91,217.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp craftenmines_lab_sanctum.yml");
        setDisguise("custom:elitemobs_craftenmines_laboratory_teleporter");
        setCustomDisguiseData("player elitemobs_craftenmines_laboratory_teleporter setskin {\"id\":\"bfe398ba-f41a-48f1-80d7-7513002dacf1\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY4MjYzMzE1ODc1MywKICAicHJvZmlsZUlkIiA6ICJlN2E1OWU2Yzk1NTY0M2IxYTYxZGI2NzNkNTA3ZjE5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJZSmNhdCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84ZDY4ODNkYThjM2UzYjI1ZDQ1ODJhYjdhMzA2MzQ1ZDhiOTY5ZTg1MzI1ZDE2Zjg3OTY3MjE2MzgzMDY4NDgwIgogICAgfQogIH0KfQ==\",\"signature\":\"RXEyj+rpRtydqtNSntHcQmJhAvFxCLeI8d7ErnEjbVLMpNnv+SIrjMCplT+T32EW8qTny+LVtqd+xibNLYzqkmOcNBzwga5inYqEGpQ40XEbGycLYxFV8r/L8+oaG9b36/szAWMqWzWbFOUFDk2CwJEhR8sYOOq1vfWshpONl38BoaM78F7GTM0ck/J3VBDF/hiwD5GwDlQ5O2hDofwsMay7fE7ilwQ/6i4eBWW0PlCtoTwF5dvdvG04MSs4tF9FF5KTF8BlulOq41qnGVHmyu1VAQdKNk7HHqudSII2aiV2x0rhw10dgzPvPllu5qI1KMozF07YLdv3C5j53CLewnLCDje9HGIuNmImGkJ8hFnVex/hGDeX5FY9IRC+9kLULzrVjdVj3q/V42HouN5HrboI/4dicMveQbZ8aBoPjtX7MCH/6dCxnRZJG47AQSQs8/xCcO7n1xEJcwLx7FuxVKmSzie91LCcyCsCsZ7Qm9MvtVjRyqYYGaTbr+Mht0czG3qUGMyw7iaT4PWmEbCjLgU46S35kAmTDNQLZmZzF5JOHECijQqGtTeNgJIwNdNDdM6AFPzhnFa85ubdRLa86Fv5FhLehJ8ySz9+xBXAecuUr3NOmG0AFQLklDw9HcGHJ7Jr0MNM+LkJdNMVSymZNJaT21o4Qa9fJNq6GdSTpP4=\"}],\"legacy\":false}");
    }
}
