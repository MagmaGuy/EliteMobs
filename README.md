# EliteMobs

This is the GitHub project for the Minecraft plugin EliteMobs (Spigot page can be seen here: https://www.spigotmc.org/resources/elitemobs.40090/)

Feel free to check the source code and do any edits you might deem necessary for your own servers.

At this time, I am not interested in accepting contributions to the code. This is my first project of this dimension, and I'd like to have all components of it be written by me.

That being said, I am deeply interested in any suggestions, be they for improving existing features or creating new ones.

# API / developing around this

There is currently no real API, but you can easily use metadata to hook into the plugin, as almost every single feature of mobs is handled through metadata.

Currently, the following metadata can easily be detected or messed with to achieve basic results, such as spawning a custom Elite Mob through a thrid party or detecting a mob dying.

["EliteMob", int] - refers to an aggressive elite mob and its level

["PassiveEliteMobs", boolean] - refers to a passive elite mob (always true in plugin)

["NaturalMob", boolean] - applied to mobs on spawn, always true as it is simply not applied to mobs that aren't valid.

["CustomName", boolean] - prevents plugin from changing the mob's name. Always true or removed.

["CustomArmor", boolean] - prevents plugin from changing the mob's armor. Always true or removed.

["CustomHealth", boolean] - prevents plugin from changing the mob health. Always true or removed.

["Forbidden", boolean] - prevents the plug from stacking the mob. Always true or removed.

["Custom", boolean] - prevents mob from getting any additional powers. Always true or removed.

# //TODO: add description of features and how they work, some day, maybe