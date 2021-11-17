package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RelativeCoordinatesCommand {
    public static void get(Player player, String minidungeonString) {

        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonString);
        if (minidungeon == null) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Minidungeons name " + minidungeonString + " &4isn't valid!"));
            return;
        }

        if (!minidungeon.isInstalled()) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Minidungeon isn't installed! Can't get the relative location for uninstalled Minidungeons!"));
            return;
        }

        Location anchorpoint = minidungeon.getDungeonPackagerConfigFields().getAnchorPoint();

        if (anchorpoint == null) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Something went wrong and made the anchor point not valid!"));
            return;
        }

        Vector relativeVector = player.getLocation().clone().subtract(anchorpoint).toVector();
        if (minidungeon.getDungeonPackagerConfigFields().getRotation() != 0)
            GenericRotationMatrixMath.rotateVectorYAxis(minidungeon.getDungeonPackagerConfigFields().getRotation(), anchorpoint, relativeVector);

        String relativePosition = relativeVector.getBlockX() + ", "
                + relativeVector.getBlockY() + ", "
                + relativeVector.getBlockZ();

        player.sendMessage(ChatColorConverter.convert(
                "[EliteMobs] Relative position to anchor point of " + minidungeon.getDungeonPackagerConfigFields().getName() + ": " + relativePosition));

    }

}
