package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;

import java.util.List;

public class DefaultCustomItemsPackage extends ContentPackagesConfigFields {
    public DefaultCustomItemsPackage() {
        super("default_items", true);
        setCustomItemFilenames(List.of(
                "berserker_charm.yml",
                "challengers_boots.yml",
                "challengers_chestplate.yml",
                "challengers_helmet.yml",
                "challengers_leggings.yml",
                "challengers_sword.yml",
                "chameleon_charm.yml",
                "cheetah_charm.yml",
                "depths_seeker.yml",
                "dwarven_greed.yml",
                "elephant_charm.yml",
                "elite_lucky_ticket.yml",
                "elite_scrap_huge.yml",
                "elite_scrap_large.yml",
                "elite_scrap_medium.yml",
                "elite_scrap_small.yml",
                "elite_scrap_tiny.yml",
                "enchanted_book_arrow_damage.yml",
                "enchanted_book_arrow_fire.yml",
                "enchanted_book_arrow_infinite.yml",
                "enchanted_book_arrow_knockback.yml",
                "enchanted_book_channeling.yml",
                "enchanted_book_critical_strikes.yml",
                "enchanted_book_damage_all.yml",
                "enchanted_book_damage_arthropods.yml",
                "enchanted_book_damage_undead.yml",
                "enchanted_book_depth_strider.yml",
                "enchanted_book_dig_speed.yml",
                "enchanted_book_drilling.yml",
                "enchanted_book_durability.yml",
                "enchanted_book_earthquake.yml",
                "enchanted_book_flamethrower.yml",
                "enchanted_book_frost_walker.yml",
                "enchanted_book_hunter.yml",
                "enchanted_book_ice_breaker.yml",
                "enchanted_book_impaling.yml",
                "enchanted_book_knockback.yml",
                "enchanted_book_lightning.yml",
                "enchanted_book_loot_bonus_blocks.yml",
                "enchanted_book_loot_bonus_mobs.yml",
                "enchanted_book_loud_strikes.yml",
                "enchanted_book_loyalty.yml",
                "enchanted_book_luck.yml",
                "enchanted_book_lure.yml",
                "enchanted_book_mending.yml",
                "enchanted_book_meteor_shower.yml",
                "enchanted_book_multishot.yml",
                "enchanted_book_oxygen.yml",
                "enchanted_book_piercing.yml",
                "enchanted_book_plasma_boots.yml",
                "enchanted_book_protection_environmental.yml",
                "enchanted_book_protection_explosions.yml",
                "enchanted_book_protection_fall.yml",
                "enchanted_book_protection_fire.yml",
                "enchanted_book_protection_projectile.yml",
                "enchanted_book_quick_charge.yml",
                "enchanted_book_riptide.yml",
                "enchanted_book_silk_touch.yml",
                "enchanted_book_soul_speed.yml",
                "enchanted_book_sweeping_edge.yml",
                "enchanted_book_thorns.yml",
                "enchanted_book_water_worker.yml",
                "firefly_charm.yml",
                "fishy_charm.yml",
                "goblin_ballista.yml",
                "goblin_boots.yml",
                "goblin_chestplate.yml",
                "goblin_cleaver.yml",
                "goblin_helmet.yml",
                "goblin_leggings.yml",
                "goblin_poker.yml",
                "goblin_shooter.yml",
                "goblin_slasher.yml",
                "grappling_arrow.yml",
                "grunts_boots.yml",
                "grunts_chestplate.yml",
                "grunts_helmet.yml",
                "grunts_leggings.yml",
                "grunts_sword.yml",
                "invictus_boots.yml",
                "invictus_chestplate.yml",
                "invictus_helmet.yml",
                "invictus_leggings.yml",
                "invictus_pickaxe.yml",
                "invictus_shovel.yml",
                "invictus_sword.yml",
                "lucky_charms.yml",
                "magmaguys_toothpick.yml",
                "meteor_shower_scroll.yml",
                "novices_boots.yml",
                "novices_chestplate.yml",
                "novices_helmet.yml",
                "novices_leggings.yml",
                "novices_sword.yml",
                "owl_charm.yml",
                "rabbit_charm.yml",
                "rod_of_the_depths.yml",
                "salamander_charm.yml",
                "scorpion_charm.yml",
                "shulker_charm.yml",
                "slowpoke_charm.yml",
                "summon_merchant_scroll.yml",
                "summon_wolf_scroll.yml",
                "the_feller.yml",
                "the_stinger.yml",
                "unbind_scroll.yml",
                "vampiric_charm.yml",
                "veterans_boots.yml",
                "veterans_chestplate.yml",
                "veterans_helmet.yml",
                "veterans_leggings.yml",
                "veterans_sword.yml",
                "werewolf_bone.yml",
                "wolfsbane.yml",
                "xmas_lost_present.yml",
                "zombie_kings_axe.yml"
        ));
        setName("&2Default EliteMobs Custom Items");
        setCustomInfo(List.of("All default custom items from EliteMobs!"));
        setDownloadLink(DiscordLinks.premiumMinidungeons);
    }
}