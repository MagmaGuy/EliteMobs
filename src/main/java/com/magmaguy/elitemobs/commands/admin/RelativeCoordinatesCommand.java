package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RelativeCoordinatesCommand {
    public static void get(Player player, String minidungeonString) {

        EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonString);
        if (emPackage == null) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Minidungeons name " + minidungeonString + " &4isn't valid!"));
            return;
        }

        if (!emPackage.isInstalled()) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Minidungeon isn't installed! Can't get the relative location for uninstalled Minidungeons!"));
            return;
        }

        if (!(emPackage instanceof SchematicPackage)) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Content is not a schematic minidungeon! Run this command for schematic minidungeons only!"));
            return;
        }

        Location anchorpoint = emPackage.getDungeonPackagerConfigFields().getAnchorPoint();

        if (anchorpoint == null) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Something went wrong and made the anchor point not valid!"));
            return;
        }

        Vector relativeVector = player.getLocation().clone().subtract(anchorpoint).toVector();
        if (((SchematicPackage) emPackage).getDungeonPackagerConfigFields().getCalculatedRotation() != 0)
            GenericRotationMatrixMath.rotateVectorYAxis(((SchematicPackage) emPackage).getDungeonPackagerConfigFields().getCalculatedRotation(), anchorpoint, relativeVector);

        String relativePosition = relativeVector.getBlockX() + ", "
                + relativeVector.getBlockY() + ", "
                + relativeVector.getBlockZ();

        player.sendMessage(ChatColorConverter.convert(
                "[EliteMobs] Relative position to anchor point of " + emPackage.getDungeonPackagerConfigFields().getName() + ": " + relativePosition));

    }

}
