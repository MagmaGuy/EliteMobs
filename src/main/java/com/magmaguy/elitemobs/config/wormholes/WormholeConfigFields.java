package com.magmaguy.elitemobs.config.wormholes;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class WormholeConfigFields extends CustomConfigFields {

    @Getter
    @Setter
    private String location1;
    @Getter
    @Setter
    private String location2;
    @Getter
    @Setter
    private String location1Text;
    @Getter
    @Setter
    private String location2Text;
    @Getter
    @Setter
    private String permission;
    @Getter
    @Setter
    private Wormhole.WormholeStyle style = Wormhole.WormholeStyle.CUBE;
    @Getter
    @Setter
    private int particleColor;
    @Getter
    @Setter
    private double coinCost;
    @Getter
    @Setter
    private boolean blindPlayer;
    @Getter
    @Setter
    private double sizeMultiplier = 1D;
    @Getter
    @Setter
    private String customModel1 = null;
    @Getter
    @Setter
    private String customModel2 = null;
    // Optional flight section: stepping in opens this network node's flight menu instead of teleporting.
    @Getter
    @Setter
    private String flightNetwork;
    @Getter
    @Setter
    private String flightNode;
    @Getter
    @Setter
    private String flightDestination;
    // Both ends live in a dungeon blueprint world; a copy is created per instance, like instanced NPCs.
    @Getter
    @Setter
    private boolean instanced;

    public WormholeConfigFields(String filename,
                                boolean isEnabled,
                                String location1,
                                String location2,
                                Wormhole.WormholeStyle style) {
        super(filename, isEnabled);
        this.location1 = location1;
        this.location2 = location2;
        this.style = style;
    }

    public WormholeConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.location1 = processString("location1", location1, null, true);
        this.location1Text = translatable(filename, "location1Text", processString("location1Text", location1Text, null, false));
        this.flightNetwork = processString("flight.network", flightNetwork, null, false);
        this.flightNode = processString("flight.node", flightNode, null, false);
        this.flightDestination = processString("flight.destination", flightDestination, null, false);
        this.instanced = processBoolean("instanced", instanced, false, false);
        if (isFlightPortal() && (flightNode == null || flightNode.isBlank()))
            Logger.warn("Wormhole " + filename + " has a flight section without flight.node! Its flight menu will not know where it departs from.");
        // Flight portals may be one-ended, so they don't get a location2 default written into them.
        this.location2 = processString("location2", location2, null, !isFlightPortal());
        this.location2Text = translatable(filename, "location2Text", processString("location2Text", location2Text, null, false));
        this.permission = processString("permission", permission, null, false);
        this.coinCost = processDouble("coinCost", coinCost, 0, false);
        this.style = processEnum("style", style, Wormhole.WormholeStyle.CUBE, Wormhole.WormholeStyle.class, false);
        this.particleColor = processInt("particleColor", particleColor, 0x800080, false);
        this.blindPlayer = processBoolean("blindPlayer", blindPlayer, false, false);
        this.sizeMultiplier = processDouble("sizeMultiplier", sizeMultiplier, 1D, false);
        this.customModel1 = processString("customModel1", customModel1, null, false);
        this.customModel2 = processString("customModel2", customModel2, null, false);
    }

    public boolean isFlightPortal() {
        return flightNetwork != null && !flightNetwork.isBlank();
    }

    public void setWormholeEntryLocation(Location location, int wormholeNumber) {
        if (wormholeNumber == 1) {
            this.location1 = ConfigurationLocation.deserialize(location);
            fileConfiguration.set("location1", location1);
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
        } else {
            this.location2 = ConfigurationLocation.deserialize(location);
            fileConfiguration.set("location2", location2);
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
        }
    }

}
