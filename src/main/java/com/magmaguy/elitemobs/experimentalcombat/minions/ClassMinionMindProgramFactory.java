package com.magmaguy.elitemobs.experimentalcombat.minions;

import com.magmaguy.magmacore.ai.MindBehavior;
import com.magmaguy.magmacore.ai.MindContext;
import com.magmaguy.magmacore.ai.MindControl;
import com.magmaguy.magmacore.ai.MindProgram;
import com.magmaguy.magmacore.ai.MindStopReason;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/** Builds fresh native programs from three reusable, control-leased goal categories. */
final class ClassMinionMindProgramFactory {
    private static final double ATTACK_REACH_SQUARED = 2.75D * 2.75D;

    private ClassMinionMindProgramFactory() {
    }

    static MindProgram create(
            Plugin plugin,
            ClassMinionTheme theme,
            ClassMinionControlState state) {
        MindProgram.Builder builder = MindProgram.builder(
                new NamespacedKey(plugin, "class_minion/" + theme.name().toLowerCase()).toString(),
                1L);
        if (!theme.supportRole()) builder.behavior(new CombatBehavior(state));
        return builder
                .behavior(new FollowOwnerBehavior(state))
                .behavior(new LocalWanderBehavior(state))
                .build();
    }

    private static final class CombatBehavior implements MindBehavior {
        private static final Set<MindControl> CONTROLS = EnumSet.of(
                MindControl.MOVE, MindControl.LOOK, MindControl.TARGET, MindControl.ATTACK);
        private final ClassMinionControlState state;
        private long nextAttackTick;

        private CombatBehavior(ClassMinionControlState state) {
            this.state = state;
        }

        @Override
        public String identifier() {
            return "elitemobs:class_minion/combat";
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public Set<MindControl> controls() {
            return CONTROLS;
        }

        @Override
        public boolean canStart(MindContext context) {
            return state.target(context.entity()).isPresent();
        }

        @Override
        public boolean canContinue(MindContext context) {
            return canStart(context);
        }

        @Override
        public void tick(MindContext context) {
            LivingEntity target = state.target(context.entity()).orElse(null);
            if (target == null) return;
            context.actuator().setTarget(target);
            context.actuator().lookAt(target.getEyeLocation());
            if (context.entity().getLocation().distanceSquared(target.getLocation()) > ATTACK_REACH_SQUARED) {
                context.actuator().moveTo(target.getLocation(), 1.15D);
                return;
            }
            context.actuator().stopMoving();
            if (context.gameTick() < nextAttackTick) return;
            context.actuator().attack(target);
            nextAttackTick = context.gameTick() + state.attackPeriodTicks();
        }

        @Override
        public void stop(MindContext context, MindStopReason reason) {
            context.actuator().clearTarget();
            context.actuator().stopMoving();
        }
    }

    private static final class FollowOwnerBehavior implements MindBehavior {
        private static final Set<MindControl> CONTROLS = EnumSet.of(MindControl.MOVE, MindControl.LOOK);
        private final ClassMinionControlState state;

        private FollowOwnerBehavior(ClassMinionControlState state) {
            this.state = state;
        }

        @Override
        public String identifier() {
            return "elitemobs:class_minion/follow_owner";
        }

        @Override
        public int priority() {
            return 10;
        }

        @Override
        public Set<MindControl> controls() {
            return CONTROLS;
        }

        @Override
        public boolean canStart(MindContext context) {
            boolean combat = state.target(context.entity()).isPresent();
            return MinionBehaviorPolicy.choose(combat, state.ownerDistance(context.entity()), false)
                    == MinionBehaviorPolicy.Intent.FOLLOW;
        }

        @Override
        public boolean canContinue(MindContext context) {
            return canStart(context);
        }

        @Override
        public void tick(MindContext context) {
            Player owner = state.owner().orElse(null);
            if (owner == null) return;
            context.actuator().lookAt(owner.getEyeLocation());
            context.actuator().moveTo(owner.getLocation(), 1.1D);
        }

        @Override
        public void stop(MindContext context, MindStopReason reason) {
            context.actuator().stopMoving();
        }
    }

    private static final class LocalWanderBehavior implements MindBehavior {
        private static final Set<MindControl> CONTROLS = EnumSet.of(MindControl.MOVE, MindControl.LOOK);
        private final ClassMinionControlState state;
        private long nextWanderTick;
        private long stopAtTick;
        private Location destination;

        private LocalWanderBehavior(ClassMinionControlState state) {
            this.state = state;
        }

        @Override
        public String identifier() {
            return "elitemobs:class_minion/local_wander";
        }

        @Override
        public int priority() {
            return 20;
        }

        @Override
        public Set<MindControl> controls() {
            return CONTROLS;
        }

        @Override
        public boolean canStart(MindContext context) {
            boolean combat = state.target(context.entity()).isPresent();
            boolean ready = context.gameTick() >= nextWanderTick;
            return state.owner().isPresent()
                    && MinionBehaviorPolicy.choose(combat, state.ownerDistance(context.entity()), ready)
                    == MinionBehaviorPolicy.Intent.WANDER;
        }

        @Override
        public boolean canContinue(MindContext context) {
            if (destination == null || context.gameTick() >= stopAtTick) return false;
            if (state.target(context.entity()).isPresent()) return false;
            if (state.ownerDistance(context.entity()) > MinionBehaviorPolicy.SETTLED_RADIUS + 2D) return false;
            return context.perception().navigation().consecutiveStuckTicks() < 20;
        }

        @Override
        public void start(MindContext context) {
            Player owner = state.owner().orElse(null);
            if (owner == null) return;
            long seed = context.entity().getUniqueId().getLeastSignificantBits() ^ context.gameTick();
            double angle = Math.floorMod(seed, 360L) * Math.PI / 180D;
            double radius = 1.5D + Math.floorMod(seed >>> 8, 150L) / 100D;
            destination = owner.getLocation().clone().add(
                    Math.cos(angle) * radius, 0D, Math.sin(angle) * radius);
            if (!destination.getWorld().isChunkLoaded(
                    destination.getBlockX() >> 4, destination.getBlockZ() >> 4)) {
                destination = null;
                nextWanderTick = context.gameTick() + 40L;
                return;
            }
            stopAtTick = context.gameTick() + 40L;
            context.actuator().lookAt(destination);
            context.actuator().moveTo(destination, .8D);
        }

        @Override
        public void tick(MindContext context) {
            if (destination != null) context.actuator().lookAt(destination);
        }

        @Override
        public void stop(MindContext context, MindStopReason reason) {
            context.actuator().stopMoving();
            destination = null;
            long seed = context.entity().getUniqueId().getMostSignificantBits() ^ context.gameTick();
            nextWanderTick = context.gameTick() + 40L + Math.floorMod(seed, 41L);
        }
    }
}
