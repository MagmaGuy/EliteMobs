package com.magmaguy.elitemobs.mobs.passive;

import com.magmaguy.elitemobs.api.SuperMobDamageEvent;
import com.magmaguy.elitemobs.items.ItemDropVelocity;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static org.bukkit.Material.BEEF;
import static org.bukkit.Material.LEATHER;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class CowHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void superDrops(SuperMobDamageEvent event) {

        if (event.getEntityDamageEvent().getFinalDamage() < 1)
            return;

        if (event.getLivingEntity().getType().equals(EntityType.COW)) {

            Random random = new Random();

            Cow cow = (Cow) event.getLivingEntity();

            double damage = event.getEntityDamageEvent().getFinalDamage();
            //health is hardcoded here, maybe change it at some point?
            double dropChance = damage / 10;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack beefStack = new ItemStack(BEEF, (random.nextInt(3) + 1));
            //leather can drop 0, meaning that it could create visual artifacts. Have to filter that out.
            ItemStack leatherStack = new ItemStack(LEATHER, (random.nextInt(2)));

            for (int i = 0; i < dropMinAmount; i++) {

                cow.getWorld().dropItem(cow.getLocation(), beefStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = cow.getWorld().spawn(cow.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

                if (leatherStack.getAmount() != 0) {

                    cow.getWorld().dropItem(cow.getLocation(), leatherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

            if (dropChance > dropRandomizer) {

                cow.getWorld().dropItem(cow.getLocation(), beefStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = cow.getWorld().spawn(cow.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

                if (leatherStack.getAmount() != 0) {

                    cow.getWorld().dropItem(cow.getLocation(), leatherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                }

            }

        }

    }

}
