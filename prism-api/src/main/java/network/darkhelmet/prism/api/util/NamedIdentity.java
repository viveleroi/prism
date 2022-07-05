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

package network.darkhelmet.prism.api.util;

import java.util.UUID;

public class NamedIdentity {
    /**
     * The name.
     */
    private final String name;

    /**
     * The uuid.
     */
    private final UUID uuid;

    /**
     * Construct a named identity.
     *
     * @param uuid The uuid
     * @param name The name
     */
    public NamedIdentity(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * Get the name.
     *
     * @return The name
     */
    public String name() {
        return name;
    }

    /**
     * Get the uuid.
     *
     * @return The uuid
     */
    public UUID uuid() {
        return uuid;
    }
}
