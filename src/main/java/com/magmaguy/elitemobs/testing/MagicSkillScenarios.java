package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.ProcRoll;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.*;
import com.magmaguy.magmacore.enchantments.EnchantmentItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Behavior scenarios for the staff and wand skills, run by the combat diagnostic in place of its
 * proc-counting level sweep. Every cast goes through FreeMinecraftModels' own input handling, so
 * fireballs, staff strikes and wand missiles fly, land and reach the skills as they do in play.
 * Each scenario selects one skill, stages the situation that skill is about and checks the outcome
 * it promises, next to a control where it must stay quiet. Proc chances are forced so an effect
 * can be checked on demand; the chances themselves are plain configuration.
 * <p>
 * Dummies are placed relative to where the player stands and faces, so run it on open, flat ground
 * with about 14 blocks free behind the player and 16 in front.
 */
final class MagicSkillScenarios implements Listener {
    static final int LEVEL = 75;
    private static final String DUMMY_CONFIG = "damage_test_dummy.yml";
    private static final String MULTICAST = "freeminecraftmodels:multicast";
    private static final int LANDING_TIMEOUT_TICKS = 60;
    private static final int SETTLE_TICKS = 5;
    private static final double MULTIPLIER_TOLERANCE = 0.002;
    private static final double DAMAGE_TOLERANCE = 0.01;
    private static final double INCOMING_HIT = 10;

    private final Player player;
    private final UUID playerId;
    private final SkillType type;
    private final CombatSimulator simulator;
    private final TestReport report;
    private final CombatTestLog log;
    private final Runnable onComplete;

    private final Deque<Step> steps = new ArrayDeque<>();
    private final Map<String, Dummy> dummies = new LinkedHashMap<>();
    private final Map<String, UUID> ids = new HashMap<>();
    private final Map<String, Location> marks = new HashMap<>();
    private final Map<String, Double> values = new HashMap<>();
    private final List<Hit> hits = new ArrayList<>();
    private final List<Cast> casts = new ArrayList<>();
    private final Map<EliteMobDamagedByPlayerEvent, Double> enteringSkills = new IdentityHashMap<>();
    private final Map<org.bukkit.event.entity.EntityDamageByEntityEvent, Integer> unsettled = new IdentityHashMap<>();
    private long scenarioStart;
    // Every damage event the player dealt, filtered or not, to explain a cast that never landed.
    private final List<String> trace = new ArrayList<>();
    private final Map<MagicStrike, Long> readyAt = new EnumMap<>(MagicStrike.class);
    private Location anchor;
    private Vector forward, right;
    private SkillTestResult result;
    private PotionEffect casterSlownessAfterCast;
    private String lastClick = "";
    // The diagnostic blocks the tester's own clicks; the casts it fires itself must still reach FMM.
    private static boolean firingCast;
    private BukkitTask ticker;
    private long tick;
    private int plannedCasts;
    private boolean closed;

    private interface Step {
        boolean advance();
    }

    private record Dummy(CustomBossEntity boss, LivingEntity body, Location spawn) {
    }

    /**
     * One damage event the player dealt; side hits (burns, arcs, bolts) carry no attack. A hit that
     * another listener cancelled after EliteMobs scored it never {@code landed}.
     */
    private record Hit(long tick, UUID target, UUID attackId, MagicStrike strike,
                       double multiplier, double damage, double nonCritical, boolean landed) {
        Hit cancelled() {
            return new Hit(tick, target, attackId, strike, multiplier, damage, nonCritical, false);
        }
    }

    private record Cast(UUID attackId, long landedAt, List<Hit> hits) {
        Hit on(UUID target) {
            return hits.stream().filter(hit -> hit.target().equals(target)).findFirst().orElse(null);
        }
    }

    MagicSkillScenarios(Player player, SkillType type, CombatSimulator simulator, TestReport report,
                        CombatTestLog log, Runnable onComplete) {
        this.player = player;
        this.playerId = player.getUniqueId();
        this.type = type;
        this.simulator = simulator;
        this.report = report;
        this.log = log;
        this.onComplete = onComplete;
    }

    static boolean covers(SkillType type) {
        return type == SkillType.STAVES || type == SkillType.WANDS;
    }

