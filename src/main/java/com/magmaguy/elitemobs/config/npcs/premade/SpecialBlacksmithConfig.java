package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class SpecialBlacksmithConfig extends NPCsConfigFields {
    public SpecialBlacksmithConfig() {
        super("special_blacksmith",
                true,
                "Grog",
                "<Special Blacksmith>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,297.5,81,240.5,90,0",
                new ArrayList<>(List.of(
                        "Need something?",
                        "Got anything good?",
                        "We have nothing but the best",
                        "Got anything worth selling?",
                        "We only buy elite mob gear.")),
                new ArrayList<>(List.of(
                        "We buy low and sell high.",
                        "No refunds.",
                        "Absolutely no refunds.",
                        "There are no refunds.",
                        "No taksies backsies.",
                        "Shop purchases are final.")),
                new ArrayList<>(List.of(
                        "Come back when you have\\nmore to spend",
                        "Don't get yourself killed,\\nwe want you to bring us\\nmore gear.",
                        "Next time buy something \\nmore expensive.",
                        "Don't forget, no refunds.")),
                true,
                3,
                NPCInteractions.NPCInteractionType.CUSTOM_SHOP);
        setDisguise("custom:ag_special_blacksmith");
        setCustomDisguiseData("player ag_special_blacksmith setskin {\"id\":\"5a5bfe9e-6db4-4458-b66d-31d059260f1f\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTYxMjk2NjU5MTAzNCwKICAicHJvZmlsZUlkIiA6ICIxNzhmMTJkYWMzNTQ0ZjRhYjExNzkyZDc1MDkzY2JmYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzaWxlbnRkZXRydWN0aW9uIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhNjk2MWE4ZGQ3NDA1YWMzOGMxZWI3YmM0YTBhNzQ2NzdkMWU0NjZmNjM3NjRmZjhhYjIxYmNkMjQ4Yzg0ODUiCiAgICB9CiAgfQp9\",\"signature\":\"lIL1qePOExtPIFEK4r8woroF//ii3SOY7Rggh9Qwwj7qloAdKUg4+iGvllSD1SKOFoYP7SiUb37tU3CbfEbfr+W2lib1NF+NneHDdTmqngIoGrMS+ATSpzZvlx103tZQTLRytxoB2grMLtWprG5RXgBewqhvk0aXhIGYte3z8pyCX59YTI5ViKK7SWivIxKv5m2vOrW0Z2pVKvGst1Ck36/2vmLZmgRR3RVUkErxGcamsqhHlRCXW1swcWnZQJJbx4kWtAdkI0GtiRYkb7ZauVm9WC9Ip5ywN6hdTc+EZvtP7xfSsQjyI0kGpiw93GwBQY/hYEdqmPzTrdbC8qx09G6U7I9BwbSujDJDhS4LxwcxoMd8I4T8tDbDp8lFbTDldMVngWWTARE6gJZUwQo0ttkYUDADyi6tmAbyYrYGDxFz24rZRIEWUzxJqUmJ5NzWWvW5pBXZ7yJ8glYW86yjwwXO3b6Qg0VBlFQdEv6z6L90Ji3U3GzENMTdiyio+iqv8D/G5F0iN1D3Il9cS80ZvU3DhyxAH6DQOwN4mf6i+0Mud6t9Qz/pIMLAGgNWCz9VPm4yvPazMPFKLbaCTYwOui3d+J8sHbkrtYSRfT2oHnX5NzVC97b6F3W6IIXPbo/f+pku4AA5+0BawK1ECR3yYEz9w0KTwA2aRxkugeWsuqc=\"}],\"legacy\":false}");
    }
}
