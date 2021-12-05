package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.CriticalStrikesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.customenchantments.CriticalStrikesEnchantment;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.DialogArmorStand;
import com.magmaguy.elitemobs.utils.VisualArmorStand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class PopupDisplay implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        if (!MobCombatSettingsConfig.displayDamageOnHit) return;

        Location mobLocation = event.getEliteMobEntity().getLocation();

        Vector offset = new Vector(ThreadLocalRandom.current().nextDouble(-2, 2), 0, ThreadLocalRandom.current().nextDouble(-2, 2));

        String colorPrefix = "";
        if (event.getDamageModifier() < 1) {
            //resist
            colorPrefix += MobCombatSettingsConfig.getResistTextColor();
            DialogArmorStand.createDialogArmorStand(event.getEliteMobEntity().getUnsyncedLivingEntity(),
                    MobCombatSettingsConfig.getResistText(), offset.clone().subtract(new Vector(0, 0.2, 0)));
            mobLocation.getWorld().playSound(mobLocation, Sound.BLOCK_ANVIL_USE, 1f, 1f);
            if (MobCombatSettingsConfig.isDoResistEffect())
                resistArmorStandCreator(event.getEliteMobEntity(), event.getPlayer(), Material.SHIELD);
        } else if (event.getDamageModifier() > 1) {
            //weak
            colorPrefix += MobCombatSettingsConfig.getWeakTextColor();
            DialogArmorStand.createDialogArmorStand(event.getEliteMobEntity().getUnsyncedLivingEntity(),
                    MobCombatSettingsConfig.getWeakText(), offset.clone().subtract(new Vector(0, 0.2, 0)));
            mobLocation.getWorld().playSound(mobLocation, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            if (MobCombatSettingsConfig.isDoWeakEffect())
                weakArmorStandCreator(event.getEliteMobEntity(), event.getPlayer(), Material.DIAMOND_SWORD);
        }
        if (event.isCriticalStrike()) {
            //crit
            colorPrefix += CriticalStrikesConfig.getCriticalHitColor();
            CriticalStrikesEnchantment.criticalStrikePopupMessage(event.getEliteMobEntity().getUnsyncedLivingEntity(), new Vector(0, 0.2, 0));
        }

        DialogArmorStand.createDialogArmorStand(event.getEliteMobEntity().getUnsyncedLivingEntity(), ChatColor.RED +
                colorPrefix + "" + ChatColor.BOLD + "" + (int) event.getDamage() + "", offset);

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EliteMobHealEvent event) {
        if (!MobCombatSettingsConfig.displayDamageOnHit) return;
        if (!event.getEliteEntity().isValid()) return;

        Vector offset = new Vector(ThreadLocalRandom.current().nextDouble(-1, 1), 0, ThreadLocalRandom.current().nextDouble(-1, 1));

        if (event.isFullHeal()){
            DialogArmorStand.createDialogArmorStand(event.getEliteEntity().getUnsyncedLivingEntity(),
                    ChatColor.GREEN + "FULL HEAL!", offset.clone().subtract(new Vector(0, 0.2, 0)));
        } else {
            DialogArmorStand.createDialogArmorStand(event.getEliteEntity().getUnsyncedLivingEntity(),
                    ChatColor.GREEN + "" + event.getHealAmount() + " HP HEAL!", offset.clone().subtract(new Vector(0, 0.2, 0)));
        }

    }

    private void resistArmorStandCreator(EliteEntity eliteEntity, Player player, Material material) {
        if (!eliteEntity.isValid() || !player.isValid() || !eliteEntity.getLocation().getWorld().equals(player.getWorld()))
            return;

        ArmorStand armorStand = VisualArmorStand.VisualArmorStand(getResistLocation(player, eliteEntity), "Resist");
        armorStand.getEquipment().setItemInMainHand(new ItemStack(material));
        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.setRightArmPose(new EulerAngle(Math.PI / 2d, Math.PI + Math.PI / 2d, Math.PI));

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > 20 || !eliteEntity.isValid() || !player.isValid() || !eliteEntity.getLocation().getWorld().equals(player.getWorld())) {
                    EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                    cancel();
                    return;
                }
                armorStand.teleport(getResistLocation(player, eliteEntity));
                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
    }

    private Location getResistLocation(Player player, EliteEntity eliteEntity) {
        Vector armorsStandVector = player.getLocation().subtract(eliteEntity.getLocation()).toVector().normalize().multiply(1.5);
        Location armorStandLocation = eliteEntity.getLocation().add(armorsStandVector);
        armorStandLocation.setDirection(armorsStandVector);
        return armorStandLocation;
    }

    private void weakArmorStandCreator(EliteEntity eliteEntity, Player player, Material material) {
        if (!eliteEntity.isValid() || !player.isValid() || !eliteEntity.getLocation().getWorld().equals(player.getWorld()))
            return;

        ArmorStand[] armorStands = new ArmorStand[2];
        armorStands[0] = generateWeakArmorStand(player, eliteEntity, material, -1);
        armorStands[1] = generateWeakArmorStand(player, eliteEntity, material, 1);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > 10 || !eliteEntity.isValid() || !player.isValid() || !eliteEntity.getLocation().getWorld().equals(player.getWorld())) {
                    EntityTracker.unregister(armorStands[0], RemovalReason.EFFECT_TIMEOUT);
                    EntityTracker.unregister(armorStands[1], RemovalReason.EFFECT_TIMEOUT);
                    cancel();
                    return;
                }
                for (ArmorStand armorStand : armorStands)
                    armorStand.teleport(armorStand.getLocation().add(eliteEntity.getLocation().add(new Vector(0, 0, 0))
                            .subtract(armorStand.getLocation()).toVector().normalize().multiply(.4)));
                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
    }

    private ArmorStand generateWeakArmorStand(Player player, EliteEntity eliteEntity, Material material, int offset) {
        Vector armorsStandVector = player.getLocation().clone().add(new Vector(0, 2, 0)).subtract(eliteEntity.getLocation()).toVector().normalize().multiply(3.0).rotateAroundY(Math.PI / 8 * offset);
        Location armorStandLocation = eliteEntity.getLocation().add(armorsStandVector);
        armorStandLocation.setDirection(armorsStandVector.multiply(-1));
        ArmorStand armorStand = VisualArmorStand.VisualArmorStand(armorStandLocation, "Weak");
        armorStand.getEquipment().setHelmet(new ItemStack(material));
        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.setHeadPose(new EulerAngle(-Math.PI - Math.PI / 4D, Math.PI / 4D, 0));
        return armorStand;
    }

}
