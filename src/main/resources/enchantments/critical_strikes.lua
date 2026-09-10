return {
  api_version = 1,
  on_critical_chance = function(context)
    return context.enchantment.level / 10
  end,
}
