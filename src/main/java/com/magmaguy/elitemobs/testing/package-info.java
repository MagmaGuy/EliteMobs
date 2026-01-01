/**
 * Automated batch testing framework for the EliteMobs skill system.
 * <p>
 * <b>How It Works:</b>
 * <ol>
 *   <li>Groups all skills by weapon type (SWORDS, AXES, BOWS, etc.)</li>
 *   <li>For each weapon type, spawns a single training dummy</li>
 *   <li>Tests all levels (10, 20, 30... 100) with all skills active simultaneously</li>
 *   <li>Uses proc counting to verify skills activate correctly based on unlock level</li>
 * </ol>
 * <p>
 * <b>0-Tick Trick:</b>
 * For melee/armor skills, 200 attacks happen in a single tick by:
 * <ul>
 *   <li>Clearing iframes (setNoDamageTicks(0)) before each attack</li>
 *   <li>Resetting skill cooldowns (endCooldown()) before each attack</li>
 * </ul>
 * <p>
 * <b>Key Classes:</b>
 * <ul>
 *   <li>{@link com.magmaguy.elitemobs.testing.SkillSystemTest} - Main test orchestrator</li>
 *   <li>{@link com.magmaguy.elitemobs.testing.CombatSimulator} - Spawns dummies, simulates attacks</li>
 *   <li>{@link com.magmaguy.elitemobs.testing.CombatTestLog} - Logs test results to file</li>
 *   <li>{@link com.magmaguy.elitemobs.testing.TestReport} - Aggregates results for display</li>
 * </ul>
 * <p>
 * <b>Proc Tracking:</b>
 * Skills call {@link com.magmaguy.elitemobs.skills.bonuses.SkillBonus#incrementProcCount} when they activate.
 * The test checks {@link com.magmaguy.elitemobs.skills.bonuses.SkillBonus#getProcCount} to verify activation.
 * <p>
 * Usage: /em skilltest start
 */
package com.magmaguy.elitemobs.testing;
