package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusesConfig;
import com.magmaguy.elitemobs.config.skillbonuses.premade.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.axes.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.*;
import com.magmaguy.magmacore.util.Logger;

/**
 * Initializes and registers all skill bonuses.
 * Called during plugin startup after configuration is loaded.
 */
public class SkillBonusInitializer {

    private SkillBonusInitializer() {
        // Static utility class
    }

    /**
     * Initializes all skill bonuses.
     * Should be called after SkillBonusesConfig has loaded.
     */
    public static void initialize() {
        Logger.info("Initializing skill bonuses...");

        // Register SWORDS skills
        registerSwordsSkills();

        // Register AXES skills
        registerAxesSkills();

        // Register BOWS skills
        registerBowsSkills();

        // Register CROSSBOWS skills
        registerCrossbowsSkills();

        // Register TRIDENTS skills
        registerTridentsSkills();

        // Register HOES skills
        registerHoesSkills();

        // Register ARMOR skills
        registerArmorSkills();

        // Register MACES skills
        registerMacesSkills();

        // Register SPEARS skills
        registerSpearsSkills();

        Logger.info("Registered " + SkillBonusRegistry.getAllBonuses().size() + " skill bonuses.");
    }

    /**
     * Registers all SWORDS skill bonuses.
     */
    private static void registerSwordsSkills() {
        // Tier 1 (Level 10)
        registerSkill(new LacerateSkill(), new SwordsLacerateConfig());
        registerSkill(new PoiseSkill(), new SwordsPoiseConfig());
        registerSkill(new SwiftStrikesSkill(), new SwordsSwiftStrikesConfig());

        // Tier 2 (Level 25)
        registerSkill(new FlurrySkill(), new SwordsFlurryConfig());
        registerSkill(new RiposteSkill(), new SwordsRiposteConfig());
        registerSkill(new ExposeWeaknessSkill(), new SwordsExposeWeaknessConfig());

        // Tier 3 (Level 50)
        registerSkill(new DuelistSkill(), new SwordsDuelistConfig());
        registerSkill(new FinishingFlourishSkill(), new SwordsFinishingFlourishConfig());
        registerSkill(new ParrySkill(), new SwordsParryConfig());

        // Tier 4 (Level 75)
        registerSkill(new VorpalStrikeSkill(), new SwordsVorpalStrikeConfig());
    }

    /**
     * Registers all AXES skill bonuses.
     */
    private static void registerAxesSkills() {
        // Tier 1 (Level 10)
        registerSkill(new DevastatingBlowSkill(), new AxesDevastatingBlowConfig());
        registerSkill(new WoundSkill(), new AxesWoundConfig());

        // Tier 2 (Level 25)
        registerSkill(new ExecutionerSkill(), new AxesExecutionerConfig());
        registerSkill(new StaggerSkill(), new AxesStaggerConfig());

        // Tier 3 (Level 50)
        registerSkill(new MomentumSkill(), new AxesMomentumConfig());

        // Tier 4 (Level 75)
        registerSkill(new TimberSkill(), new AxesTimberConfig());
    }

    /**
     * Registers all BOWS skill bonuses.
     */
    private static void registerBowsSkills() {
        // Tier 1 (Level 10)
        registerSkill(new FrostbiteSkill(), new BowsFrostbiteConfig());
        registerSkill(new PackHunterSkill(), new BowsPackHunterConfig());

        // Tier 2 (Level 25)
        registerSkill(new MultishotSkill(), new BowsMultishotConfig());
        registerSkill(new OverdrawSkill(), new BowsOverdrawConfig());
        registerSkill(new HuntersMarkSkill(), new BowsHuntersMarkConfig());

        // Tier 3 (Level 50)
        registerSkill(new RicochetSkill(), new BowsRicochetConfig());
        registerSkill(new WindRunnerSkill(), new BowsWindRunnerConfig());
        registerSkill(new BarrageSkill(), new BowsBarrageConfig());

        // Tier 4 (Level 75)
        registerSkill(new RangersFocusSkill(), new BowsRangersFocusConfig());
        registerSkill(new DeadEyeSkill(), new BowsDeadEyeConfig());
    }

    /**
     * Registers all CROSSBOWS skill bonuses.
     */
    private static void registerCrossbowsSkills() {
        // Tier 1 (Level 10)
        registerSkill(new SteadyAimSkill(), new CrossbowsSteadyAimConfig());
        registerSkill(new QuickReloadSkill(), new CrossbowsQuickReloadConfig());

        // Tier 2 (Level 25)
        registerSkill(new ExplosiveTipSkill(), new CrossbowsExplosiveTipConfig());
        registerSkill(new HuntersPreySkill(), new CrossbowsHuntersPreyConfig());

        // Tier 3 (Level 50)
        registerSkill(new HeavyBoltsSkill(), new CrossbowsHeavyBoltsConfig());
        registerSkill(new SuppressingFireSkill(), new CrossbowsSuppressingFireConfig());

        // Tier 4 (Level 75)
        registerSkill(new ArrowRainSkill(), new CrossbowsArrowRainConfig());
        registerSkill(new FirstBloodSkill(), new CrossbowsFirstBloodConfig());
    }

