package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class VisualEffects {
    private final WormholeConfigFields wormholeConfigFields;
    private final List<Vector> cachedLocations = new ArrayList<>();
    @Getter
    private final List<List<Vector>> cachedRotations = new ArrayList<>();

    public VisualEffects(WormholeConfigFields wormholeConfigFields) {
        this.wormholeConfigFields = wormholeConfigFields;
        initializeVisualEffect();
    }

    private void initializeVisualEffect() {
        switch (wormholeConfigFields.getStyle()) {
            case CUBE:
                generateCube();
                break;
            case CRYSTAL:
                generateCrystal();
                break;
            case ICOSAHEDRON:
                generateIcosahedron();
                break;
            default:
                new WarningMessage("Missing com.magmaguy.elitemobs.wormhole style for " + wormholeConfigFields.getStyle());
        }
    }

    private void generateCrystal() {
        //top
        Vector top = new Vector(0, 1, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        cachedLocations.add(top);
        //bottom
        Vector bottom = new Vector(0, -1, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        cachedLocations.add(bottom);
        //front
        Vector front = new Vector(.5, 0, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        cachedLocations.add(front);
        //side
        Vector side = new Vector(0, 0, .5).multiply(wormholeConfigFields.getSizeMultiplier());
        cachedLocations.add(side);

        finishCuboidInitialization(top, bottom, front, side);

    }

    private void generateCube() {
        //top
        Vector top = new Vector(0, 1, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        cachedLocations.add(top);
        //bottom
        Vector bottom = new Vector(0, -1, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        cachedLocations.add(bottom);
        //front
        Vector front = new Vector(1, 0, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        cachedLocations.add(front);
        //side
        Vector side = new Vector(0, 0, 1).multiply(wormholeConfigFields.getSizeMultiplier());
        cachedLocations.add(side);

        finishCuboidInitialization(top, bottom, front, side);

    }

    private void generateIcosahedron() {
        Vector top = new Vector(0, 1, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        cachedLocations.add(top);
        Vector offsetTopPoint = new Vector(1, .5, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        List<Vector> topPentagon = getPentagonVectors(top, offsetTopPoint);

        Vector bottom = new Vector(0, -1, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        Vector offsetBottomPoint = new Vector(1, -.5, 0).multiply(wormholeConfigFields.getSizeMultiplier());
        offsetBottomPoint.rotateAroundY(2 * Math.PI / 5D / 2D);
        List<Vector> bottomPentagon = getPentagonVectors(bottom, offsetBottomPoint);

        for (int i = 0; i < topPentagon.size(); i++) {
            trace(topPentagon.get(i), bottomPentagon.get(i));
            if (i + 1 < topPentagon.size() - 1)
                trace(topPentagon.get(i + 1), bottomPentagon.get(i));
        }
        trace(topPentagon.get(0), bottomPentagon.get(4));
        cacheRotations();
    }

    private List<Vector> getPentagonVectors(Vector centerPosition, Vector offsetPosition) {
        List<Vector> pentagonVectors = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Vector rotatedVector = offsetPosition.clone().rotateAroundY(2 * Math.PI / 5D * i).clone();
            if (i > 0)
                trace(pentagonVectors.get(i - 1), rotatedVector);
            trace(centerPosition, rotatedVector);
            pentagonVectors.add(rotatedVector);
        }
        trace(pentagonVectors.get(0), pentagonVectors.get(pentagonVectors.size() - 1));
        return pentagonVectors;
    }

    private void finishCuboidInitialization(Vector top, Vector bottom, Vector front, Vector side) {
        trace(top, front);
        trace(bottom, front);
        trace(front, side);

        List<Vector> otherSidesList = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            double rotation = Math.PI / 2 * i;
            for (Vector vector : cachedLocations) {
                Vector rotatedVector = vector.clone().rotateAroundY(rotation);
                if (rotatedVector.getX() != vector.getX() || rotatedVector.getZ() != vector.getZ())
                    otherSidesList.add(rotatedVector);
            }
        }

        cachedLocations.addAll(otherSidesList);

        cacheRotations();
    }

    private void cacheRotations() {
        double rotationPointCount = 100;
        for (int i = 0; i < rotationPointCount; i++) {
            double rotation = (Math.PI * 2) / rotationPointCount * i;
            List<Vector> vectors = new ArrayList<>();
            for (Vector vector : cachedLocations) {
                vectors.add(vector.clone().rotateAroundY(rotation));
            }
            cachedRotations.add(vectors);
        }
    }

    private void trace(Vector source, Vector target) {
        Vector ray = target.clone().subtract(source).normalize().multiply(.2);
        Vector clonedSource = source.clone();
        int counter = 0;
        while (clonedSource.distance(target) > 0.1 && counter < 20) {
            if (WormholesConfig.isReducedParticlesMode()) {
                if (counter % 2 == 0) {
                    clonedSource = clonedSource.add(ray);
                    cachedLocations.add(clonedSource.clone());
                }
            } else {
                clonedSource = clonedSource.add(ray);
                cachedLocations.add(clonedSource.clone());
            }
            counter++;
        }
    }

}
