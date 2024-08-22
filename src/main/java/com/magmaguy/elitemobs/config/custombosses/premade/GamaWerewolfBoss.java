package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GamaWerewolfBoss extends CustomBossesConfigFields {
    public GamaWerewolfBoss() {
        super("gamma_werewolf",
                EntityType.WOLF,
                true,
                "$reinforcementLevel &7Gamma Werewolf",
                "dynamic");
        setHealthMultiplier(1.5);
        setDamageMultiplier(1.5);
        setPowers(new ArrayList<>(List.of("ground_pound.yml",
                "summonable:summonType=ON_COMBAT_ENTER:filename=omega_wolf.yml:amount=2:inheritAggro=true:spawnNearby=true")));
        setDisguise("custom:gamma_werewolf");
        setCustomDisguiseData("player gamma_werewolf setskin {\"id\":\"5f5fff79-687e-46ad-8020-e618a6ec8871\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTYyNzE4OTcxMTA4MCwKICAicHJvZmlsZUlkIiA6ICJkMGFkNThjOTNiMzY0ZjgyOWRjZWEzMTAzMzE5MTgyMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJfU2FwcGhpcmVfTHVuYV8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgxZjRmMGRiNWUzZDQzYmU2ZWMwNzE3Njc0MDMxN2Y4ZTQxZWJiNjU0YzYwZTU4MzVhZGYyMzE5MzUzMTE3IgogICAgfQogIH0KfQ==\",\"signature\":\"d9XXwZOzBUfoV04rV2XOoSRI1GYolEe2ABeNXL8Yft56L+ci/tzM+DWya9yzTjP1cS4wQ1B23rAX2CQfcSC9HF/LPTtwzE+gmGt0EVGH+S3jvcXPl/Vb2igfK6LmINqhjpqEIhvF8NePF3oZ01S6Bwc46v1SMRQ3LpWC9cPPrIeVc48RrpsxknQVzszuKHeiDv3H1x4pdhpR3EdLlAPMyapijio1yjYCRgxd9TWN/4EcbtKTWy2AeEwU/eATYiNFYcFPdOQ3FGjiveUkeq8dGGe8huZvFCNcSizuws70HaIQrJceosRfnwiuJUBUVkYS9cPCVmV+dDOBOCYt7falct2wuHQWFXcD6e9Umqts26lzLbccDY0VQnb9k32Ip/6m95v8sg7AP1OcEN9PNrD5MV+p+CSkzfqfoS2nDcBg69hhi6DYqPBzlqVmDAdRJs2MX5kQoYwM5Zqi6LJDWqVNkNixc/MBnzkS92PcG93cd6eMPjE6p4gWRLoU9hbuUo+6xxh4vItBJj+65UgRvVk48fJJSEKn61be8Jhp6nuhmJ4AUYfGcBun9Q0Wn7py+YsBAZs6Abt3+Nr56V7dOxhiR2hINmhklZVOsiv5UDTzlWhpUe0yBjp92ELfzt7mbLzv5he6SUaxK1OytYchA8nJbTEZnsLpOiXaJs+wKgz1u9s=\"}],\"legacy\":false}");
        HashMap<Material, Double> damageModifiers = new HashMap<>();
        damageModifiers.put(Material.IRON_SWORD, 2D);
        damageModifiers.put(Material.IRON_AXE, 2D);
        setDamageModifiers(damageModifiers);
        setFollowDistance(100);
    }
}
