package com.decursioteam.pickable_orbs.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Readme {
    public static final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    public static final ModConfigSpec config;
    public static ModConfigSpec.BooleanValue generate_defaults;
    static {
        builder.push("Readme file - Pickable Orbs");
        generate_defaults = builder.comment("# **Pickable Orbs** - Starter Guide" +
                        "\nHere you'll see everything you need to create a custom orb/modify the defaults ones or regenerate the default ones." +


                        "\n## Locating the orbs folder" +
                        "\nDepending on the minecraft instances manager you're using you should be able to get to the mods config folder pretty easy." +
                        "\nThe easiest method that is for sure going to work on any of these launchers is opening the game going to the Settings menu and " +
                        "\nopening the Resource Packs tab and clicking on the **Open Resource Pack folder** button which is going to open you a file manager " +
                        "\ninstance located in the almost exact spot that you need to be so you can begin everything, " +
                        "\nso to do that go back one folder in the directories tree and after that you should see a **config** folder, click on that one and " +
                        "\nthen you should see a **com.decursioteam.pickable_orbs** folder, open that and that's it!" +

                        "\n## Modifying already existing orbs" +
                        "\nThis is just a matter of making sure that the generation of the default orbs is set to false in the **common.toml** file, after " +
                        "\nthat go to the **orbs** folder where you're going to see each orb in it's glory, open one of those files to start editing whatever orb you like." +
                        "\n## Creating custom orbs" +
                        "\nThis one is easy as well, just create a new **.json** file and in the next step you'll see every parameter that you can enter in there and what it does." +

                        "\n## Orbs Data" +
                        "\nSo in the end, the structure of an orb should look something of the sort:" +

                        "\n{" +
                        "\n  \"OrbData\": {" +
                        "\n    \"type\": \"healing\"," +
                        "\n    \"effectMultiplier\": 2," +
                        "\n    \"color\": \"#FF0046\"," +
                        "\n    \"blockList\": [\"minecraft:spawner\"]," +
                        "\n    \"blockListType\": \"whitelist\"," +
                        "\n    \"blockDropChance\": 30.0," +
                        "\n    \"entityList\": [\"minecraft:pig\", \"minecraft:sheep\"]," +
                        "\n    \"entityListType\": \"blacklist\"," +
                        "\n    \"entityDropChance\": 15.5" +
                        "\n  }," +
                        "\n  \"ExtraData\": {" +
                        "\n    \"pickup-message\": \"You've picked up a health orb!\"," +
                        "\n    \"animation\": false," +
                        "\n    \"sound\": true," +
                        "\n    \"follow-player\": false," +
                        "\n    \"pickup-delay\": 10" +
                        "\n  }" +
                        "\n}" +

                        "\n## Orbs Data - what do they mean????" +
                        "\nWell let's start with the **OrbData** field first, there we can have:" +
                        "\n>     \"name\": \"<name>\" -> The default name that an orb receives is the name of the file, but you can change that by adding this parameter" +
                        "\n>     \"type\": \"<type>\" -> At the moment there are 8 types of orbs, **healing**, **poisonous**, **damaging**, **jumping**, **speedster**, " +
                        "\n**confusion**, **levitation** and **fire_resistance**." +
                        "\n>     \"effectMultiplier\": \"<number value>\", -> Sets the multiplier of the effect" +
                        "\n>     \"effectDuration\": \"<number value>\", -> Sets the duration of the effect, unavailable for the **healing** and **damaging** types." +
                        "\n>     \"color\": \"<color hex code>\", -> Sets the color that should be applied over the plain texture of an orb, just use any color picker " +
                        "you can find on google that also offers you an hex code." +
                        "\n>     \"blockList\": [<list of block tags and id's>], -> Sets the blocks that should/shouldn't have a chance of spawning this Orb when " +
                        "they're broken, ex: \"blockList\": [\"forge:storage_blocks/netherite\", \"minecraft:dirt\"]" +
                        "\n>     \"blockListType\": \"<whitelist/blacklist>\", -> Sets the type of the **blockList** and **blockNames** lists." +
                        "\n>     \"blockDropChance\": <number ranging from 0.0 to 100.0>, -> Sets the chance of the orb to spawn then one of the blocks in the block lists are broken." +
                        "\n>     \"entityList\": [<list of entity id's>], -> Sets the entities that should/shouldn't have a chance of spawning this Orb when " +
                        "they're killed, ex: \"entityList\": [\"minecraft:skeleton\", \"minecraft:chicken\"]" +
                        "\n>     \"entityListType\": \"<whitelist/blacklist>\", -> Sets the type of the **entityList** and **entityNames** lists." +
                        "\n>     \"entityDropChance\": <number ranging from 0.0 to 100.0>, -> Sets the chance of the orb to spawn then one of the entities in the entity lists are killed." +

                        "\nOk that's all for that field, let's do the **ExtraData** field now:" +

                        "\n>     \"pickup-message\": \"<string message>\", -> Sets the message that the player should receive when he pickups up this orb." +
                        "\n>     \"animation\": <true/false>, -> Toggles the animation for this orb." +
                        "\n>     \"sound\": <true/false>, -> Toggles the sound for this orb." +
                        "\n>     \"follow-player\": <true/false>, -> Toggles the follow player function (exactly how an Experience Orb does in vanilla) for this orb." +
                        "\n>     \"pickup-delay\": <time in ticks (20 ticks = 1 second)> -> Sets the pickup delay(the amount of time that has to pass after the orb " +
                        "was spawned so that a player can pick it up) of this orb." +
                        "\n### I know it seems like a lot but really it isn't that much, especially after you've read it one time and figured what everything means, " +
                        "\nif you're still having issues/you didn't understand something contact me on the [Decursio Project Discord](https://discord.com/invite/EWuqDrPF49).")
                .define("agree", true);
        builder.pop();
        config = builder.build();
    }
}
