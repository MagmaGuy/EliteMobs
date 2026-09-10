package com.magmaguy.elitemobs.advancedcombat.presentation;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassLineage;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Bukkit renderer for one semantic, hard-budgeted class ability presentation plan. */
public final class BukkitClassAbilityPresentation {

    public Session begin(Player caster, ClassLineage lineage, FixedAbilitySpec spec) {
        return new Session(
                Objects.requireNonNull(caster, "caster"),
                Objects.requireNonNull(spec, "spec"),
                ClassAbilityPresentationPlanner.plan(lineage, spec));
    }

    public static final class Session {
        private final Player caster;
        private final FixedAbilitySpec spec;
        private final AbilityPresentationPlan plan;
        private final AbilityPresentationBudgetLedger budget;
        private final Set<UUID> renderedTargets = new HashSet<>();
        private boolean castRendered;
        private boolean mobilityStarted;
        private boolean mobilityFinished;

        private Session(Player caster, FixedAbilitySpec spec, AbilityPresentationPlan plan) {
            this.caster = caster;
            this.spec = spec;
            this.plan = plan;
            this.budget = plan.newBudgetLedger();
        }

        /** Commits the cast cue after mechanics have accepted the activation. */
        public void commit() {
            ensureCast();
        }

        /** Renders applicable target cues once per target for this cast. */
        public void effects(
                Location origin,
                List<? extends LivingEntity> enemies,
                List<? extends Player> allies) {
            ensureCast();
            for (LivingEntity enemy : enemies) {
                if (enemy == null || !renderedTargets.add(enemy.getUniqueId())) continue;
                Location target = center(enemy, origin);
                renderTarget(AbilityPresentationCue.IMPACT, target);
                renderTarget(AbilityPresentationCue.CONTROL, target);
            }
            for (Player ally : allies) {
                if (ally == null || !renderedTargets.add(ally.getUniqueId())) continue;
                Location target = center(ally, origin);
                renderTarget(AbilityPresentationCue.HEAL, target);
                renderTarget(AbilityPresentationCue.BUFF, target);
            }
        }

        /**
         * Renders physical movement or a completed transfer. Blink uses the actual accepted
         * destination. The flourish is visual only and never changes player velocity or position.
         */
        public void mobility(Location departure, Location arrival) {
            ensureCast();
            if (mobilityFinished || !plan.cues().contains(AbilityPresentationCue.MOBILITY)) return;
            if (departure == null || departure.getWorld() == null) return;

            boolean renderDeparture = !mobilityStarted;
            boolean hasArrival = sameWorld(departure, arrival)
                    && departure.distanceSquared(arrival) > 1.0E-6D;
            List<Double> samples = hasArrival && tracesTransfer(spec.family())
                    ? plan.traceProgress(departure.distance(arrival))
                    : List.of();
            int desiredParticles = (renderDeparture ? 12 : 0) + samples.size() * 2
                    + (hasArrival ? 18 : 0);
            int desiredSounds = spec.family() == AbilityFamily.SAFE_BLINK && hasArrival
                    ? 3
                    : (renderDeparture ? 1 : 0) + (hasArrival ? 1 : 0);
            AbilityPresentationBudgetLedger.Grant grant =
                    budget.claim(AbilityPresentationCue.MOBILITY, desiredParticles, desiredSounds);
            if (!grant.granted()) return;

            Palette palette = palette(plan.theme());
            int particles = grant.particles();
            if (renderDeparture) {
                int departureParticles = Math.min(12, particles);
                burst(departure.clone().add(0D, .8D, 0D), palette.primary(), palette.accent(),
                        departureParticles, .38D, .55D);
                particles -= departureParticles;
                mobilityStarted = true;
            }

            if (!samples.isEmpty() && particles > 0) {
                int arrivalReserve = Math.min(18, particles);
                int traceParticles = particles - arrivalReserve;
                renderTrace(departure, arrival, samples, palette, traceParticles);
                particles = arrivalReserve;
            }
            if (hasArrival && particles > 0)
                burst(arrival.clone().add(0D, .8D, 0D), palette.accent(), palette.primary(),
                        particles, .4D, .65D);

            playMobilitySounds(
                    departure, arrival, palette, grant.sounds(), renderDeparture, hasArrival);
            if (hasArrival) mobilityFinished = true;
        }

