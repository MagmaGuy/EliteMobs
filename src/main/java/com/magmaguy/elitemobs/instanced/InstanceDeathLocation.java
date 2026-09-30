package com.magmaguy.elitemobs.instanced;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import com.magmaguy.magmacore.match.MatchPlayer;
import com.magmaguy.magmacore.match.ReviveMarker;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * A dead participant's revive banner. MagmaCore's match core places and guards the banner and
 * revives on a punch; this class adds EliteMobs' floating text and keeps the public banner map.
 */
public class InstanceDeathLocation {
    private final MatchInstance matchInstance;
    @Getter
    private final Block bannerBlock;
    @Getter
    private final Player deadPlayer;
    private FakeText nameTag;
    private FakeText livesLeft;
    private FakeText instructions;

    private InstanceDeathLocation(MatchInstance matchInstance, Player deadPlayer, Block bannerBlock) {
        this.matchInstance = matchInstance;
        this.deadPlayer = deadPlayer;
        this.bannerBlock = bannerBlock;
    }

    /** The marker the spectate-and-revive death policy draws with. */
    static ReviveMarker marker(MatchPlayer participant) {
        MatchInstance instance = (MatchInstance) participant.getMatch();
        return new ReviveMarker() {
            private InstanceDeathLocation shown;

            @Override
            public void show(Block banner, MatchPlayer dead, int lives) {
                hide();
                shown = new InstanceDeathLocation(instance, dead.getPlayer(), banner);
                shown.createDisplays(lives);
                instance.deathBanners.put(banner, shown);
            }

            @Override
            public void hide() {
                if (shown == null) return;
                instance.deathBanners.remove(shown.bannerBlock, shown);
                shown.removeDisplays();
                shown = null;
            }
        };
    }

    private void createDisplays(int lives) {
        Location deathLocation = bannerBlock.getLocation();
        instructions = VisualDisplay.generateFakeText(deathLocation.clone().add(new Vector(0, 2.2, 0)), DungeonsConfig.getInstancePunchToRez(), 30);
        nameTag = VisualDisplay.generateFakeText(deathLocation.clone().add(new Vector(0, 2, 0)), deadPlayer.getDisplayName(), 30);
        livesLeft = VisualDisplay.generateFakeText(deathLocation.clone().add(new Vector(0, 1.8, 0)), DungeonsConfig.getInstanceLivesLeft().replace("$amount", String.valueOf(lives)), 30);
    }

    private void removeDisplays() {
        if (nameTag != null) nameTag.remove();
        if (instructions != null) instructions.remove();
        if (livesLeft != null) livesLeft.remove();
        nameTag = null;
        instructions = null;
        livesLeft = null;
    }

    public void clear(boolean resurrect) {
        matchInstance.releaseBanner(deadPlayer, resurrect);
    }

    public Location getRespawnLocation() {
        return bannerBlock.getLocation().clone().add(0.5, 0, 0.5);
    }

    /** The match core keeps the banner in place; kept for API compatibility. */
    public void bannerWatchdog() {
    }
}
