/*
 * prism
 *
 * Copyright (c) 2022 M Botsko (viveleroi)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package network.darkhelmet.prism.loader.services.configuration;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class CommandsConfiguration {
    @Comment("""
            Allow Minecraft tags in tag parameters. The list is long and often
            includes tags unlikely to be useful. We instead recommend using
            the tag whitelist option to reduce the list to something useful.
            Note: disabling this overrides the tag whitelist.
            """)
    private boolean allowMinecraftTags = true;

    @Comment("""
            Enable the block tag whitelist. If disabled, values will be ignored.
            """)
    private boolean blockTagWhitelistEnabled = false;

    @Comment("""
            Define block tags allowed in the btag parameter (and auto-suggestions).
            These must include the tag namespace, e.g. `minecraft:dirt`, but can
            include any tag - even custom ones.
            https://minecraft.wiki/w/Block_tag_(Java_Edition)
            Note: All Minecraft tags will be excluded if `allowMinecraftTags` is false.
            """)
    private List<String> blockTagWhitelist = new ArrayList<>();

    @Comment("""
            Enable the entity type tag whitelist. If disabled, values will be ignored.
            """)
    private boolean entityTypeTagWhitelistEnabled = false;

    @Comment("""
            Define entity type tags allowed in the btag parameter (and auto-suggestions).
            These must include the tag namespace, e.g. `minecraft:aquatic`, but can
            include any tag - even custom ones.
            https://minecraft.wiki/w/Entity_type_tag_(Java_Edition)
            Note: All Minecraft tags will be excluded if `allowMinecraftTags` is false.
            """)
    private List<String> entityTypeTagWhitelist = new ArrayList<>();

    @Comment("""
            Enable the item tag whitelist. If disabled, values will be ignored.
            """)
    private boolean itemTagWhitelistEnabled = false;

    @Comment("""
            Define item tags allowed in the btag parameter (and auto-suggestions).
            These must include the tag namespace, e.g. `minecraft:dirt`, but can
            include any tag - even custom ones.
            https://minecraft.wiki/w/Item_tag_(Java_Edition)
            Note: All Minecraft tags will be excluded if `allowMinecraftTags` is false.
            """)
    private List<String> itemTagWhitelist = new ArrayList<>();

    /**
     * Constructor.
     */
    public CommandsConfiguration() {
        blockTagWhitelist.add("minecraft:coal_ores");
        blockTagWhitelist.add("minecraft:copper_ores");
        blockTagWhitelist.add("minecraft:crops");
        blockTagWhitelist.add("minecraft:diamond_ores");
        blockTagWhitelist.add("minecraft:emerald_ores");
        blockTagWhitelist.add("minecraft:gold_ores");
        blockTagWhitelist.add("minecraft:iron_ores");
        blockTagWhitelist.add("minecraft:lapis_ores");
        blockTagWhitelist.add("minecraft:logs_that_burn");
        blockTagWhitelist.add("minecraft:beacon_base_blocks");
        blockTagWhitelist.add("minecraft:mineable/axe");
        blockTagWhitelist.add("minecraft:mineable/hoe");
        blockTagWhitelist.add("minecraft:mineable/pickaxe");
        blockTagWhitelist.add("minecraft:mineable/shovel");
        blockTagWhitelist.add("minecraft:redstone_ores");
        blockTagWhitelist.add("prism:all_ores");

        itemTagWhitelist.add("minecraft:axes");
        itemTagWhitelist.add("minecraft:beacon_payment_item");
        itemTagWhitelist.add("minecraft:bundles");
        itemTagWhitelist.add("minecraft:chest_armor");
        itemTagWhitelist.add("minecraft:coal_ores");
        itemTagWhitelist.add("minecraft:copper_ores");
        itemTagWhitelist.add("minecraft:diamond_ores");
        itemTagWhitelist.add("minecraft:emerald_ores");
        itemTagWhitelist.add("minecraft:foot_armor");
        itemTagWhitelist.add("minecraft:gold_ores");
        itemTagWhitelist.add("minecraft:head_armor");
        itemTagWhitelist.add("minecraft:hoes");
        itemTagWhitelist.add("minecraft:iron_ores");
        itemTagWhitelist.add("minecraft:lapis_ores");
        itemTagWhitelist.add("minecraft:leg_armor");
        itemTagWhitelist.add("minecraft:pickaxes");
        itemTagWhitelist.add("minecraft:redstone_ores");
        itemTagWhitelist.add("minecraft:shovels");
        itemTagWhitelist.add("prism:all_armor");
        itemTagWhitelist.add("prism:all_ores");
    }
}
