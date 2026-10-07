package com.magmaguy.elitemobs.config.transport;

import com.magmaguy.elitemobs.config.EliteMobsConfigInheritance;
import com.magmaguy.magmacore.config.CustomConfig;

/** Flight networks use the same file loading, inheritance and enablement as other custom content. */
public final class TransportNetworksConfig extends CustomConfig {
    public TransportNetworksConfig() {
        super("transport_networks", "com.magmaguy.elitemobs.config.transport.networks.premade",
                TransportNetworksConfigFields.class, EliteMobsConfigInheritance.POLICY);
    }
}
