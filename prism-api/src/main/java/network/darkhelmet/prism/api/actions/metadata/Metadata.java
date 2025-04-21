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

package network.darkhelmet.prism.api.actions.metadata;

import java.util.Locale;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
    private enum MetadataKey {
        USING
    }

    @Getter
    @Singular("entry")
    public Map<String, String> data;

    public static class MetadataBuilder {
        /**
         * Set the using metadata.
         *
         * @param value Using
         * @return The builder
         */
        public MetadataBuilder using(String value) {
            entry(MetadataKey.USING.toString().toLowerCase(Locale.ENGLISH), value);
            return this;
        }
    }
}
