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

package network.darkhelmet.prism.bukkit.actions;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Setter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import network.darkhelmet.prism.api.actions.metadata.ReasonMetadata;
import network.darkhelmet.prism.api.actions.metadata.UsingMetadata;
import network.darkhelmet.prism.api.actions.types.ActionType;
import network.darkhelmet.prism.api.services.translation.TranslationService;

public abstract class BukkitAction implements network.darkhelmet.prism.api.actions.Action {
    /**
     * The object mapper.
     */
    public static final ObjectMapper ObjectMapper = new ObjectMapper();

    /**
     * The descriptor.
     */
    @Setter
    protected String descriptor = "";

    /**
     * The type.
     */
    protected final ActionType type;

    /**
     * The metadata.
     */
    protected Record metadata;

    /**
     * Construct a new action.
     *
     * @param type The action type
     */
    public BukkitAction(ActionType type) {
        this(type, null, null);
    }

    /**
     * Construct a new action.
     *
     * @param type The action type
     * @param descriptor The descriptor
     */
    public BukkitAction(ActionType type, String descriptor) {
        this(type, descriptor, null);
    }

    /**
     * Construct a new action.
     *
     * @param type The action type
     * @param descriptor The descriptor
     * @param metadata The metadata
     */
    public BukkitAction(ActionType type, String descriptor, Record metadata) {
        this.type = type;
        this.descriptor = descriptor;
        this.metadata = metadata;
    }

    @Override
    public String descriptor() {
        return descriptor;
    }

    @Override
    public Component descriptorComponent() {
        return Component.text(descriptor);
    }

    @Override
    public Record metadata() {
        return metadata;
    }

    @Override
    public Component metadataComponent(Object receiver, TranslationService translationService) {
        if (metadata instanceof UsingMetadata usingMetadata) {
            String using = translationService.translate(receiver, "text.metadata-hover-using");

            return Component.text()
                .append(Component.text(using + ": ", NamedTextColor.GRAY))
                .append(Component.text(usingMetadata.using(), NamedTextColor.WHITE))
                .build();
        } else if (metadata instanceof ReasonMetadata reasonMetadata) {
            String reason = translationService.translate(receiver, "text.metadata-hover-reason");

            return Component.text()
                .append(Component.text(reason + ": ", NamedTextColor.GRAY))
                .append(Component.text(reasonMetadata.reason(), NamedTextColor.WHITE))
                .build();
        }

        return null;
    }

    @Override
    public String serializeMetadata() throws Exception {
        return ObjectMapper.writeValueAsString(metadata);
    }

    @Override
    public ActionType type() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Action{type=%s,descriptor=%s}", type, descriptor);
    }
}
