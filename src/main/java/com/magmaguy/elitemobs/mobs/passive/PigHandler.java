package com.magmaguy.elitemobs.mobs.passive;

import com.magmaguy.elitemobs.api.SuperMobDamageEvent;
import com.magmaguy.elitemobs.items.ItemDropVelocity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static org.bukkit.Material.PORKCHOP;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class PigHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void superDrops(SuperMobDamageEvent event) {

        if (event.getEntityDamageEvent().getFinalDamage() < 1)
            return;

        if (!event.getLivingEntity().getType().equals(EntityType.PIG)) return;

        Random random = new Random();

        Pig pig = (Pig) event.getLivingEntity();

        double damage = event.getEntityDamageEvent().getFinalDamage();
        //health is hardcoded here, maybe change it at some point?
        double dropChance = damage / 10;
        double dropRandomizer = random.nextDouble();
        //this rounds down
        int dropMinAmount = (int) dropChance;

        ItemStack porkchopStack = new ItemStack(PORKCHOP, random.nextInt(3) + 1);

        for (int i = 0; i < dropMinAmount; i++) {

            pig.getWorld().dropItem(pig.getLocation(), porkchopStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

            ExperienceOrb xpDrop = pig.getWorld().spawn(pig.getLocation(), ExperienceOrb.class);
            xpDrop.setExperience(random.nextInt(3) + 1);

        }

        if (dropChance > dropRandomizer) {

            pig.getWorld().dropItem(pig.getLocation(), porkchopStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

            ExperienceOrb xpDrop = pig.getWorld().spawn(pig.getLocation(), ExperienceOrb.class);
            xpDrop.setExperience(random.nextInt(3) + 1);

        }

    }

}
