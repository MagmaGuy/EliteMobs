package com.magmaguy.elitemobs.config.transport;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.transport.TransportRoute;

public final class TransportRoutesConfigFields extends CustomConfigFields {
    private TransportRoute route;

    public TransportRoutesConfigFields(String filename, boolean enabled) {
        super(filename, enabled);
    }

    @Override
    public void processConfigFields() {
        // Read before adding defaults: obsolete definitions must fail without being rewritten.
        isEnabled = fileConfiguration.getBoolean("isEnabled", true);
        if (isEnabled) route = TransportRoute.read(filename.replaceFirst("\\.yml$", ""), fileConfiguration);
    }

    public TransportRoute getRoute() { return route; }
}
