/*
 * Prism (Refracted)
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

package network.darkhelmet.prism.utils;

import lombok.experimental.UtilityClass;

import network.darkhelmet.prism.PrismBukkit;

import org.bukkit.Material;
import org.bukkit.Tag;

@UtilityClass
public class TagLib {
    /**
     * Buckets which produce a water block when emptied outside of water.
     */
    public static final MaterialTag WATER_BUCKETS = new MaterialTag(
        Material.AXOLOTL_BUCKET,
        Material.COD_BUCKET,
        Material.PUFFERFISH_BUCKET,
        Material.SALMON_BUCKET,
        Material.TROPICAL_FISH_BUCKET,
        Material.WATER_BUCKET);

    static {
        if (PrismBukkit.getInstance().serializerVersion() >= 119) {
            WATER_BUCKETS.append(Material.TADPOLE_BUCKET);
        }
    }

    /**
     * Some blocks are always "waterlogged" and not actually subclasses of Waterlogged.
     */
    public static final MaterialTag WATER_BLOCKS = new MaterialTag(
        Material.WATER,
        Material.BUBBLE_COLUMN,
        Material.KELP,
        Material.KELP_PLANT,
        Material.SEAGRASS,
        Material.TALL_SEAGRASS,
        Material.TUBE_CORAL,
        Material.BRAIN_CORAL,
        Material.BUBBLE_CORAL,
        Material.FIRE_CORAL,
        Material.CONDUIT
    );

    /**
     * Buckets which produce a lava block when emptied outside of lava.
     */
    public static final MaterialTag LAVA_BUCKETS = new MaterialTag(Material.LAVA_BUCKET);

    /**
     * All plants that have a one-block structure.
     */
    public static final MaterialTag PLANTS = new MaterialTag(
        Material.FERN,
        Material.DEAD_BUSH,
        Material.DANDELION,
        Material.POPPY,
        Material.BLUE_ORCHID,
        Material.ALLIUM,
        Material.AZURE_BLUET,
        Material.RED_TULIP,
        Material.ORANGE_TULIP,
        Material.WHITE_TULIP,
        Material.PINK_TULIP,
        Material.OXEYE_DAISY,
        Material.BROWN_MUSHROOM,
        Material.RED_MUSHROOM,
        Material.LILY_PAD,
        Material.KELP,
        Material.KELP_PLANT,
        Material.SHORT_GRASS,
        Material.SEAGRASS,
        Material.SWEET_BERRY_BUSH,
        Material.TALL_GRASS,
        Material.TALL_SEAGRASS)
        .append(Tag.WALL_CORALS)
        .append(Tag.CORALS);

    /**
     * Plants that have a two-block structure.
     */
    public static final MaterialTag TALL_PLANTS = new MaterialTag(
        Material.SUNFLOWER,
        Material.LILAC,
        Material.ROSE_BUSH,
        Material.PEONY,
        Material.TALL_GRASS,
        Material.LARGE_FERN,
        Material.TALL_SEAGRASS);

    /**
     * All plants (not counting crops).
     */
    public static final MaterialTag ALL_PLANTS = new MaterialTag(PLANTS).append(TALL_PLANTS);

    /**
     * All vegetation that can be grown.
     */
    public static final MaterialTag GROWABLES = new MaterialTag(
        Material.BAMBOO,
        Material.CACTUS,
        Material.KELP,
        Material.KELP_PLANT,
        Material.SUGAR_CANE,
        Material.CHORUS_PLANT,
        Material.CHORUS_FLOWER
    ).append(Tag.CROPS);

    /**
     * All banners that are placed on the top of a block.
     */
    public static final MaterialTag TOP_BANNERS = new MaterialTag(Tag.BANNERS)
        .exclude("_WALL_", MaterialTag.MatchMode.CONTAINS);

    /**
     * Banners hung on a wall.
     */
    public static final MaterialTag WALL_BANNERS = new MaterialTag(Tag.BANNERS).exclude(TOP_BANNERS);

    /**
     * Materials that will fall due to physics if the supporting block is broken.
     */
    public static final MaterialTag GRAVITY_AFFECTED = new MaterialTag(
        Material.GRAVEL,
        Material.ANVIL,
        Material.DRAGON_EGG
    ).append(Tag.SAND).append("_CONCRETE_POWDER", MaterialTag.MatchMode.SUFFIX);

    /**
     * Materials that attach to any side of a block.
     */
    public static final MaterialTag DETACHABLES = new MaterialTag(
        Material.AMETHYST_CLUSTER,
        Material.CHORUS_PLANT);

    /**
     * Materials that attach to any side of a block, recursively.
     */
    public static final MaterialTag RECURSIVE_DETACHABLES = new MaterialTag(
        Material.CHORUS_PLANT,
        Material.END_PORTAL,
        Material.NETHER_PORTAL);

    /**
     * Materials that can have recursive attachments.
     */
    public static final MaterialTag RECURSIVE_DETACHABLE_HOLDERS = new MaterialTag(
        Material.END_PORTAL_FRAME,
        Material.CHORUS_FLOWER,
        Material.CHORUS_PLANT,
        Material.OBSIDIAN);

    /**
     * All redstone-related items that detach when connected block is broken.
     */
    public static final MaterialTag REDSTONE_DETACHABLE = new MaterialTag(
        Material.COMPARATOR,
        Material.LEVER,
        Material.REPEATER,
        Material.REDSTONE_TORCH,
        Material.REDSTONE_WALL_TORCH,
        Material.REDSTONE_WIRE
    ).append(Tag.BUTTONS, Tag.PRESSURE_PLATES);

    /**
     * All materials that can attach to themselves on the bottom (breaks travel downward).
     */
    public static final MaterialTag RECURSIVE_BOTTOM_DETACHABLES = new MaterialTag(
        Material.CHAIN,
        Material.POINTED_DRIPSTONE,
        Material.VINE,
        Material.WEEPING_VINES,
        Material.WEEPING_VINES_PLANT
    ).append(Tag.CAVE_VINES).append(RECURSIVE_DETACHABLES);

    /**
     * Materials that attach to the bottom of a block.
     */
    public static final MaterialTag BOTTOM_DETACHABLES = new MaterialTag(
        Material.SPORE_BLOSSOM,
        Material.LANTERN,
        Material.SOUL_LANTERN
    ).append(DETACHABLES, RECURSIVE_BOTTOM_DETACHABLES);

    /**
     * All materials that can detach from the side of a block.
     */
    public static final MaterialTag SIDE_DETACHABLES = new MaterialTag(
        // Pistons
        Material.STICKY_PISTON,
        Material.PISTON,
        Material.PISTON_HEAD,
        Material.MOVING_PISTON,

        // Torches
        Material.WALL_TORCH,
        Material.REDSTONE_WALL_TORCH,
        Material.SOUL_WALL_TORCH,

        // Hanging
        Material.ITEM_FRAME,
        Material.PAINTING,

        // Misc
        Material.COCOA,
        Material.GLOW_LICHEN,
        Material.LEVER,
        Material.SCAFFOLDING,
        Material.TRIPWIRE_HOOK)
        .append(Tag.BUTTONS, Tag.WALL_SIGNS, Tag.CLIMBABLE)
        .append(WALL_BANNERS, DETACHABLES);

    /**
     * All materials that can attach to themselves on the top (breaks travel upward).
     */
    public static final MaterialTag RECURSIVE_TOP_DETACHABLES = new MaterialTag(
        Material.BAMBOO,
        Material.KELP_PLANT,
        Material.KELP,
        Material.CACTUS,
        Material.SCAFFOLDING,
        Material.SUGAR_CANE,
        Material.TWISTING_VINES,
        Material.TWISTING_VINES_PLANT
    ).append(Tag.CAVE_VINES).append(RECURSIVE_DETACHABLES);

    /**
     * All materials that can detach from the top of a block.
     */
    public static final MaterialTag TOP_DETACHABLES = new MaterialTag(
        Material.STICKY_PISTON,
        Material.DEAD_BUSH,
        Material.PISTON,
        Material.PISTON_HEAD,
        Material.MOVING_PISTON,
        Material.TORCH,
        Material.SOUL_TORCH,
        Material.LEVER,
        Material.SNOW,
        Material.LILY_PAD,
        Material.NETHER_WART,
        Material.BEACON,
        Material.ITEM_FRAME,
        Material.LANTERN,
        Material.CHAIN,
        Material.CONDUIT,
        Material.BELL)
        .append(
            Tag.DOORS,
            Tag.RAILS,
            Tag.SAPLINGS,
            Tag.STANDING_SIGNS)
        .append(
            Tag.WOOL_CARPETS,
            Tag.FLOWER_POTS)
        .append(REDSTONE_DETACHABLE, GROWABLES, ALL_PLANTS, DETACHABLES, TOP_BANNERS, RECURSIVE_TOP_DETACHABLES);

    /**
     * All wall-placeable skulls.
     */
    public static final MaterialTag WALL_SKULLS = new MaterialTag(
        Material.SKELETON_WALL_SKULL,
        Material.WITHER_SKELETON_WALL_SKULL,
        Material.CREEPER_WALL_HEAD,
        Material.DRAGON_WALL_HEAD,
        Material.PLAYER_WALL_HEAD,
        Material.ZOMBIE_WALL_HEAD);

    /**
     * All floor-placeable skulls.
     */
    public static final MaterialTag FLOOR_SKULLS = new MaterialTag(
        Material.SKELETON_SKULL,
        Material.WITHER_SKELETON_SKULL,
        Material.CREEPER_HEAD,
        Material.DRAGON_HEAD,
        Material.PLAYER_HEAD,
        Material.ZOMBIE_HEAD);

    /**
     * All skulls.
     */
    public static final MaterialTag ALL_SKULLS = new MaterialTag(WALL_SKULLS).append(FLOOR_SKULLS);

    /**
     * All materials that can be broken by fluid flowing.
     */
    public static final MaterialTag FLUID_BREAKABLE = new MaterialTag(TOP_DETACHABLES)
        .append(Material.END_ROD)
        .append(Material.TRIPWIRE)
        .append(ALL_SKULLS);

    /**
     * All materials that can be "used" - opened but doesn't store inventory or performs some action.
     */
    public static final MaterialTag USABLE = new MaterialTag(
        Material.ANVIL,
        Material.BELL,
        Material.CARTOGRAPHY_TABLE,
        Material.COMPARATOR,
        Material.CRAFTING_TABLE,
        Material.DRAGON_EGG,
        Material.ENCHANTING_TABLE,
        Material.FLETCHING_TABLE,
        Material.GRINDSTONE,
        Material.LEVER,
        Material.LOOM,
        Material.NOTE_BLOCK,
        Material.REPEATER,
        Material.SMITHING_TABLE,
        Material.STONECUTTER
    ).append(Tag.BUTTONS)
        .append(Tag.DOORS)
        .append(Tag.TRAPDOORS)
        .append(Tag.FENCE_GATES)
        .append(Tag.PRESSURE_PLATES);
}
