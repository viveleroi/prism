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

package org.prism_mc.prism.api.services.filters;

public enum FilterMode {
    /**
     * All IGNORE filters are evaluated first, then ALLOW filters. If any ALLOW filter exists,
     * the default decision is to deny. IGNORE always takes precedence over ALLOW.
     */
    GROUPED,

    /**
     * Filters are evaluated in the order they are defined. The first filter whose conditions
     * match decides the outcome (ALLOW records, IGNORE drops). If none match, the activity is
     * recorded.
     */
    ORDERED,
}
