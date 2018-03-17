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

package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.elitedrops.EliteDropsDropper;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TreasureGoblin implements Listener {

    private Random random = new Random();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.TREASURE_GOBLIN)) {

            EliteDropsDropper eliteDropsDropper = new EliteDropsDropper();

            Entity entity = event.getEntity();

            for (int i = 0; i < ConfigValues.eventsConfig.getInt(EventsConfig.SMALL_TREASURE_GOBLIN_REWARD); i++) {

                eliteDropsDropper.dropItem(entity);

            }

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                if (((LivingEntity) entity).getKiller() != null) {

                    String newMessage = ConfigValues.eventsConfig.getString(EventsConfig.SMALL_TREASURE_GOBLIN_EVENT_PLAYER_END_TEXT).replace("$player", ((LivingEntity) entity).getKiller().getDisplayName());

                    player.sendMessage(ChatColorConverter.chatColorConverter(newMessage));

                } else {

                    player.sendMessage(ConfigValues.eventsConfig.getString(ChatColorConverter.chatColorConverter(EventsConfig.SMALL_TREASURE_GOBLIN_EVENT_OTHER_END_TEXT)));

                }

            }

        }

    }

    @EventHandler
    public void onHit(EntityDamageEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.TREASURE_GOBLIN)) {

            if (random.nextDouble() < 0.20) {

                //run power
                if (!event.getEntity().hasMetadata(MetadataHandler.TREASURE_GOBLIN_RADIAL_GOLD_EXPLOSION_COOLDOWN)) {

                    PowerCooldown.cooldownTimer(event.getEntity(), MetadataHandler.TREASURE_GOBLIN_RADIAL_GOLD_EXPLOSION_COOLDOWN, 20 * 20);
                    radialGoldExplosionInitializer((Zombie) event.getEntity());

                }

            }

        }

    }

    private void radialGoldExplosionInitializer(Zombie zombie) {

        zombie.setAI(false);

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter == 20 * 2) {

                    radialGoldExplosion(zombie);
                    zombie.setAI(true);
                    cancel();

                }

                if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_WARNING_VISUAL_EFFECTS)) {

                    zombie.getWorld().spawnParticle(Particle.SMOKE_NORMAL, zombie.getLocation(), counter, 1, 1, 1, 0);

                }


                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void radialGoldExplosion(Zombie zombie) {

        int projectileCount = 200;
        List<Item> itemList = new ArrayList<>();

        for (int i = 0; i < projectileCount; i++) {

            double x = random.nextDouble() - 0.5;
            double y = random.nextDouble() / 1.5;
            double z = random.nextDouble() - 0.5;

            Vector locationAdjuster = new Vector(x, 0, z).normalize();

            Location zombieLocation = zombie.getLocation().add(new Vector(0, 0.5, 0));
            Location projectileLocation = zombieLocation.add(locationAdjuster);
            ItemStack visualProjectileItemStack = new ItemStack(Material.GOLD_NUGGET, 1);

            /*
            Set custom lore to prevent item stacking
             */
            ItemMeta itemMeta = visualProjectileItemStack.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("" + random.nextDouble() + "" + random.nextDouble());
            itemMeta.setLore(lore);
            visualProjectileItemStack.setItemMeta(itemMeta);

            Item visualProjectile = projectileLocation.getWorld().dropItem(projectileLocation, visualProjectileItemStack);

            Vector velocity = new Vector(x, y, z).multiply(2);
            visualProjectile.setVelocity(velocity);
            visualProjectile.setPickupDelay(Integer.MAX_VALUE);
            visualProjectile.setMetadata(MetadataHandler.VISUAL_EFFECT_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));

            itemList.add(visualProjectile);

        }

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (itemList.size() == 0) cancel();

                Iterator<Item> iterator = itemList.iterator();

                while (iterator.hasNext()) {

                    Item item = iterator.next();

                    if (item == null || !item.isValid() || item.isDead()) {

                        iterator.remove();

                    } else if (item.isOnGround()) {

                        iterator.remove();
                        item.remove();

                    } else {

                        item.setVelocity(item.getVelocity().multiply(0.9));
                        radialGoldExplosionDamage(item.getNearbyEntities(0, 0, 0), zombie);

                    }

                    if (counter > 20 * 5) {

                        iterator.remove();
                        if (item != null) item.remove();

                    }

                }

                if (counter > 20 * 5) {

                    cancel();

                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static void radialGoldExplosionDamage(List<Entity> entityList, Zombie eliteMob) {

        for (Entity entity : entityList) {

            if (entity.equals(eliteMob)) break;

            if (entity instanceof LivingEntity && !entity.isInvulnerable()) {

                double entityHealth = ((LivingEntity) entity).getHealth();

                if (!(entity instanceof Player && (((Player) entity).getGameMode().equals(GameMode.SURVIVAL) ||
                        ((Player) entity).getGameMode().equals(GameMode.ADVENTURE)))) return;

                if (entityHealth > 0 && entityHealth - 1 < 0) {

                    ((LivingEntity) entity).setHealth(0);

                    if (entity instanceof Player) {

                        PlayerDeathMessageByEliteMob.handleDeathMessage((Player) entity, eliteMob);

                    }

                } else if (entityHealth > 0) {

                    ((LivingEntity) entity).setHealth(entityHealth - 1);

                }

            }

        }

    }

    public static void equipTreasureGoblin(Zombie zombie) {

        zombie.getEquipment().setHelmet(new ItemStack(Material.GOLD_HELMET, 1));
        zombie.getEquipment().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
        zombie.getEquipment().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
        zombie.getEquipment().setBoots(new ItemStack(Material.GOLD_BOOTS));

    }


}
