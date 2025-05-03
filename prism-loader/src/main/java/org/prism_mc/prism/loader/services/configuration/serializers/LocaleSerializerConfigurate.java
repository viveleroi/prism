/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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

package org.prism_mc.prism.loader.services.configuration.serializers;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public class LocaleSerializerConfigurate implements TypeSerializer<Locale> {
    @Override
    public Locale deserialize(final Type type, final ConfigurationNode node) {
        final @Nullable String value = node.getString();

        if (value == null) {
            return Locale.ENGLISH;
        }

        return Objects.requireNonNull(parseLocale(value), "value locale cannot be null!");
    }

    @Override
    public void serialize(final Type type, final @Nullable Locale obj, final ConfigurationNode node)
        throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            node.set(obj.toString());
        }
    }

    /**
     * Return a Locale from a string.
     *
     * <p>Used under MIT from Kyori: https://tinyurl.com/4yvnh48u</p>
     *
     * @param string The locale string
     * @return The locale
     */
    private static Locale parseLocale(final String string) {
        final String[] segments = string.split("_", 3); // language_country_variant
        final int length = segments.length;
        if (length == 1) {
            return new Locale(string); // language
        } else if (length == 2) {
            return new Locale(segments[0], segments[1]); // language + country
        } else if (length == 3) {
            return new Locale(segments[0], segments[1], segments[2]); // language + country + variant
        }
        return null;
    }
}