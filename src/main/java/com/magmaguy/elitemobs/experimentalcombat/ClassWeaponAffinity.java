package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Encourages playing the active class's weapons: class weapons hit elites 10% harder and
 * off-class weapons 10% softer, with a rate-limited action-bar reminder. Vanilla-mob combat is
 * deliberately untouched because EliteMobs separates elite combat from vanilla balance.
 */
public final class ClassWeaponAffinity implements Listener {

    private static final double CLASS_WEAPON_MULTIPLIER = 1.10D;
    private static final double OFF_CLASS_WEAPON_MULTIPLIER = .90D;
    private static final long WARNING_INTERVAL_MILLIS = 5L * 60L * 1_000L;

    private final Map<UUID, Long> lastWarnings = new HashMap<>();
    private final Set<UUID> chatWarnedThisSession = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEliteDamagedByPlayer(EliteMobDamagedByPlayerEvent event) {
        Player player = event.getPlayer();
        if (!ExperimentalCombatModule.isInitialized()) return;
        ExperimentalCombatModule module = ExperimentalCombatModule.get();
        if (!module.mechanicsActive(player) || !module.hasActiveClass(player)) return;
        SkillType weaponSkill = WeaponIdentityResolver.progressionSkill(
                player.getInventory().getItemInMainHand());
        if (weaponSkill == null) return; // bare hands and non-weapons stay neutral
        boolean classWeapon = module.activeClassSkills(player).contains(weaponSkill);
        event.setDamage(event.getDamage()
                * (classWeapon ? CLASS_WEAPON_MULTIPLIER : OFF_CLASS_WEAPON_MULTIPLIER));
        if (!classWeapon) warnOffClassWeapon(player);
    }

    private void warnOffClassWeapon(Player player) {
        long now = System.currentTimeMillis();
        Long previous = lastWarnings.get(player.getUniqueId());
        if (previous != null && now - previous < WARNING_INTERVAL_MILLIS) return;
        lastWarnings.put(player.getUniqueId(), now);
        String header = ClassPresentationTheme.gradient(ClassPresentationTheme.RED, "Off-class weapon");
        ActionBarCompositor.show(
                player,
                ActionBarCompositor.Source.AFFINITY_WARNING,
                ChatColorConverter.convert(
                        header + " &8» &c-10% damage&7. Your class weapons hit &a+10%&7."));
        if (chatWarnedThisSession.add(player.getUniqueId()))
            player.sendMessage(ChatColorConverter.convert(
                    header + " &8» &7This weapon does not match your class: &c-10% damage&7."
                            + " Class weapons hit &a+10%&7."));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        lastWarnings.remove(event.getPlayer().getUniqueId());
        chatWarnedThisSession.remove(event.getPlayer().getUniqueId());
    }

    void shutdown() {
        lastWarnings.clear();
        chatWarnedThisSession.clear();
    }
}