    void start() {
        if (!Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")
                || !com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI.isOperational()) {
            for (SkillBonus skill : SkillBonusRegistry.getEnabledBonuses(type)) {
                SkillTestResult skipped = new SkillTestResult(skill.getSkillId(), skill.getBonusName(), type, LEVEL);
                skipped.markSkipped("needs FreeMinecraftModels magic weapons");
                report.addSkippedResult(skipped);
            }
            onComplete.run();
            return;
        }
        anchor = player.getLocation().clone();
        forward = anchor.getDirection().setY(0);
        if (forward.lengthSquared() < 1.0E-6) forward = new Vector(0, 0, 1);
        forward.normalize();
        right = forward.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        // Skills of other weapons and armor must not change what the player deals or takes.
        SkillBonusRegistry.removeAllBonuses(player);
        Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
        if (type == SkillType.STAVES) staffScenarios();
        else wandScenarios();
        steps.add(() -> {
            close();
            onComplete.run();
            return true;
        });
        ticker = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::advance, 1L, 1L);
    }

    void close() {
        if (closed) return;
        closed = true;
        steps.clear();
        if (ticker != null) ticker.cancel();
        HandlerList.unregisterAll(this);
        removeDummies();
        ProcRoll.clear(playerId);
        resetTypeSkills();
        if (anchor != null && player.isOnline()) player.teleport(anchor);
    }

    // ==================== STAVES ====================

    private void staffScenarios() {
        scenario("staves_casting_baseline", "Staff casts without skills", LEVEL);
        spawn("A", 6, 0);
        spawn("B", 6, 1.3);
        spawn("C", 6, -1.3);
        spawn("M", 2.5, 2.5);
        int plainFireball = cast(MagicStrike.STAFF_FIREBALL, "A");
        expectBlast("A fireball hits every dummy in its blast at plain damage", plainFireball, 3, () -> 1);
        int plainStrike = cast(MagicStrike.STAFF_MELEE, "M");
        expectMultiplier("A staff strike lands as a staff strike at plain damage", plainStrike, "M", () -> 1);
        end();

        if (enabled(SmolderSkill.SKILL_ID)) {
            scenario(SmolderSkill.SKILL_ID, "Smolder", LEVEL, SmolderSkill.SKILL_ID);
            spawn("A", 6, 0);
            spawn("M", 2.5, 2.5);
            force(SmolderSkill.SKILL_ID, true);
            int burning = cast(MagicStrike.STAFF_FIREBALL, "A");
            waitAfterLanding(burning, 90);
            expectPulses("A proc burns the target in 4 pulses, one second apart", "A", burning, 4);
            expectNear("The burn totals the skill's share of the fireball hit",
                    () -> skill(SmolderSkill.SKILL_ID).getBonusValue(LEVEL) * nonCritical(burning, "A"),
                    () -> sideDamage("A", landed(burning)), DAMAGE_TOLERANCE);
            check("Burn pulses do not trigger skills again",
                    () -> skill(SmolderSkill.SKILL_ID).getProcCount(player) == 1,
                    () -> "procs " + skill(SmolderSkill.SKILL_ID).getProcCount(player));
            force(SmolderSkill.SKILL_ID, false);
            int plain = cast(MagicStrike.STAFF_FIREBALL, "A");
            waitAfterLanding(plain, 90);
            expectNoSideHits("Without a proc the fireball leaves no burn", "A", plain);
            force(SmolderSkill.SKILL_ID, true);
            int melee = cast(MagicStrike.STAFF_MELEE, "M");
            waitAfterLanding(melee, 40);
            expectNoSideHits("Staff strikes never smolder", "M", melee);
            end();
        }

        if (enabled(ConcussiveBlastSkill.SKILL_ID)) {
            scenario(ConcussiveBlastSkill.SKILL_ID, "Concussive Blast", LEVEL, ConcussiveBlastSkill.SKILL_ID);
            spawnLoose("A", 6, 0);
            force(ConcussiveBlastSkill.SKILL_ID, false);
            knockback("plain", MagicStrike.STAFF_FIREBALL, "A");
            check("Without a proc the fireball does not slow", () -> value("plain.slowed") == 0, () -> "slowness level " + value("plain.slowed"));
            force(ConcussiveBlastSkill.SKILL_ID, true);
            knockback("proc", MagicStrike.STAFF_FIREBALL, "A");
            check("A proc throws the target at least a block farther than a plain fireball",
                    () -> value("proc") >= value("plain") + 1.0,
                    () -> "plain fireball moved it " + format(value("plain")) + ", proc " + format(value("proc")));
            check("…and gives it Slowness II for 2 seconds", () -> value("proc.slowed") == 2, () -> "slowness level " + value("proc.slowed"));
            end();
        }

        if (enabled(BattlemagesGuardSkill.SKILL_ID)) {
            scenario(BattlemagesGuardSkill.SKILL_ID, "Battlemage's Guard", LEVEL, BattlemagesGuardSkill.SKILL_ID);
            spawn("N", 3, 0);
            spawn("F", 7, 0);
            measure("near", () -> simulator.takeHitWithOverride(body("N"), INCOMING_HIT));
            measure("far", () -> simulator.takeHitWithOverride(body("F"), INCOMING_HIT));
            act(() -> simulator.holdTestItem(new ItemStack(Material.STICK)));
            measure("unarmed", () -> simulator.takeHitWithOverride(body("N"), INCOMING_HIT));
            act(() -> simulator.equipWeapon(type));
            expectNear("Hits from enemies within 4 blocks are reduced by the skill's share",
                    () -> INCOMING_HIT * (1 - skill(BattlemagesGuardSkill.SKILL_ID).getBonusValue(LEVEL)),
                    () -> value("near"), DAMAGE_TOLERANCE);
            expectNear("Hits from farther away are not reduced", () -> INCOMING_HIT, () -> value("far"), DAMAGE_TOLERANCE);
            expectNear("Without a staff in hand there is no reduction", () -> INCOMING_HIT, () -> value("unarmed"), DAMAGE_TOLERANCE);
            end();
        }

        if (enabled(StokeTheFlamesSkill.SKILL_ID)) {
            scenario(StokeTheFlamesSkill.SKILL_ID, "Stoke the Flames", LEVEL, StokeTheFlamesSkill.SKILL_ID);
            spawn("A", 6, 0);
            spawn("B", 6, 1.3);
            spawn("C", 6, -1.3);
            spawn("M", 2.5, 2.5);
            int last = -1;
            for (int fireball = 0; fireball < 7; fireball++) {
                int stacks = Math.min(fireball, 5);
                last = cast(MagicStrike.STAFF_FIREBALL, "A");
                expectBlast("Fireball " + (fireball + 1) + " carries " + stacks
                                + " heat on every target it hits (one stack per fireball, at most 5)", last, 3,
                        () -> 1 + stacks * stacking(StokeTheFlamesSkill.SKILL_ID).getBonusPerStack(LEVEL));
                if (fireball == 2) {
                    int melee = cast(MagicStrike.STAFF_MELEE, "M");
                    expectMultiplier("Staff strikes neither use nor build heat", melee, "M", () -> 1);
                }
            }
            waitAfterLanding(last, 130);
            int cooled = cast(MagicStrike.STAFF_FIREBALL, "A");
            expectBlast("Heat fades 6 seconds after the last fireball", cooled, 3, () -> 1);
            end();
        }

        if (enabled(WildfireSkill.SKILL_ID)) {
            DoubleSupplier perEnemy = () -> skill(WildfireSkill.SKILL_ID).getBonusValue(LEVEL) / 4;
            scenario(WildfireSkill.SKILL_ID, "Wildfire", LEVEL, WildfireSkill.SKILL_ID);
            spawn("L", 6, 0);
            int alone = cast(MagicStrike.STAFF_FIREBALL, "L");
            expectMultiplier("A lone target gets no crowd bonus", alone, "L", () -> 1);
            remove("L");
            spawn("A", 6, 0);
            spawn("B", 6, 1.3);
            spawn("C", 6, -1.3);
            int crowd = cast(MagicStrike.STAFF_FIREBALL, "A");
            expectCrowd("Each hit grows by the skill's share for every other enemy within 4 blocks of it", crowd, perEnemy);
            spawn("D", 7.4, 0);
            spawn("E", 7.4, 1.3);
            spawn("F", 7.4, -1.3);
            int packed = cast(MagicStrike.STAFF_FIREBALL, "A");
            expectCrowd("The bonus stops growing at 4 nearby enemies", packed, perEnemy);
            check("That blast hit a target with more than 4 enemies around it",
                    () -> cast(packed).hits().stream().anyMatch(hit -> neighbours(hit.target()) > 4),
                    () -> describe(cast(packed)));
            end();
        }

        if (enabled(RepelSkill.SKILL_ID)) {
            scenario(RepelSkill.SKILL_ID, "Repel", LEVEL, RepelSkill.SKILL_ID);
            spawnLoose("M", 2.5, 0);
            spawnLoose("A", 6, 3);
            force(RepelSkill.SKILL_ID, false);
            knockback("strike", MagicStrike.STAFF_MELEE, "M");
            check("Without a proc the strike does not slow", () -> value("strike.slowed") == 0, () -> "slowness level " + value("strike.slowed"));
            knockback("fireball", MagicStrike.STAFF_FIREBALL, "A");
            force(RepelSkill.SKILL_ID, true);
            knockback("repel", MagicStrike.STAFF_MELEE, "M");
            check("A proc knocks the struck enemy at least a block farther than a plain strike",
                    () -> value("repel") >= value("strike") + 1.0,
                    () -> "plain strike moved it " + format(value("strike")) + ", proc " + format(value("repel")));
            check("…and gives it Slowness I for 2 seconds", () -> value("repel.slowed") == 1, () -> "slowness level " + value("repel.slowed"));
            knockback("forcedFireball", MagicStrike.STAFF_FIREBALL, "A");
            check("Fireballs never repel: with a forced proc they push and slow exactly like a plain fireball",
                    () -> Math.abs(value("forcedFireball") - value("fireball")) < 0.3 && value("forcedFireball.slowed") == 0,
                    () -> "plain fireball moved it " + format(value("fireball")) + ", forced " + format(value("forcedFireball"))
                            + ", slowness level " + value("forcedFireball.slowed"));
            end();
        }

        if (enabled(ScorchedEarthSkill.SKILL_ID)) {
            scenario(ScorchedEarthSkill.SKILL_ID, "Scorched Earth", LEVEL, ScorchedEarthSkill.SKILL_ID);
            spawn("T", 6, 0);
            spawn("B", 6, 1.8);
            spawn("C", 6, -3.6);
            force(ScorchedEarthSkill.SKILL_ID, true);
            int scorch = cast(MagicStrike.STAFF_FIREBALL, "T");
            waitAfterLanding(scorch, 90);
            expectPulses("The ground under the target burns 4 times, one second apart (one patch per fireball)", "T", scorch, 4);
            expectPulses("Other enemies standing in it burn the same way", "B", scorch, 4);
            expectNoSideHits("Enemies outside the 2.5 block patch are untouched", "C", scorch);
            check("Each pulse deals the skill's share of the fireball hit", () -> {
                List<Hit> pulses = sideHits("T", landed(scorch));
                double share = skill(ScorchedEarthSkill.SKILL_ID).getBonusValue(LEVEL) / 4;
                return !pulses.isEmpty() && cast(scorch).hits().stream().anyMatch(hit -> pulses.stream()
                        .allMatch(pulse -> close(pulse.damage(), share * hit.nonCritical(), DAMAGE_TOLERANCE)));
            }, () -> "pulses " + damages(sideHits("T", landed(scorch))) + ", fireball hits " + describe(cast(scorch)));
            end();
        }

        if (enabled(ImmolateSkill.SKILL_ID)) {
            DoubleSupplier immolated = () -> 1 + skill(ImmolateSkill.SKILL_ID).getBonusValue(LEVEL);
            scenario(ImmolateSkill.SKILL_ID, "Immolate", LEVEL, ImmolateSkill.SKILL_ID);
            spawn("A", 6, 0);
            int healthy = cast(MagicStrike.STAFF_FIREBALL, "A");
            expectMultiplier("Healthy targets take plain damage", healthy, "A", () -> 1);
            healthFraction("A", 0.20);
            int wounded = cast(MagicStrike.STAFF_FIREBALL, "A");
            expectMultiplier("Below 30% health the fireball deals the bonus", wounded, "A", immolated);
            healthFraction("A", 0.40);
            int recovered = cast(MagicStrike.STAFF_FIREBALL, "A");
            expectMultiplier("Above 30% health it does not", recovered, "A", () -> 1);
            end();
        }

        if (enabled(SunfallSkill.SKILL_ID)) {
            scenario(SunfallSkill.SKILL_ID, "Sunfall", LEVEL - 1, SunfallSkill.SKILL_ID);
            spawn("A", 6, 0);
            spawn("B", 6, 1.3);
            spawn("C", 6, -1.3);
            int locked = cast(MagicStrike.STAFF_FIREBALL, "A");
            expectBlast("Sunfall stays locked below level 75", locked, 3, () -> 1);
            level(LEVEL);
            int sunfall = cast(MagicStrike.STAFF_FIREBALL, "A");
            expectBlast("The next fireball lands with the bonus on every target in its blast", sunfall, 3,
                    () -> 1 + skill(SunfallSkill.SKILL_ID).getBonusValue(LEVEL));
            expectCooldown("…and starts the 20 second cooldown", SunfallSkill.SKILL_ID, 18, 20);
            int after = cast(MagicStrike.STAFF_FIREBALL, "A");
            expectBlast("The fireball after it is back to plain damage", after, 3, () -> 1);
            end();
        }

        if (enabled(PhoenixMantleSkill.SKILL_ID)) {
            scenario(PhoenixMantleSkill.SKILL_ID, "Phoenix Mantle", LEVEL, PhoenixMantleSkill.SKILL_ID);
            spawnLoose("N", 2.5, 0);
            spawn("F", 7, 0);
            mark("N");
            measure("saved", () -> simulator.takeFatalHit(body("N")));
            waitTicks(8);
            expectNear("A fatal blow leaves the player at exactly one heart", () -> 2.0, () -> value("saved"), DAMAGE_TOLERANCE);
            check("Enemies within 4 blocks are thrown back and set on fire",
                    () -> movedAway("N") >= 0.8 && body("N").getFireTicks() > 0,
                    () -> blocks(movedAway("N")) + ", fire ticks " + body("N").getFireTicks());
            check("Enemies farther away are untouched", () -> body("F").getFireTicks() <= 0,
                    () -> "fire ticks " + body("F").getFireTicks());
            measure("again", () -> simulator.takeFatalHit(body("N")));
            check("A second fatal blow inside the cooldown is not prevented", () -> value("again") > 2.5,
                    () -> "health after the blow " + value("again"));
            expectCooldown("The cooldown is 120 seconds", PhoenixMantleSkill.SKILL_ID, 118, 120);
            end();
        }
    }

    // ==================== WANDS ====================

    private void wandScenarios() {
        scenario("wands_casting_baseline", "Wand casts without skills", LEVEL);
        spawn("A", 8, 0);
        int plainMissile = cast(MagicStrike.WAND_MISSILE, "A");
        expectMultiplier("A wand missile lands as a missile at plain damage", plainMissile, "A", () -> 1);
        end();

        if (enabled(HexBrandSkill.SKILL_ID)) {
            DoubleSupplier branded = () -> 1 + skill(HexBrandSkill.SKILL_ID).getBonusValue(LEVEL);
            scenario(HexBrandSkill.SKILL_ID, "Hex Brand", LEVEL, HexBrandSkill.SKILL_ID);
            spawn("A", 8, 0);
            spawn("B", 8, -4);
            force(HexBrandSkill.SKILL_ID, true);
            int brand = cast(MagicStrike.WAND_MISSILE, "A");
            force(HexBrandSkill.SKILL_ID, false);
            expectMultiplier("The branding missile itself is not boosted", brand, "A", () -> 1);
            int follow = cast(MagicStrike.WAND_MISSILE, "A");
            expectMultiplier("Later missiles on the branded target deal the bonus", follow, "A", branded);
            int other = cast(MagicStrike.WAND_MISSILE, "B");
            expectMultiplier("Other targets are not branded", other, "B", () -> 1);
            waitAfterLanding(brand, 125);
            int faded = cast(MagicStrike.WAND_MISSILE, "A");
            expectMultiplier("The brand fades after 6 seconds", faded, "A", () -> 1);
            end();
        }

        if (enabled(ChillingBoltSkill.SKILL_ID)) {
            scenario(ChillingBoltSkill.SKILL_ID, "Chilling Bolt", LEVEL, ChillingBoltSkill.SKILL_ID);
            spawn("A", 8, 0);
            force(ChillingBoltSkill.SKILL_ID, true);
            int chilled = cast(MagicStrike.WAND_MISSILE, "A");
            waitAfterLanding(chilled, 2);
            expectSlowness("A proc gives the target Slowness II for 2 seconds", "A", 1);
            restore("A");
            force(ChillingBoltSkill.SKILL_ID, false);
            int plain = cast(MagicStrike.WAND_MISSILE, "A");
            waitAfterLanding(plain, 2);
            check("Without a proc the target is not slowed",
                    () -> body("A").getPotionEffect(PotionEffectType.SLOWNESS) == null, () -> slowness("A"));
            end();
        }

        if (enabled(ArcaneWardSkill.SKILL_ID)) {
            scenario(ArcaneWardSkill.SKILL_ID, "Arcane Ward", LEVEL, ArcaneWardSkill.SKILL_ID);
            spawn("S", 4, 0);
            measure("warded", () -> simulator.takeProjectileHitWithOverride(body("S"), INCOMING_HIT));
            measure("melee", () -> simulator.takeHitWithOverride(body("S"), INCOMING_HIT));
            act(() -> simulator.holdTestItem(new ItemStack(Material.STICK)));
            measure("unarmed", () -> simulator.takeProjectileHitWithOverride(body("S"), INCOMING_HIT));
            act(() -> simulator.equipWeapon(type));
            select();
            measure("plain", () -> simulator.takeProjectileHitWithOverride(body("S"), INCOMING_HIT));
            expectNear("Without the skill an elite's arrow lands in full", () -> INCOMING_HIT, () -> value("plain"), DAMAGE_TOLERANCE);
            expectNear("With it, arrows are reduced by the skill's share",
                    () -> INCOMING_HIT * (1 - skill(ArcaneWardSkill.SKILL_ID).getBonusValue(LEVEL)),
                    () -> value("warded"), DAMAGE_TOLERANCE);
            expectNear("Melee hits are not reduced", () -> INCOMING_HIT, () -> value("melee"), DAMAGE_TOLERANCE);
            expectNear("Without a wand in hand there is no reduction", () -> INCOMING_HIT, () -> value("unarmed"), DAMAGE_TOLERANCE);
            end();
        }

        if (enabled(SpellweaveSkill.SKILL_ID)) {
            scenario(SpellweaveSkill.SKILL_ID, "Spellweave", LEVEL, SpellweaveSkill.SKILL_ID);
            spawn("A", 8, 0);
            spawn("B", 8, -4);
            int last = -1;
            for (int missile = 0; missile < 10; missile++)
                last = expectWeave("Missile " + (missile + 1) + " on the same target carries "
                        + Math.min(missile, 8) + " stacks (at most 8)", "A", Math.min(missile, 8));
            expectWeave("A missile on another target breaks the weave", "B", 0);
            expectWeave("…which then starts over on that target", "B", 0);
            expectWeave("…and builds from there", "B", 1);
            expectWeave("Going back to the first target does not recover its stacks", "A", 0);
            last = expectWeave("…it starts over", "A", 0);
            waitAfterLanding(last, 70);
            expectWeave("The weave fades after 3 seconds without a missile", "A", 0);

            remove("B");
            spawn("B", 8, 1.5);
            act(this::holdMulticastWand);
            select(SpellweaveSkill.SKILL_ID);
            int first = plannedCasts;
            for (int missile = 0; missile < 6; missile++) cast(MagicStrike.WAND_MISSILE, "A");
            check("Each Multicast cast lands one missile on each of the two enemies",
                    () -> casts.subList(first, first + 6).stream().allMatch(cast -> cast.on(id("A")) != null && cast.on(id("B")) != null),
                    () -> casts.subList(first, first + 6).stream().map(this::describe).collect(Collectors.joining(" / ")));
            check("With Multicast the focused target keeps building stacks while the other missile stays plain", () -> {
                double perStack = stacking(SpellweaveSkill.SKILL_ID).getBonusPerStack(LEVEL);
                UUID woven = multiplierOn(first + 5, "A") > multiplierOn(first + 5, "B") ? id("A") : id("B");
                UUID other = woven.equals(id("A")) ? id("B") : id("A");
                for (int index = 0; index < 6; index++) {
                    Cast cast = casts.get(first + index);
                    if (cast.on(woven) == null || cast.on(other) == null) return false;
                    if (!closeAbsolute(cast.on(woven).multiplier(), 1 + index * perStack)) return false;
                    if (!closeAbsolute(cast.on(other).multiplier(), 1)) return false;
                }
                return true;
            }, () -> casts.subList(first, first + 6).stream().map(this::describe).collect(Collectors.joining(" / ")));
            act(() -> simulator.equipWeapon(type));
            end();
        }

        if (enabled(ArcingBoltSkill.SKILL_ID)) {
            scenario(ArcingBoltSkill.SKILL_ID, "Arcing Bolt", LEVEL, ArcingBoltSkill.SKILL_ID);
            spawn("A", 8, 0);
            spawn("B", 8, -3);
            spawn("C", 8, -7);
            force(ArcingBoltSkill.SKILL_ID, true);
            int arc = cast(MagicStrike.WAND_MISSILE, "A");
            waitAfterLanding(arc, 5);
            check("A proc arcs once to the nearest enemy within 5 blocks for the skill's share of the hit", () -> {
                List<Hit> arcs = sideHits("B", landed(arc));
                return arcs.size() == 1 && close(arcs.get(0).damage(),
                        skill(ArcingBoltSkill.SKILL_ID).getBonusValue(LEVEL) * damageOn(arc, "A"), DAMAGE_TOLERANCE);
            }, () -> "arcs " + damages(sideHits("B", landed(arc))) + ", missile " + describe(cast(arc)));
            expectNoSideHits("The arc does not chain on to the next enemy", "C", arc);
            remove("B");
            remove("C");
            int lone = cast(MagicStrike.WAND_MISSILE, "A");
            waitAfterLanding(lone, 5);
            expectNoSideHits("With no second enemy nearby there is no arc", "A", lone);
            end();
        }

        if (enabled(DuelistsFocusSkill.SKILL_ID)) {
            DoubleSupplier focused = () -> 1 + skill(DuelistsFocusSkill.SKILL_ID).getBonusValue(LEVEL);
            scenario(DuelistsFocusSkill.SKILL_ID, "Duelist's Focus", LEVEL, DuelistsFocusSkill.SKILL_ID);
            spawn("A", 8, 0);
            int alone = cast(MagicStrike.WAND_MISSILE, "A");
            expectMultiplier("Alone with the target, missiles deal the bonus", alone, "A", focused);
            spawn("W", -8, 0);
            int watched = cast(MagicStrike.WAND_MISSILE, "A");
            expectMultiplier("Another enemy within 12 blocks of the player cancels it", watched, "A", () -> 1);
            move("W", -13, 0);
            int distant = cast(MagicStrike.WAND_MISSILE, "A");
            expectMultiplier("Enemies farther than 12 blocks do not count", distant, "A", focused);
            end();
        }

        if (enabled(UnravelSkill.SKILL_ID)) {
            DoubleSupplier unravelled = () -> 1 + skill(UnravelSkill.SKILL_ID).getBonusValue(LEVEL);
            scenario(UnravelSkill.SKILL_ID, "Unravel", LEVEL, UnravelSkill.SKILL_ID);
            spawn("A", 8, 0);
            spawn("B", 8, -4);
            force(UnravelSkill.SKILL_ID, true);
            int unravel = cast(MagicStrike.WAND_MISSILE, "A");
            force(UnravelSkill.SKILL_ID, false);
            expectMultiplier("The unravelling missile itself is not boosted", unravel, "A", () -> 1);
            int follow = cast(MagicStrike.WAND_MISSILE, "A");
            expectMultiplier("Later hits on the target deal the bonus", follow, "A", unravelled);
            int other = cast(MagicStrike.WAND_MISSILE, "B");
            expectMultiplier("Other targets are not unravelled", other, "B", () -> 1);
            select();
            int party = cast(MagicStrike.WAND_MISSILE, "A");
            expectMultiplier("The bonus holds for an attacker without Unravel, so the whole party gets it",
                    party, "A", unravelled);
            waitAfterLanding(unravel, 105);
            int faded = cast(MagicStrike.WAND_MISSILE, "A");
            expectMultiplier("It fades after 5 seconds", faded, "A", () -> 1);
            end();
        }

        if (enabled(EchoBoltSkill.SKILL_ID)) {
            // Hex Brand rides along as a witness: if the echo went through the skills, it would proc twice.
            boolean witness = enabled(HexBrandSkill.SKILL_ID);
            if (witness) scenario(EchoBoltSkill.SKILL_ID, "Echo Bolt", LEVEL, EchoBoltSkill.SKILL_ID, HexBrandSkill.SKILL_ID);
            else scenario(EchoBoltSkill.SKILL_ID, "Echo Bolt", LEVEL, EchoBoltSkill.SKILL_ID);
            spawn("A", 8, 0);
            force(EchoBoltSkill.SKILL_ID, true);
            force(HexBrandSkill.SKILL_ID, true);
            int echoed = cast(MagicStrike.WAND_MISSILE, "A");
            waitAfterLanding(echoed, 20);
            check("A proc repeats the hit once, half a second later, for the same damage", () -> {
                List<Hit> echoes = sideHits("A", landed(echoed));
                return echoes.size() == 1 && echoes.get(0).tick() - landed(echoed) >= 9
                        && echoes.get(0).tick() - landed(echoed) <= 12
                        && close(echoes.get(0).damage(), damageOn(echoed, "A"), DAMAGE_TOLERANCE);
            }, () -> "echo ticks " + ticks(sideHits("A", landed(echoed)), landed(echoed))
                    + ", damages " + damages(sideHits("A", landed(echoed))) + ", missile " + describe(cast(echoed)));
            if (witness)
                check("The echo does not trigger other skills", () -> skill(HexBrandSkill.SKILL_ID).getProcCount(player) == 1,
                        () -> "Hex Brand procs " + skill(HexBrandSkill.SKILL_ID).getProcCount(player));
            force(EchoBoltSkill.SKILL_ID, false);
            force(HexBrandSkill.SKILL_ID, false);
            int plain = cast(MagicStrike.WAND_MISSILE, "A");
            waitAfterLanding(plain, 20);
            expectNoSideHits("Without a proc there is no echo", "A", plain);
            end();
        }

        if (enabled(StarfallVolleySkill.SKILL_ID)) {
            scenario(StarfallVolleySkill.SKILL_ID, "Starfall Volley", LEVEL - 1, StarfallVolleySkill.SKILL_ID);
            spawn("A", 8, 0);
            spawn("D1", 8, 2);
            spawn("D2", 8, -2);
            spawn("D3", 10, 0);
            spawn("D4", 8, 6);
            int locked = cast(MagicStrike.WAND_MISSILE, "A");
            waitAfterLanding(locked, 30);
            check("Starfall stays locked below level 75", () -> sideHitsSince(landed(locked)).isEmpty(),
                    () -> sideHitsSince(landed(locked)).size() + " bolts");
            level(LEVEL);
            int volley = cast(MagicStrike.WAND_MISSILE, "A");
            waitAfterLanding(volley, 30);
            check("A missile hit calls down 8 bolts, 2 on the target and on each of the 3 nearest enemies", () -> {
                long from = landed(volley);
                return sideHitsSince(from).size() == 8
                        && List.of("A", "D1", "D2", "D3").stream().allMatch(name -> sideHits(name, from).size() == 2)
                        && sideHits("D4", from).isEmpty();
            }, () -> boltSpread(landed(volley)));
            check("Each bolt deals the skill's share of the missile hit", () -> {
                double bolt = skill(StarfallVolleySkill.SKILL_ID).getBonusValue(LEVEL) * damageOn(volley, "A");
                List<Hit> bolts = sideHitsSince(landed(volley));
                return !bolts.isEmpty() && bolts.stream().allMatch(hit -> close(hit.damage(), bolt, DAMAGE_TOLERANCE));
            }, () -> "bolts " + damages(sideHitsSince(landed(volley))) + ", missile " + describe(cast(volley)));
            expectCooldown("The cooldown is 20 seconds", StarfallVolleySkill.SKILL_ID, 17, 20);
            int after = cast(MagicStrike.WAND_MISSILE, "A");
            waitAfterLanding(after, 30);
            check("The next missile inside the cooldown calls down nothing", () -> sideHitsSince(landed(after)).isEmpty(),
                    () -> sideHitsSince(landed(after)).size() + " bolts");
            end();
        }

        if (enabled(SpellMirrorSkill.SKILL_ID)) {
            scenario(SpellMirrorSkill.SKILL_ID, "Spell Mirror", LEVEL, SpellMirrorSkill.SKILL_ID);
            spawn("N", 3, 0);
            act(() -> values.put("mirroredAt", (double) tick));
            measure("mirrored", () -> simulator.takeHitWithOverride(body("N"), INCOMING_HIT));
            waitTicks(2);
            expectNear("The first hit is negated", () -> 0, () -> value("mirrored"), DAMAGE_TOLERANCE);
            check("…and answered with the skill's multiple of it", () -> {
                List<Hit> counters = sideHits("N", (long) value("mirroredAt"));
                return counters.size() == 1 && close(counters.get(0).damage(),
                        INCOMING_HIT * skill(SpellMirrorSkill.SKILL_ID).getBonusValue(LEVEL), DAMAGE_TOLERANCE);
            }, () -> "counters " + damages(sideHits("N", (long) value("mirroredAt"))));
            measure("next", () -> simulator.takeHitWithOverride(body("N"), INCOMING_HIT));
            expectNear("Hits inside the cooldown land normally", () -> INCOMING_HIT, () -> value("next"), DAMAGE_TOLERANCE);
            expectCooldown("The cooldown is 15 seconds", SpellMirrorSkill.SKILL_ID, 14, 15);
            end();
        }

        scenario("wands_no_mobility", "No wand skill grants movement", LEVEL);
        spawn("A", 8, 0);
        act(() -> {
            values.put("walkSpeed", (double) player.getWalkSpeed());
            values.put("speedModifiers", (double) movementModifiers().size());
        });
        select(SkillBonusRegistry.getEnabledBonuses(SkillType.WANDS).stream().map(SkillBonus::getSkillId).toArray(String[]::new));
        for (int missile = 0; missile < 3; missile++) cast(MagicStrike.WAND_MISSILE, "A");
        check("Selecting every wand skill adds no movement speed", () -> player.getWalkSpeed() == value("walkSpeed")
                        && movementModifiers().size() == value("speedModifiers")
                        && player.getPotionEffect(PotionEffectType.SPEED) == null,
                () -> "walk speed " + player.getWalkSpeed() + ", movement modifiers " + movementModifiers());
        check("Casting still slows the caster", () -> casterSlownessAfterCast != null,
                () -> "slowness after the last cast: " + casterSlownessAfterCast);
        end();
    }

    // ==================== SCENARIO STEPS ====================

    private void scenario(String id, String name, int level, String... selected) {
        steps.add(() -> {
            removeDummies();
            ids.clear();
            marks.clear();
            values.clear();
            casts.clear();
            ProcRoll.clear(playerId);
            result = new SkillTestResult(id, name, type, LEVEL);
            scenarioStart = tick;
            log.log("Scenario: " + name);
            setLevel(level);
            applySelection(selected);
            simulator.equipWeapon(type);
            player.teleport(anchor);
            player.setHealth(player.getMaxHealth());
            player.setFireTicks(0);
            for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
            return true;
        });
        plannedCasts = 0;
    }

    private void end() {
        steps.add(new End());
    }

    /** Closes the current scenario; a scenario that throws skips ahead to here. */
    private final class End implements Step {
        @Override
        public boolean advance() {
            if (result != null) {
                Set<UUID> fired = casts.stream().map(Cast::attackId).filter(Objects::nonNull).collect(Collectors.toSet());
                List<Hit> scenarioHits = hits.stream().filter(hit -> hit.tick() >= scenarioStart).toList();
                List<String> stray = scenarioHits.stream()
                        .filter(hit -> hit.attackId() != null && !fired.contains(hit.attackId()))
                        .map(hit -> hit.strike() + " on " + name(hit.target())).distinct().toList();
                if (!stray.isEmpty()) issue("Casts fired that the scenario never made: " + stray);
                List<String> lost = scenarioHits.stream().filter(hit -> !hit.landed())
                        .map(hit -> (hit.strike() == null ? "side hit" : hit.strike().toString()) + " on " + name(hit.target())
                                + " for " + format(hit.damage())).toList();
                if (!lost.isEmpty()) issue("Hits were cancelled before dealing any damage: " + lost);
                report.addResult(result);
                String first = result.getIssues().isEmpty() ? "" : " §7" + result.getIssues().get(0);
                player.sendMessage((result.isPassed() ? "§a✓ " : "§c✗ ") + result.getSkillName() + first);
            }
            removeDummies();
            ProcRoll.clear(playerId);
            result = null;
            return true;
        }
    }

    private void advance() {
        tick++;
        try {
            while (!steps.isEmpty()) {
                if (!steps.peek().advance()) return;
                steps.poll();
            }
        } catch (RuntimeException failure) {
            if (result != null) result.addIssue("Scenario stopped: " + failure);
            log.log("  Scenario stopped: " + failure);
            while (!steps.isEmpty() && !(steps.peek() instanceof End)) steps.poll();
            if (steps.isEmpty()) {
                close();
                onComplete.run();
            }
        }
    }

    private void act(Runnable action) {
        steps.add(() -> {
            action.run();
            return true;
        });
    }

    private void waitTicks(int ticks) {
        long[] until = {-1};
        steps.add(() -> {
            if (until[0] < 0) until[0] = tick + ticks;
            return tick >= until[0];
        });
    }

    private void waitAfterLanding(int cast, int ticks) {
        steps.add(() -> tick >= landed(cast) + ticks);
    }

    private void measure(String key, DoubleSupplier measurement) {
        act(() -> values.put(key, measurement.getAsDouble()));
    }

    private void force(String skillId, boolean outcome) {
        act(() -> ProcRoll.force(playerId, skillId, outcome));
    }

    private void select(String... skillIds) {
        act(() -> applySelection(skillIds));
    }

    private void level(int level) {
        act(() -> {
            setLevel(level);
            SkillBonusRegistry.applyBonuses(player, type);
        });
    }

    private void setLevel(int level) {
        PlayerData.setSkillXP(playerId, type, SkillXPCalculator.totalXPForLevel(level));
    }

    private void applySelection(String... skillIds) {
        resetTypeSkills();
        for (String skillId : skillIds) PlayerSkillSelection.addActiveSkill(playerId, type, skillId, true);
        SkillBonusRegistry.applyBonuses(player, type);
    }

    /** Deselects and deactivates every skill of the weapon, clearing their state and cooldowns. */
    private void resetTypeSkills() {
        for (String active : List.copyOf(PlayerSkillSelection.getActiveSkills(playerId, type)))
            PlayerSkillSelection.removeActiveSkill(playerId, type, active);
        for (SkillBonus skill : SkillBonusRegistry.getAllBonuses()) {
            if (skill.getSkillType() != type) continue;
            if (skill.isActive(player)) skill.removeBonus(player);
            if (skill instanceof CooldownSkill cooldown) cooldown.endCooldown(player);
            skill.resetProcCount(player);
        }
    }

    private void holdMulticastWand() {
        ItemStack wand = player.getInventory().getItemInMainHand();
        simulator.holdTestItem(EnchantmentItems.prepareConstruction(List.of(MULTICAST))
                .applyCustom(wand, Map.of(MULTICAST, 1)));
    }

    // ==================== DUMMIES ====================

    private void spawn(String name, double ahead, double side) {
        spawn(name, ahead, side, false);
    }

    /** Spawns a dummy that takes knockback but never acts on its own. */
    private void spawnLoose(String name, double ahead, double side) {
        spawn(name, ahead, side, true);
    }

    private void spawn(String name, double ahead, double side, boolean loose) {
        act(() -> {
            Location at = place(ahead, side);
            CustomBossEntity boss = RegionalBossEntity.createTemporaryRegionalBossEntity(DUMMY_CONFIG, at);
            if (boss == null) throw new IllegalStateException("the damage test dummy is not loaded");
            boss.spawn(true);
            LivingEntity body = boss.getLivingEntity();
            if (body == null) throw new IllegalStateException("dummy " + name + " did not spawn");
            body.setMaximumNoDamageTicks(0);
            body.setCustomName("§e" + name);
            body.setCustomNameVisible(true);
            if (loose) {
                body.setAI(true);
                if (body instanceof Mob mob) mob.setAware(false);
            }
            dummies.put(name, new Dummy(boss, body, at.clone()));
            ids.put(name, body.getUniqueId());
        });
    }

    private Location place(double ahead, double side) {
        Location at = anchor.clone().add(forward.clone().multiply(ahead)).add(right.clone().multiply(side));
        for (int step = 0; step < 4 && !at.getBlock().isPassable(); step++) at.add(0, 1, 0);
        for (int step = 0; step < 4 && at.clone().add(0, -1, 0).getBlock().isPassable(); step++) at.add(0, -1, 0);
        at.setDirection(anchor.toVector().subtract(at.toVector()));
        return at;
    }

    private void move(String name, double ahead, double side) {
        act(() -> body(name).teleport(place(ahead, side)));
    }

    private void remove(String name) {
        act(() -> {
            Dummy dummy = dummies.remove(name);
            if (dummy != null) discard(dummy);
        });
    }

    /** Puts a loose dummy back on its spawn point with no effects left on it. */
    private void restore(String name) {
        act(() -> {
            Dummy dummy = dummies.get(name);
            dummy.body().teleport(dummy.spawn());
            dummy.body().setVelocity(new Vector());
            dummy.body().setFireTicks(0);
            for (PotionEffect effect : dummy.body().getActivePotionEffects())
                dummy.body().removePotionEffect(effect.getType());
        });
        waitTicks(3);
    }

    private void mark(String name) {
        act(() -> marks.put(name, body(name).getLocation().clone()));
    }

    private void healthFraction(String name, double fraction) {
        act(() -> {
            EliteEntity elite = EntityTracker.getEliteMobEntity(body(name));
            elite.setHealth(elite.getMaxHealth() * fraction);
        });
    }

    private void removeDummies() {
        for (Dummy dummy : dummies.values()) discard(dummy);
        dummies.clear();
    }

    private static void discard(Dummy dummy) {
        LivingEntity body = dummy.body();
        if (body.isValid()) {
            body.setHealth(0);
            body.remove();
        }
    }

    private LivingEntity body(String name) {
        Dummy dummy = dummies.get(name);
        if (dummy == null) throw new IllegalStateException("no dummy " + name);
        return dummy.body();
    }

    private UUID id(String name) {
        UUID id = ids.get(name);
        if (id == null) throw new IllegalStateException("no dummy " + name);
        return id;
    }

    // ==================== CASTS ====================

    /** Queues one cast at a dummy and returns its index in this scenario. */
    private int cast(MagicStrike strike, String target) {
        int index = plannedCasts++;
        int[] firstHit = {-1};
        long[] firedAt = {-1};
        steps.add(() -> tick >= readyAt.getOrDefault(strike, 0L));
        steps.add(() -> {
            LivingEntity body = body(target);
            aimAt(body);
            firstHit[0] = hits.size();
            firedAt[0] = tick;
            lastClick = "";
            switch (strike) {
                case STAFF_MELEE -> player.attack(body);
                case STAFF_FIREBALL -> click(Action.RIGHT_CLICK_AIR);
                case WAND_MISSILE -> click(Action.LEFT_CLICK_AIR);
            }
            casterSlownessAfterCast = player.getPotionEffect(PotionEffectType.SLOWNESS);
            lastClick += ", weapon cooldown " + player.getCooldown(player.getInventory().getItemInMainHand().getType());
            readyAt.put(strike, tick + reloadTicks(strike) + 1);
            return true;
        });
        steps.add(() -> {
            Hit landing = firstHitOf(firstHit[0], strike);
            if (landing == null) {
                if (tick - firedAt[0] < LANDING_TIMEOUT_TICKS) return false;
                issue("The " + strikeName(strike) + " aimed at " + target + " never landed (click " + lastClick
                        + ", caster slowed by the cast: " + (casterSlownessAfterCast != null) + ", damage events since: "
                        + trace.stream().filter(line -> Long.parseLong(line.substring(0, line.indexOf(':'))) >= firedAt[0])
                        .limit(6).toList() + ")");
                casts.add(new Cast(null, firedAt[0], List.of()));
                return true;
            }
            // Multicast missiles can land a few ticks apart; keep collecting until they stop.
            List<Hit> landed = hits.subList(firstHit[0], hits.size()).stream()
                    .filter(hit -> landing.attackId().equals(hit.attackId())).toList();
            long lastLanding = landed.get(landed.size() - 1).tick();
            if (tick - lastLanding < SETTLE_TICKS && tick - landing.tick() < 40) return false;
            casts.add(new Cast(landing.attackId(), landing.tick(), landed));
            return true;
        });
        return index;
    }

    private void aimAt(LivingEntity target) {
        Location center = target.getLocation().add(0, Math.max(.35, target.getHeight() * .52), 0);
        Location look = anchor.clone();
        look.setDirection(center.toVector().subtract(anchor.clone().add(0, player.getEyeHeight(), 0).toVector()));
        player.teleport(look);
    }

    private void click(Action action) {
        PlayerInteractEvent click = new PlayerInteractEvent(player, action,
                player.getInventory().getItemInMainHand(), null, BlockFace.SELF, EquipmentSlot.HAND);
        firingCast = true;
        try {
            Bukkit.getPluginManager().callEvent(click);
        } finally {
            firingCast = false;
        }
        lastClick = "cancelled " + click.isCancelled() + ", item use " + click.useItemInHand();
    }

    static boolean isFiringCast() {
        return firingCast;
    }

    private static int reloadTicks(MagicStrike strike) {
        return switch (strike) {
            case STAFF_FIREBALL -> 40;
            case STAFF_MELEE -> 12;
            case WAND_MISSILE -> 11;
        };
    }

    private Hit firstHitOf(int from, MagicStrike strike) {
        for (int index = from; index < hits.size(); index++) {
            Hit hit = hits.get(index);
            if (hit.attackId() != null && hit.strike() == strike) return hit;
        }
        return null;
    }

    /**
     * Puts a loose dummy back on its spawn point, casts at it and records how far the cast pushed
     * it away from the player and the Slowness level it left (0 for none).
     */
    private int knockback(String key, MagicStrike strike, String target) {
        restore(target);
        mark(target);
        int cast = cast(strike, target);
        waitAfterLanding(cast, 12);
        measure(key, () -> movedAway(target));
        measure(key + ".slowed", () -> {
            PotionEffect slowness = body(target).getPotionEffect(PotionEffectType.SLOWNESS);
            return slowness == null ? 0 : slowness.getAmplifier() + 1;
        });
        return cast;
    }

    // ==================== RECORDING ====================

    @EventHandler(priority = EventPriority.NORMAL)
    public void beforeSkills(EliteMobDamagedByPlayerEvent event) {
        if (event.getPlayer() == player) enteringSkills.put(event, event.getDamage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterSkills(EliteMobDamagedByPlayerEvent event) {
        Double entering = enteringSkills.remove(event);
        if (entering == null) return;
        CombatDamageContext.MagicHit magicHit = event.getMagicHit();
        LivingEntity struck = event.getEliteMobEntity().getLivingEntity();
        trace.add(tick + ": " + (struck == null ? "?" : name(struck.getUniqueId()))
                + (magicHit != null ? " " + magicHit.strike() : event.isCustomDamage() ? " side" : " plain")
                + (event.isCancelled() ? " cancelled" : ""));
        if (event.isCancelled()) return;
        // Vanilla swings that FreeMinecraftModels turns into a staff strike are not hits of their own.
        if (magicHit == null && !event.isCustomDamage()) return;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        if (target == null) return;
        hits.add(new Hit(tick, target.getUniqueId(),
                magicHit == null ? null : magicHit.attackId(), magicHit == null ? null : magicHit.strike(),
                entering > 0 ? event.getDamage() / entering : 1, event.getDamage(), event.getDamageWithoutCriticalStrike(), true));
        if (event.getEntityDamageByEntityEvent() != null)
            unsettled.put(event.getEntityDamageByEntityEvent(), hits.size() - 1);
    }

    /** EliteMobs scores a hit midway through the native damage event; this sees whether it still landed. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void settled(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        Integer index = unsettled.remove(event);
        if (index == null || !event.isCancelled()) return;
        hits.set(index, hits.get(index).cancelled());
        trace.add(tick + ": " + name(event.getEntity().getUniqueId()) + " cancelled by a later listener");
    }

    // ==================== CHECKS ====================

    private void check(String expectation, BooleanSupplier holds, Supplier<String> observed) {
        steps.add(() -> {
            boolean held;
            String detail;
            try {
                held = holds.getAsBoolean();
                detail = observed.get();
            } catch (RuntimeException failure) {
                held = false;
                detail = failure.toString();
            }
            String line = (held ? "PASS " : "FAIL ") + expectation + " | " + detail;
            result.addLog(line);
            log.log("  " + line);
            if (!held) result.addIssue(expectation + " (" + detail + ")");
            return true;
        });
    }

    private void issue(String text) {
        result.addLog("FAIL " + text);
        log.log("  FAIL " + text);
        result.addIssue(text);
    }

    private void expectNear(String expectation, DoubleSupplier expected, DoubleSupplier actual, double tolerance) {
        check(expectation, () -> close(actual.getAsDouble(), expected.getAsDouble(), tolerance),
                () -> "expected " + format(expected.getAsDouble()) + ", observed " + format(actual.getAsDouble()));
    }

    private void expectMultiplier(String expectation, int cast, String target, DoubleSupplier expected) {
        check(expectation, () -> closeAbsolute(multiplierOn(cast, target), expected.getAsDouble()),
                () -> "expected ×" + format(expected.getAsDouble()) + ", observed " + describe(cast(cast)));
    }

    private void expectBlast(String expectation, int cast, int minimumTargets, DoubleSupplier expected) {
        check(expectation, () -> cast(cast).hits().size() >= minimumTargets && cast(cast).hits().stream()
                        .allMatch(hit -> closeAbsolute(hit.multiplier(), expected.getAsDouble())),
                () -> "expected ×" + format(expected.getAsDouble()) + " on " + minimumTargets + "+ targets, observed "
                        + describe(cast(cast)));
    }

    private void expectCrowd(String expectation, int cast, DoubleSupplier perEnemy) {
        check(expectation, () -> !cast(cast).hits().isEmpty() && cast(cast).hits().stream().allMatch(hit ->
                        closeAbsolute(hit.multiplier(), 1 + Math.min(4, neighbours(hit.target())) * perEnemy.getAsDouble())),
                () -> "per enemy " + format(perEnemy.getAsDouble()) + ", observed " + cast(cast).hits().stream()
                        .map(hit -> name(hit.target()) + " ×" + format(hit.multiplier()) + " with "
                                + neighbours(hit.target()) + " near").collect(Collectors.joining(", ")));
    }

    private void expectPulses(String expectation, String target, int cast, int count) {
        check(expectation, () -> {
            List<Hit> pulses = sideHits(target, landed(cast));
            if (pulses.size() != count) return false;
            long previous = landed(cast);
            for (Hit pulse : pulses) {
                if (pulse.tick() - previous < 18 || pulse.tick() - previous > 22) return false;
                previous = pulse.tick();
            }
            return true;
        }, () -> "pulse ticks after landing " + ticks(sideHits(target, landed(cast)), landed(cast)));
    }

    private void expectNoSideHits(String expectation, String target, int cast) {
        check(expectation, () -> sideHits(target, landed(cast)).isEmpty(),
                () -> "side hits " + damages(sideHits(target, landed(cast))));
    }

    private void expectSlowness(String expectation, String target, int amplifier) {
        check(expectation, () -> {
            PotionEffect slowness = body(target).getPotionEffect(PotionEffectType.SLOWNESS);
            return slowness != null && slowness.getAmplifier() == amplifier
                    && slowness.getDuration() > 20 && slowness.getDuration() <= 40;
        }, () -> slowness(target));
    }

    private void expectCooldown(String expectation, String skillId, long minimum, long maximum) {
        check(expectation, () -> {
            long remaining = ((CooldownSkill) skill(skillId)).getRemainingCooldown(player);
            return remaining >= minimum && remaining <= maximum;
        }, () -> "remaining " + ((CooldownSkill) skill(skillId)).getRemainingCooldown(player) + "s");
    }

    /** Queues a missile at a dummy and expects Spellweave to carry {@code stacks} on it. */
    private int expectWeave(String expectation, String target, int stacks) {
        int cast = cast(MagicStrike.WAND_MISSILE, target);
        expectMultiplier(expectation, cast, target,
                () -> 1 + stacks * stacking(SpellweaveSkill.SKILL_ID).getBonusPerStack(LEVEL));
        return cast;
    }

    // ==================== OBSERVATIONS ====================

    private Cast cast(int index) {
        if (index >= casts.size()) throw new IllegalStateException("cast " + index + " has not happened");
        return casts.get(index);
    }

    private long landed(int cast) {
        return cast(cast).landedAt();
    }

    private double multiplierOn(int cast, String target) {
        Hit hit = cast(cast).on(id(target));
        return hit == null ? Double.NaN : hit.multiplier();
    }

    private double damageOn(int cast, String target) {
        Hit hit = cast(cast).on(id(target));
        return hit == null ? Double.NaN : hit.damage();
    }

    private double nonCritical(int cast, String target) {
        Hit hit = cast(cast).on(id(target));
        return hit == null ? Double.NaN : hit.nonCritical();
    }

    /** Side hits that actually dealt their damage. */
    private List<Hit> sideHits(String target, long from) {
        UUID id = id(target);
        return hits.stream().filter(hit -> hit.attackId() == null && hit.landed() && hit.target().equals(id)
                && hit.tick() >= from).toList();
    }

    private List<Hit> sideHitsSince(long from) {
        return hits.stream().filter(hit -> hit.attackId() == null && hit.landed() && hit.tick() >= from).toList();
    }

    private double sideDamage(String target, long from) {
        return sideHits(target, from).stream().mapToDouble(Hit::damage).sum();
    }

    private double value(String key) {
        Double value = values.get(key);
        if (value == null) throw new IllegalStateException("nothing measured as " + key);
        return value;
    }

    /** Other dummies within Wildfire's 4 block crowd radius of a target. */
    private int neighbours(UUID target) {
        LivingEntity center = dummies.values().stream().map(Dummy::body)
                .filter(body -> body.getUniqueId().equals(target)).findFirst().orElse(null);
        if (center == null) return 0;
        return (int) dummies.values().stream().map(Dummy::body)
                .filter(body -> body != center && body.isValid()
                        && body.getLocation().distanceSquared(center.getLocation()) <= 16).count();
    }

    private double movedAway(String name) {
        Location from = marks.get(name);
        if (from == null) throw new IllegalStateException(name + " was not marked");
        Vector away = from.toVector().subtract(anchor.toVector()).setY(0).normalize();
        return body(name).getLocation().toVector().subtract(from.toVector()).setY(0).dot(away);
    }

    private List<NamespacedKey> movementModifiers() {
        AttributeInstance movement = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movement == null) return List.of();
        // Potion effects such as the casting slowness add their own modifiers.
        return movement.getModifiers().stream().map(AttributeModifier::getKey)
                .filter(key -> !key.getKey().startsWith("effect.")).toList();
    }

    @SuppressWarnings("unchecked")
    private static <T extends SkillBonus> T skill(String skillId) {
        SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
        if (skill == null) throw new IllegalStateException("skill " + skillId + " is not registered");
        return (T) skill;
    }

    private static StackingSkill stacking(String skillId) {
        return (StackingSkill) SkillBonusRegistry.getSkillById(skillId);
    }

    private boolean enabled(String skillId) {
        return SkillBonusRegistry.getEnabledBonuses(type).stream().anyMatch(skill -> skill.getSkillId().equals(skillId));
    }

    // ==================== FORMATTING ====================

    private static boolean close(double actual, double expected, double relative) {
        if (!Double.isFinite(actual) || !Double.isFinite(expected)) return false;
        return Math.abs(actual - expected) <= Math.max(relative * Math.abs(expected), 1.0E-6);
    }

    private static boolean closeAbsolute(double actual, double expected) {
        return Double.isFinite(actual) && Math.abs(actual - expected) <= MULTIPLIER_TOLERANCE;
    }

    private String name(UUID id) {
        return ids.entrySet().stream().filter(entry -> entry.getValue().equals(id)).map(Map.Entry::getKey)
                .findFirst().orElse("?");
    }

    private String describe(Cast cast) {
        if (cast.attackId() == null) return "no landing";
        return cast.hits().stream().map(hit -> name(hit.target()) + " ×" + format(hit.multiplier()))
                .collect(Collectors.joining(", "));
    }

    private String slowness(String name) {
        PotionEffect effect = body(name).getPotionEffect(PotionEffectType.SLOWNESS);
        return effect == null ? "no slowness" : "slowness " + (effect.getAmplifier() + 1) + " for " + effect.getDuration() + " ticks";
    }

    private String boltSpread(long from) {
        return ids.keySet().stream().filter(dummies::containsKey)
                .map(name -> name + "=" + sideHits(name, from).size()).collect(Collectors.joining(", "));
    }

    private static String damages(List<Hit> hits) {
        return hits.stream().map(hit -> format(hit.damage())).collect(Collectors.joining(", ", "[", "]"));
    }

    private static String ticks(List<Hit> hits, long from) {
        return hits.stream().map(hit -> String.valueOf(hit.tick() - from)).collect(Collectors.joining(", ", "[", "]"));
    }

    private static String blocks(double distance) {
        return "moved " + format(distance) + " blocks away";
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static String strikeName(MagicStrike strike) {
        return switch (strike) {
            case STAFF_FIREBALL -> "fireball";
            case STAFF_MELEE -> "staff strike";
            case WAND_MISSILE -> "wand missile";
        };
    }
}