        private void ensureCast() {
            if (castRendered) return;
            castRendered = true;
            Location origin = caster.getLocation().clone().add(0D, caster.getHeight() * .55D, 0D);
            if (origin.getWorld() == null) return;
            AbilityPresentationBudgetLedger.Grant grant =
                    budget.claim(AbilityPresentationCue.CAST, 16, 1);
            if (!grant.granted()) return;
            Palette palette = palette(plan.theme());
            burst(origin, palette.primary(), palette.accent(), grant.particles(), .32D, .5D);
            if (grant.sounds() > 0)
                origin.getWorld().playSound(origin, palette.castSound(), .65F, 1.18F);
        }

        private void renderTarget(AbilityPresentationCue cue, Location target) {
            if (target == null || target.getWorld() == null || !plan.cues().contains(cue)) return;
            int desiredParticles = switch (cue) {
                case HEAL -> 16;
                case IMPACT -> 14;
                case BUFF -> 12;
                case CONTROL -> 10;
                default -> 0;
            };
            AbilityPresentationBudgetLedger.Grant grant = budget.claim(cue, desiredParticles, 1);
            if (!grant.granted()) return;

            Palette palette = palette(plan.theme());
            Particle primary = cueParticle(cue, plan.theme(), palette);
            Particle accent = cue == AbilityPresentationCue.HEAL
                    ? Particle.END_ROD
                    : palette.accent();
            burst(target, primary, accent, grant.particles(), .3D, .45D);
            if (grant.sounds() > 0)
                target.getWorld().playSound(target, cueSound(cue, palette), .55F, cuePitch(cue));
        }

        private void renderTrace(
                Location start,
                Location end,
                List<Double> samples,
                Palette palette,
                int particles) {
            if (particles <= 0 || samples.isEmpty()) return;
            Vector delta = end.toVector().subtract(start.toVector());
            int remaining = particles;
            for (int index = 0; index < samples.size() && remaining > 0; index++) {
                double progress = samples.get(index);
                Location point = start.clone().add(delta.clone().multiply(progress));
                point.add(0D, .8D + Math.sin(progress * Math.PI) * .18D, 0D);
                int remainingSamples = samples.size() - index;
                int count = Math.max(1, remaining / remainingSamples);
                count = Math.min(count, remaining);
                Particle primary = index % 2 == 0 ? palette.primary() : palette.accent();
                point.getWorld().spawnParticle(primary, point, count, .045D, .045D, .045D, .005D);
                remaining -= count;
            }
        }

        private void playMobilitySounds(
                Location departure,
                Location arrival,
                Palette palette,
                int sounds,
                boolean renderDeparture,
                boolean hasArrival) {
            if (sounds <= 0 || departure.getWorld() == null) return;
            if (spec.family() == AbilityFamily.SAFE_BLINK && hasArrival) {
                departure.getWorld().playSound(
                        departure, Sound.ENTITY_ENDERMAN_TELEPORT, .62F, 1.55F);
                sounds--;
                if (sounds > 0 && arrival.getWorld() != null) {
                    arrival.getWorld().playSound(
                            arrival, Sound.ENTITY_ENDERMAN_TELEPORT, .88F, 1.18F);
                    sounds--;
                }
                if (sounds > 0 && arrival.getWorld() != null)
                    arrival.getWorld().playSound(
                            arrival, Sound.BLOCK_AMETHYST_BLOCK_CHIME, .58F, 1.35F);
                return;
            }
            if (renderDeparture) {
                departure.getWorld().playSound(departure, palette.castSound(), .55F, 1.35F);
                sounds--;
            }
            if (sounds > 0 && hasArrival && arrival.getWorld() != null)
                arrival.getWorld().playSound(arrival, palette.impactSound(), .65F, 1.1F);
        }

        private static Location center(LivingEntity entity, Location fallback) {
            Location location = entity.getLocation().add(0D, entity.getHeight() * .55D, 0D);
            return location.getWorld() == null ? fallback : location;
        }
    }

    private static boolean tracesTransfer(AbilityFamily family) {
        return family == AbilityFamily.SAFE_BLINK || family == AbilityFamily.ALLY_FLIGHT;
    }

    private static boolean sameWorld(Location first, Location second) {
        return first != null && second != null
                && first.getWorld() != null && first.getWorld().equals(second.getWorld());
    }

