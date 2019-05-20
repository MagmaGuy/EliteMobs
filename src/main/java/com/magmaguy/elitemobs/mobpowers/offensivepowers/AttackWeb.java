package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.mobpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class AttackWeb extends MinorPower implements Listener {

    public AttackWeb() {
        super("AttackWeb", Material.COBWEB);
    }

    @EventHandler
    public void attackWeb(PlayerDamagedByEliteMobEvent event) {
        AttackWeb attackWeb = (AttackWeb) event.getEliteMobEntity().getPower(this);
        if (attackWeb == null) return;

        Block block = event.getPlayer().getLocation().getBlock();
        Material originalMaterial = block.getType();
        if (!originalMaterial.equals(Material.AIR))
            return;
        EntityTracker.addTemporaryBlock(block, 20 * 3, Material.COBWEB);
    }

}
