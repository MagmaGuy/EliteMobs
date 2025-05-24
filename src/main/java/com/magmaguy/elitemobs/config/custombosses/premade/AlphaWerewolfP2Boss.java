package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AlphaWerewolfP2Boss extends CustomBossesConfigFields {
    public AlphaWerewolfP2Boss() {
        super("alpha_werewolf_p2",
                EntityType.VINDICATOR,
                true,
                "$eventBossLevel &7Alpha Werewolf",
                "dynamic");
        setPersistent(true);
        setHealthMultiplier(4);
        setDamageMultiplier(1.25);
        setPowers(new ArrayList<>(List.of("attack_blinding.yml", "ground_pound.yml", "moonwalk.yml", "spirit_walk.yml",
                "summonable:summonType=ON_HIT:filename=beta_wolf.yml:amount=5:chance=0.1:inheritAggro=true:spawnNearby=true",
                "summonable:summonType=ON_HIT:filename=gamma_werewolf.yml:amount=1:chance=0.1:inheritAggro=true:spawnNearby=true",
                "summonable:summonType=GLOBAL:filename=gamma_werewolf.yml:amount=1:customSpawn=normal_surface_spawn.yml",
                "summonable:summonType=GLOBAL:filename=omega_wolf.yml:amount=2:customSpawn=normal_surface_spawn.yml")));
        setSpawnMessage("&cThe howls of an Alpha Werewolf are heard!");
        setDeathMessage("&aThe Alpha Werewolf has been stopped by $players!");
        setDeathMessages(new ArrayList<>(List.of(
                "&e&l---------------------------------------------",
                "&4The Alpha Wolf has been put down!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------")));
        setEscapeMessage("&4Dawn breaks, the Alpha Wolf vanishes without a trace!");
        setLocationMessage("&7Alpha Wolf: $distance blocks away!");
        setUniqueLootList(new ArrayList<>(List.of(
                "werewolf_bone.yml:0.2",
                "werewolf_bone.yml:0.2",
                "werewolf_bone.yml:0.2",
                "werewolf_bone.yml:0.2",
                "werewolf_bone.yml:0.2",
                "wolfsbane.yml:0.2")));
        setTrails(Collections.singletonList(Material.BONE.toString()));
        setDisguise("custom:alpha_werewolf_p2");
        setCustomDisguiseData("player alpha_werewolf_p2 setskin {\"id\":\"50f47a40-9557-4ef8-9986-9c97215116e4\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTU5NjY5NjY4Njg4OCwKICAicHJvZmlsZUlkIiA6ICIwNmE4NjAyZDAwODk0YWQxOTcyMGQ3NGE1OGU1MDZjZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfMW5kcmFfIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU1ZWFhMDlkNDVlMzFhNmM0YzFjYTc3MWNkZWE4ZDlmM2Y2MWJjMTI1MGI5ZTg5NGY3ZmQ1YzIxM2YxOGE0YjUiCiAgICB9CiAgfQp9\",\"signature\":\"psopRAKIS71E2Chl8hKBKXJG6yvtDxP+e0yUY6iDRlPC8M/0Vs+lL1dnArQSKO68PZ6KlaiYRdMhEWHVTdbz3wXniNK8KNi+Aitl3MYXGD7BRfxENJE4GOjyvAYyWgxyU3s2D8gZbBMkrNYBcNI7SvhLUT67libXG0WOUCBLGE29mtRRS74eHEC0P55CoVXJtxksxBr3wM4p55hQepQoDV+zrGmokwNZjgxlNaMP0v6R6LWyBxfwnCTVNRa8PS+YmMtxXkqa9Dl9aDAzit41J455F9Ei/Md4wahZ89pauLzBnWrjlJLriv5yg/pCWBOlUqreLaiN3y+gFmMa1TjI52FMyPn56/GW5U+Eud90nL9I4NignfJzdGgDqjP2e7xcgRvmEb344+97/fTPy721vgsMXMAlEPwQg7HadTCUOpRGn918gGRTbLdZGPRvcZxRKVV6unM1bB25XaRNC5GZtOHRDO61YmX5c5URIy6ectjmhPrw3u6Yx8q/5N5+OIsoADANTUh9decoaCT4Z5T12s6YtsBy7qay3fTt4oVj4pWO3zWvLJ+8NmGKlyX+SHz7xcxXhFOUgXoX7V+5ICN87zn4IsQ7j4Ihn8uSrEbX05tnfBELbQFz7MGULvqwnR2GiEYyVULOB4y1fISoRNNx1TtbtAOMEB3dnKlHgdzkdhM=\"}],\"legacy\":false}");
        setAnnouncementPriority(2);
        HashMap<Material, Double> damageModifiers = new HashMap<>();
        damageModifiers.put(Material.IRON_SWORD, 2D);
        damageModifiers.put(Material.IRON_AXE, 2D);
        setDamageModifiers(damageModifiers);
        setFollowDistance(100);
    }
}