    private static void burst(
            Location location,
            Particle primary,
            Particle accent,
            int count,
            double horizontalSpread,
            double verticalSpread) {
        World world = location.getWorld();
        if (world == null || count <= 0) return;
        int accentCount = Math.max(1, count / 3);
        int primaryCount = count - accentCount;
        if (primaryCount > 0)
            world.spawnParticle(primary, location, primaryCount,
                    horizontalSpread, verticalSpread, horizontalSpread, .025D);
        world.spawnParticle(accent, location, accentCount,
                horizontalSpread * .7D, verticalSpread * .8D, horizontalSpread * .7D, .012D);
    }

    private static Particle cueParticle(
            AbilityPresentationCue cue,
            AbilityPresentationTheme theme,
            Palette palette) {
        if (cue == AbilityPresentationCue.HEAL) return switch (theme) {
            case NATURE -> Particle.COMPOSTER;
            case SPIRIT, SHADOW -> Particle.SOUL;
            default -> Particle.HAPPY_VILLAGER;
        };
        if (cue == AbilityPresentationCue.BUFF) return switch (theme) {
            case FROST -> Particle.SNOWFLAKE;
            case SHADOW, SPIRIT -> Particle.SOUL;
            default -> Particle.END_ROD;
        };
        return palette.primary();
    }

    private static Sound cueSound(AbilityPresentationCue cue, Palette palette) {
        return switch (cue) {
            case HEAL -> Sound.BLOCK_AMETHYST_BLOCK_CHIME;
            case BUFF -> Sound.BLOCK_BEACON_POWER_SELECT;
            default -> palette.impactSound();
        };
    }

    private static float cuePitch(AbilityPresentationCue cue) {
        return switch (cue) {
            case HEAL -> 1.45F;
            case BUFF -> 1.3F;
            case CONTROL -> .85F;
            default -> 1.05F;
        };
    }

    private static Palette palette(AbilityPresentationTheme theme) {
        return switch (theme) {
            case VALOR -> new Palette(Particle.END_ROD, Particle.ENCHANT,
                    Sound.BLOCK_BEACON_POWER_SELECT, Sound.ITEM_SHIELD_BLOCK);
            case FURY -> new Palette(Particle.CRIT, Particle.DAMAGE_INDICATOR,
                    Sound.ENTITY_RAVAGER_ROAR, Sound.ENTITY_IRON_GOLEM_ATTACK);
            case HUNT -> new Palette(Particle.CRIT, Particle.ELECTRIC_SPARK,
                    Sound.ENTITY_ARROW_SHOOT, Sound.ENTITY_PLAYER_ATTACK_CRIT);
            case ENGINEERING -> new Palette(Particle.SMOKE, Particle.ELECTRIC_SPARK,
                    Sound.BLOCK_PISTON_EXTEND, Sound.ENTITY_FIREWORK_ROCKET_BLAST);
            case STORM -> new Palette(Particle.ELECTRIC_SPARK, Particle.CLOUD,
                    Sound.ITEM_TRIDENT_RETURN, Sound.ENTITY_LIGHTNING_BOLT_IMPACT);
            case RADIANT -> new Palette(Particle.HAPPY_VILLAGER, Particle.END_ROD,
                    Sound.BLOCK_AMETHYST_BLOCK_CHIME, Sound.BLOCK_BEACON_POWER_SELECT);
            case NATURE -> new Palette(Particle.COMPOSTER, Particle.HAPPY_VILLAGER,
                    Sound.BLOCK_GRASS_PLACE, Sound.BLOCK_AMETHYST_BLOCK_CHIME);
            case SPIRIT -> new Palette(Particle.SOUL, Particle.END_ROD,
                    Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, Sound.ENTITY_EVOKER_CAST_SPELL);
            case ARCANE -> new Palette(Particle.WITCH, Particle.ENCHANT,
                    Sound.ENTITY_EVOKER_CAST_SPELL, Sound.BLOCK_ENCHANTMENT_TABLE_USE);
            case FLAME -> new Palette(Particle.FLAME, Particle.SOUL_FIRE_FLAME,
                    Sound.ENTITY_BLAZE_SHOOT, Sound.ITEM_FIRECHARGE_USE);
            case FROST -> new Palette(Particle.SNOWFLAKE, Particle.CLOUD,
                    Sound.ENTITY_SNOWBALL_THROW, Sound.BLOCK_GLASS_BREAK);
            case SHADOW -> new Palette(Particle.SOUL, Particle.SCULK_SOUL,
                    Sound.ENTITY_WITHER_SHOOT, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE);
        };
    }

    private record Palette(
            Particle primary,
            Particle accent,
            Sound castSound,
            Sound impactSound) {
    }
}
