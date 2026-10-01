package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class HallowedHauntTeleporter extends NPCsConfigFields {
    // Earlier builds shipped this skin with its signature cut short, which also conflicted with the Hallowed Haunt
    // content's Alaric NPC that shares the custom:em_the_hallowed_haunt_alaric alias.
    private static final String LEGACY_CUSTOM_DISGUISE_DATA = "player em_the_hallowed_haunt_alaric setskin {\"uuid\":\"899defd5-a0c9-4fa1-ac81-c24d597a2553\",\"name\":\"Unknown\",\"textureProperties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTc1NDg4MjkzOTUzNiwKICAicHJvZmlsZUlkIiA6ICJkYjQwYmNjNWUzMDE0ZmZjOGVlOWQxNDU5MTcyYjdhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJhWGUxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xODQ5NTI1NmZiZWViYjBkY2M3MTI4Mjg2MTljMzBjNTBmNjM1MGZjOWZlM2E4ZGQ4YjI5YWY2YmJkNTEyMDU0IgogICAgfQogIH0KfQ==\",\"signature\":\"FLLx/2pMyQYHZlHWR33WttxatUO9su4sUJ/NO0xFXLEla\"}]}";
    private static final String CUSTOM_DISGUISE_DATA = "player em_the_hallowed_haunt_alaric setskin {\"uuid\":\"899defd5-a0c9-4fa1-ac81-c24d597a2553\",\"name\":\"Unknown\",\"textureProperties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTc1NDg4MjkzOTUzNiwKICAicHJvZmlsZUlkIiA6ICJkYjQwYmNjNWUzMDE0ZmZjOGVlOWQxNDU5MTcyYjdhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJhWGUxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xODQ5NTI1NmZiZWViYjBkY2M3MTI4Mjg2MTljMzBjNTBmNjM1MGZjOWZlM2E4ZGQ4YjI5YWY2YmJkNTEyMDU0IgogICAgfQogIH0KfQ==\",\"signature\":\"FLLx/2pMyQYHZlHWR33WttxatUO9su4sUJ/NO0xFXLElaebpl2Vw+ie/Gj/RoJP3dnqRpdvWVO4O9+1V61Ur9vnKrN8ptP8nYC9DoJ1vzlo5tV/1019RjDc8hzEkY9d7oWtWjSznn+jDGUypZ/6W322/ONKPm1ZXRICve77QlRS/Fdwt5R+NJHeFjJUMb7oOnEnXSvg6/PlgQ+XuQF5susqhxuAaRIWys45tNYqw66WyLfcMcdfWvJr5TMoJK7IEcxmryp7rUska0LkWzJepu/GQDizUDG0nLB7b+G9phmAtESOBXue+gO7INfI3BBqi5Fej4nEn4iYjQwTPYjIlJnVIhI9f4fpQABwBYXDnQtHJiILxDqA6IWzGI2sE3PwCpeGsxwx5cNftY26HGpciC6szKscWkqWZIagRyXZ4tQ61nRxERHIbcYNbGludUDa07R0CvXIyUdWwuIg61eP7RIO3SzDoEBkpxspkkY5VNCe/IoC1/zXnqTpZcdilEiNXVi9jBwZ1Yv1+0VmZT2mKhayF6g+FXw7FGETCwaLmpRjy2dZz2qj/A+dLktJ1DK49Vtqg4NnnYCeNoNo/ymSuAZq2JQGvCM6QYCCjEw//Grq32+ol6i+tR0hju7qN6aCBvTG8dUjzJ8kdMpSYqNcfOkPRjqtV4ty9VvJmyvIfwIQ=\"}]}";

    public HallowedHauntTeleporter() {
        super("hallowed_haunt_teleporter",
                true,
                "<g:#9A8AAA:#AA9ABA>Alaric Greystone</g>",
                "<g:#8A7A9A:#9A8AAA><Hallowed Haunt></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,291.5,92,295.5,180,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_hallowed_haunt_dynamic_dungeon.yml");
        setCustomModel("em_ag_alaricgreystone");
        setDisguise("custom:em_the_hallowed_haunt_alaric");
        setCustomDisguiseData(CUSTOM_DISGUISE_DATA);
    }

    @Override
    protected void migrateLegacyDefaults() {
        migrateStringIfExact("customDisguiseData", LEGACY_CUSTOM_DISGUISE_DATA, CUSTOM_DISGUISE_DATA);
    }
}
