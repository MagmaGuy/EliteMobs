/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.mobs.passive;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.ItemDropVelocity;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static org.bukkit.Material.CHICKEN;
import static org.bukkit.Material.FEATHER;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class ChickenHandler implements Listener {

    /*
    Augmented egg drops
    There's no egg dropping event and listening for new eggs can be extremely inaccurate due to high chicken density
    Use events to add and remove loaded chicken and use the scanner to update the list of active chicken
    */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void superDrops(EntityDamageByEntityEvent event) {

        if (event.getFinalDamage() < 1)
            return;

        if (event.getEntity() instanceof Chicken && EntityTracker.isSuperMob(event.getEntity())) {

            Random random = new Random();

            Chicken chicken = (Chicken) event.getEntity();

            double damage = event.getFinalDamage();
            //health is hardcoded here, maybe change it at some point?
            double dropChance = damage / 4;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            ItemStack featherStack = new ItemStack(FEATHER, (random.nextInt(2) + 1));
            ItemStack poultryStack = new ItemStack(CHICKEN, 1);

            for (int i = 0; i < dropMinAmount; i++) {

                chicken.getWorld().dropItem(chicken.getLocation(), featherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());
                chicken.getWorld().dropItem(chicken.getLocation(), poultryStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = chicken.getWorld().spawn(chicken.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

            }

            if (dropChance > dropRandomizer) {

                chicken.getWorld().dropItem(chicken.getLocation(), featherStack).setVelocity(ItemDropVelocity.ItemDropVelocity());
                chicken.getWorld().dropItem(chicken.getLocation(), poultryStack).setVelocity(ItemDropVelocity.ItemDropVelocity());

                ExperienceOrb xpDrop = chicken.getWorld().spawn(chicken.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(random.nextInt(3) + 1);

            }

        }

    }

    private final List<String> lore = new ArrayList<>(Arrays.asList("SuperChicken Egg"));

    //Egg drop chance is based on the underlying timer
    public void dropEggs() {

        ItemStack eggStack = new ItemStack(Material.EGG, 1);
        ItemMeta eggMeta = eggStack.getItemMeta();
        eggMeta.setLore(lore);
        eggStack.setItemMeta(eggMeta);

        Iterator<LivingEntity> superChickenIterator = EntityTracker.getSuperMobs().iterator();

        while (superChickenIterator.hasNext()) {

            LivingEntity chicken = superChickenIterator.next();

            if (!(chicken instanceof Chicken)) continue;

            if (chicken == null || !chicken.isValid()) {

                superChickenIterator.remove();

            } else {

                Item droppedItem = chicken.getWorld().dropItem(chicken.getLocation(), eggStack);
                droppedItem.setVelocity(ItemDropVelocity.ItemDropVelocity());
                new BukkitRunnable() {

                    @Override
                    public void run() {

                        if (droppedItem.isValid()) {

                            droppedItem.remove();

                        }

                    }

                }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60);

            }

        }

    }

    @EventHandler
    public void onHopperSuperEggPickup(InventoryPickupItemEvent event) {

        if (event.getItem().getItemStack().hasItemMeta() && event.getItem().getItemStack().getItemMeta().hasLore() &&
                event.getItem().getItemStack().getItemMeta().getLore().equals(lore)) {

            event.setCancelled(true);

        }

    }

}
