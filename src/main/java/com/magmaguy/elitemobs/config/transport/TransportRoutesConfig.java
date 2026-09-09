package com.magmaguy.elitemobs.config.transport;

import com.magmaguy.elitemobs.config.EliteMobsConfigInheritance;
import com.magmaguy.magmacore.config.CustomConfig;

/** Routes use the same file loading, inheritance and enablement as other custom content. */
public final class TransportRoutesConfig extends CustomConfig {
    public TransportRoutesConfig() {
        super("transport_routes", "com.magmaguy.elitemobs.config.transport.premade",
                TransportRoutesConfigFields.class, EliteMobsConfigInheritance.POLICY);
    }
}
