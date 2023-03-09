package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class UnbinderConfig extends NPCsConfigFields {
    public UnbinderConfig() {
        super("unbinder",
                true,
                "Ulfric",
                "<Unbinder>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,296.5,81,253.5,90,0",
                Arrays.asList(
                        "Greetings.",
                        "Well met.",
                        "Hail, friend.",
                        "Yes?",
                        "May I help you?"),
                Arrays.asList(
                        "I will unbind your items\\nfor an extremely rare unbind scroll.",
                        "I am the only one qualified\\nto unbind your goods.",
                        "Have an unbind scroll?"),
                Arrays.asList(
                        "Remember: Use unbind\\nscrolls wisely.",
                        "Safe travels, friend."),
                true,

                3,
                NPCInteractions.NPCInteractionType.UNBINDER);
        setDisguise("custom:ag_unbinder");
        setCustomDisguiseData("player ag_unbinder setskin {\"id\":\"2bf6acbe-178e-4198-aed8-32428e461ae4\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTYxNTkxMzY0NjM4MSwKICAicHJvZmlsZUlkIiA6ICI5ZDQyNWFiOGFmZjg0MGU1OWM3NzUzZjc5Mjg5YjMyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUb21wa2luNDIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZhZWIwMDZmMmRiMDY1ZjQ5ZmQwMzdjODQ0YWYzMTdmYjllZTI0Y2U2YjM3NWJhYjk2YzgyNTRjN2EwOWRiNiIKICAgIH0KICB9Cn0=\",\"signature\":\"iFzPu0XR6tKMFtW5optWWWPbBpe9zr92BcwDb/4ietoDII8ErHouGKzQuvkbBPDrHm3BVOZHpaMhW6Ks5hPhaHoAGAv3AZHE/h6MwXZU3cJtfqlLVN0EXgQwCh3iHApzd73IZPng1q8/C8YsHDKJB6JHUWQAzj0ohfUoW4jJJiDJ6+h22eUdiZ/mc7nIRUSdgtztAOvAT7i6kcnTq+Bxx6Iw/va3kE7cZmgHkaVKOYpPyxNNNYZE3/4V/gaSljPj3l3Z3hjPGVC1AD2eQK1RRClJKoiAfihj7BLw0mt2EJuTiJ3lWAda8ubjKetfB2AlWR7FLLbSdGh3fpkpVpq/2sis/nW3rRnHo4wZuzv387aIQj7YEpvH0YqB5sF7zF+qSqfp8ibipqbJqzn3F+P0jYivcmwToxFmeTUtON0Os3PI72ckAQumkt4ra6Uz7WMxMJlVg0umPcB0WwEx4JN13qqT34YLcj7IQS/x3gbpH/VxI0oE+jPS79ZhW3SjFcwbawgxA3tRGmqlO40/ImEePcbx8HrWkLHGbi/6KH9hcPSyQk1qm9bcI+TJj0jE2pIZk2Oi2kFbaLzpV5q7FWDpalTLBP12QlsGGkfmlVm7o8ASytULJk0Gqa34r4SYTr9Hx04hOOgUu/70ge1IzwJ5Gq2kqv89Gx+iNyaSaYC2mqQ=\"}],\"legacy\":false}");
    }
}
