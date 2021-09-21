package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;


public class TransitiveBlock {

    @Getter
    private BlockData blockData;
    @Getter
    private Vector relativeLocation;
    @Getter
    @Setter
    private boolean isAir;

    public TransitiveBlock(BlockData blockData, Vector relativeLocation) {
        this.blockData = blockData;
        this.relativeLocation = relativeLocation;
    }

    public static List<TransitiveBlock> serializeTransitiveBlocks(List<String> deserializedList, String filename) {
        List<TransitiveBlock> transitiveBlocks = new ArrayList<>();
        for (String deserializedString : deserializedList) {
            try {
                String[] elements = deserializedString.split("/");
                String[] vector = elements[0].split(",");
                double x = Double.parseDouble(vector[0]);
                double y = Double.parseDouble(vector[1]);
                double z = Double.parseDouble(vector[2]);
                BlockData blockData = Bukkit.getServer().createBlockData(elements[1]);
                transitiveBlocks.add(new TransitiveBlock(blockData, new Vector(x, y, z)));
            } catch (Exception ex) {
                new WarningMessage("Failed to serialize Transitive Block! Issue with entry " + deserializedString + " in file " + filename);
            }
        }
        return transitiveBlocks;
    }
}