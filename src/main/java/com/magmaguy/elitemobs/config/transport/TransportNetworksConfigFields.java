package com.magmaguy.elitemobs.config.transport;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.transport.TransportNetwork;
import com.magmaguy.magmacore.util.Logger;

public final class TransportNetworksConfigFields extends CustomConfigFields {
    private TransportNetwork network;

    public TransportNetworksConfigFields(String filename, boolean enabled) {
        super(filename, enabled);
    }

    @Override
    public void processConfigFields() {
        isEnabled = fileConfiguration.getBoolean("isEnabled", true);
        if (isEnabled) network = TransportNetwork.load(filename.replaceFirst("\\.yml$", ""), fileConfiguration,
                warning -> Logger.warn("Flight network " + filename + ": " + warning));
    }

    public TransportNetwork getNetwork() { return network; }
}