    /**
     * Registers all TRIDENTS skill bonuses.
     */
    private static void registerTridentsSkills() {
        // Tier 1 (Level 10)
        registerSkill(new ImpaleSkill(), new TridentsImpaleConfig());
        registerSkill(new SnareSkill(), new TridentsSnareConfig());

        // Tier 2 (Level 25)
        registerSkill(new TidalSurgeSkill(), new TridentsTidalSurgeConfig());
        registerSkill(new StormCallerSkill(), new TridentsStormCallerConfig());
        registerSkill(new PoseidonsFavorSkill(), new TridentsPoseidonsFavorConfig());

        // Tier 3 (Level 50)
        registerSkill(new RiptideMasterySkill(), new TridentsRiptideMasteryConfig());
        registerSkill(new ReturningHasteSkill(), new TridentsReturningHasteConfig());
        registerSkill(new UndertowSkill(), new TridentsUndertowConfig());

        // Tier 4 (Level 75)
        registerSkill(new DepthChargeSkill(), new TridentsDepthChargeConfig());
        registerSkill(new LeviathanWrathSkill(), new TridentsLeviathanWrathConfig());
    }

    /**
     * Registers all HOES skill bonuses.
     */
    private static void registerHoesSkills() {
        // Tier 1 (Level 10)
        registerSkill(new SoulDrainSkill(), new HoesSoulDrainConfig());
        registerSkill(new ReapersHarvestSkill(), new HoesReapersHarvestConfig());
        registerSkill(new GrimReachSkill(), new HoesGrimReachConfig());

        // Tier 2 (Level 25)
        registerSkill(new SoulSiphonSkill(), new HoesSoulSiphonConfig());
        registerSkill(new DeathMarkSkill(), new HoesDeathMarkConfig());
        registerSkill(new HarvesterSkill(), new HoesHarvesterConfig());

        // Tier 3 (Level 50)
        registerSkill(new ScytheSweepSkill(), new HoesScytheSweepConfig());
        registerSkill(new ReapWhatYouSowSkill(), new HoesReapWhatYouSowConfig());

        // Tier 4 (Level 75)
        registerSkill(new SpectralScytheSkill(), new HoesSpectralScytheConfig());
        registerSkill(new DeathsEmbraceSkill(), new HoesDeathsEmbraceConfig());
    }

    /**
     * Registers all ARMOR skill bonuses.
     */
    private static void registerArmorSkills() {
        // Tier 1 (Level 10)
        registerSkill(new EvasionSkill(), new ArmorEvasionConfig());
        registerSkill(new IronStanceSkill(), new ArmorIronStanceConfig());
        registerSkill(new BattleHardenedSkill(), new ArmorBattleHardenedConfig());

        // Tier 2 (Level 25)
        registerSkill(new RetaliationSkill(), new ArmorRetaliationConfig());
        registerSkill(new AdrenalineSurgeSkill(), new ArmorAdrenalineSurgeConfig());

        // Tier 3 (Level 50)
        registerSkill(new FortifySkill(), new ArmorFortifyConfig());
        registerSkill(new SecondWindSkill(), new ArmorSecondWindConfig());
        registerSkill(new GritSkill(), new ArmorGritConfig());

        // Tier 4 (Level 75)
        registerSkill(new LastStandSkill(), new ArmorLastStandConfig());
        registerSkill(new ReactiveShieldingSkill(), new ArmorReactiveShieldingConfig());
    }

    /**
     * Registers all MACES skill bonuses.
     */
    private static void registerMacesSkills() {
        // Tier 1 (Level 10)
        registerSkill(new ConcussionSkill(), new MacesConcussionConfig());
        registerSkill(new CrushingBlowSkill(), new MacesCrushingBlowConfig());
        registerSkill(new RighteousFurySkill(), new MacesRighteousFuryConfig());

        // Tier 2 (Level 25)
        registerSkill(new JudgmentSkill(), new MacesJudgmentConfig());
        registerSkill(new ShatterSkill(), new MacesShatterConfig());
        registerSkill(new DivineShieldSkill(), new MacesDivineShieldConfig());

        // Tier 3 (Level 50)
        registerSkill(new ConsecrationSkill(), new MacesConsecrationConfig());
        registerSkill(new StunningForceSkill(), new MacesStunningForceConfig());

        // Tier 4 (Level 75)
        registerSkill(new HammerOfWrathSkill(), new MacesHammerOfWrathConfig());
        registerSkill(new AvatarOfJudgmentSkill(), new MacesAvatarOfJudgmentConfig());
    }

    /**
     * Registers all SPEARS skill bonuses.
     */
    private static void registerSpearsSkills() {
        // Tier 1 (Level 10)
        registerSkill(new FirstStrikeSkill(), new SpearsFirstStrikeConfig());
        registerSkill(new LongReachSkill(), new SpearsLongReachConfig());
        registerSkill(new PrecisionThrustSkill(), new SpearsPrecisionThrustConfig());

        // Tier 2 (Level 25)
        registerSkill(new PhalanxSkill(), new SpearsPhalanxConfig());
        registerSkill(new ImpalingStrikeSkill(), new SpearsImpalingStrikeConfig());
        registerSkill(new VortexThrustSkill(), new SpearsVortexThrustConfig());

        // Tier 3 (Level 50)
        registerSkill(new LegionsDisciplineSkill(), new SpearsLegionsDisciplineConfig());
        registerSkill(new VanguardSkill(), new SpearsVanguardConfig());

        // Tier 4 (Level 75)
        registerSkill(new PolearmMasterySkill(), new SpearsPolearmMasteryConfig());
        registerSkill(new ImpalerSkill(), new SpearsImpalerConfig());
    }

    /**
     * Helper method to register a skill and link it to its config.
     */
    private static void registerSkill(SkillBonus skill, SkillBonusConfigFields config) {
        if (config != null) {
            skill.setConfigFields(config);
        }
        SkillBonusRegistry.registerBonus(skill);
    }

    /**
     * Shuts down all skill bonuses.
     * Called during plugin disable.
     */
    public static void shutdown() {
        SkillBonusRegistry.shutdown();
        SkillBonusesConfig.shutdown();
        PlayerSkillSelection.shutdown();
        SkillBonusEventHandler.shutdown();
    }
}
