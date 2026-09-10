return {
  api_version = 1,
  on_threat_bonus = function(context)
    return context.enchantment.level / 3
  end,
}
