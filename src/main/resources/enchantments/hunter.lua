return {
  api_version = 1,
  on_natural_spawn_bonus = function(context)
    return context.enchantment.level * context.parameters.spawn_bonus
  end,
}
