package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class EnchanterConfig extends NPCsConfigFields {
    public EnchanterConfig() {
        super("enchanted",
                true,
                "<g:#6A4A7A:#7A5A8A>Eden</g>",
                "<g:#5A3A6A:#6A4A7A><Enchanter></g>",
                Villager.Profession.TOOLSMITH,
                "em_adventurers_guild,284.5,74,246.5,0,0",
                new ArrayList<>(List.of(
                        "Need something enchanted?",
                        "Feeling lucky?",
                        "Got an item to improve?",
                        "Got enchanted books?")),
                new ArrayList<>(List.of(
                        "Enchantment results are\\nnot guaranteed!",
                        "Don't complain if it fails!",
                        "Got an enchanted book?",
                        "Higher quality items are\\nriskier to enchant!")),
                new ArrayList<>(List.of(
                        "Got what you wanted!",
                        "How's your karma?",
                        "If at first you fail,\\ntry and try again!")),
                true,
                3,
                NPCInteractions.NPCInteractionType.ENCHANTER);
        setDisguise("custom:ag_enchanter");
        setCustomDisguiseData("player ag_enchanter setskin {\"id\":\"c579c972-54c4-4ebc-8530-6018fd9405b8\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTYzMzIzODI1MTc3MCwKICAicHJvZmlsZUlkIiA6ICJiNjc3NTgwYzExYmU0ZjNiODI1OGM0YjBkNzNhNzg0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJPZmZpY2lhbGx5SksiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTk1M2QyZDg0MDVhYWQxM2QyZmUwN2Y0Mzg1NmFkODAxZjQzZWI2MTBlYzAwMTJiNDMxMzk2ZTYzMTgzYjkyZiIKICAgIH0KICB9Cn0=\",\"signature\":\"t1aaf/0C7HtS+U+tdfK4p5Q4T9xowTNCxxhZCqLVqbROYu3ch08xVcud54Nb01qK9Y36K6IOuGcRyAVUVTZQMNkNktp62KzG7fXHutRva+adaGKVRdjPSCK5/cd5yekXRbMKv6f79Io+aIVuw81sm/Fa2aYLE/ZCb+TQPZ99K4N5QofreXyM+H1SsLEnXkxEiKWsi2dAuPfd0cMzA/EZXVi7wAEylayyqZrsDfjyD2cHjieS2jSLYyRtjg0BdGsWhAN3D0qHe2brwjMcm3id5uY79OlCQjfiF1QGwi+ZMwdo/QDL/KW2GojFwrL4CfjvWsePBeK7AClrmOqfRheKHGSMvOywqWv6Vn0ZAMlKZAL5uVEPn9zu6sdj8D8A0Pnt+XZ3tJ4k3RxoENpTWBZRzt2bgKQJRmhAAomnNShq07rVuaEeYP7bJgAzdE0lWBbr23HuFGv4Z2oVuZ+lAx2QofAcscJGRPPH2UVMxYpo/vZtNWpEyVQqdkR4IretpSkczMXXSL8Nb3jOkRRQLV+0ktBPsCRskLAASJIVlCE3FeeYdFY1KLkcpEEm61QJovn6CsOJvhIVZtDYDWY7ZROUR4cBYxc9uKHy/XDEKEMv3fh4vYw+5uEu5Q7ZMtd+JKW4u6pXFRyWf1OclIMMQ6DGlK2gYIaVXsUCikmuUp0Bgyo=\"}],\"legacy\":false}");
    }
}

