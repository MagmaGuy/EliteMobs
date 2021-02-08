package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class AttackFreeze extends MinorPower implements Listener {

    public AttackFreeze() {
        super(PowersConfig.getPower("attack_freeze.yml"));
    }

    @EventHandler
    public void attackFreeze(PlayerDamagedByEliteMobEvent event) {
        if (event.isCancelled()) return;
        AttackFreeze attackFreeze = (AttackFreeze) event.getEliteMobEntity().getPower(this);
        if (attackFreeze == null) return;
        if (attackFreeze.isCooldown()) return;

        attackFreeze.doCooldown(20 * 15);

        /*
        Slow player down
         */
        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 20));
        event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(
                        ChatColorConverter.convert(super.getConfiguration().getString("freezeMessage"))));

        /*
        Add block effect
         */
        Block block = event.getPlayer().getLocation().getBlock();
        Material originalMaterial = block.getType();

        if (!originalMaterial.equals(Material.AIR))
            return;

        EntityTracker.addTemporaryBlock(block, 20 * 3, Material.PACKED_ICE);

    }

}
