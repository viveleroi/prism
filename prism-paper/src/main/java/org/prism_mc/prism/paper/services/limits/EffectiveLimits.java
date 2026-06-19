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

package org.prism_mc.prism.paper.services.limits;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The merged, most-permissive set of value limits that apply to one sender for
 * one command, derived from every limit permission node the sender holds.
 *
 * <p>A parameter only appears in a map when at least one held node defined a
 * constraint for it; an absent key means the parameter is unconstrained.
 */
public final class EffectiveLimits {

    /**
     * Limits that constrain nothing.
     */
    public static final EffectiveLimits EMPTY = new EffectiveLimits(Map.of(), Map.of(), Map.of(), Map.of());

    /**
     * The highest permitted value per numeric parameter.
     */
    private final Map<String, Integer> maxByParameter;

    /**
     * The lowest permitted value per numeric parameter.
     */
    private final Map<String, Integer> minByParameter;

    /**
     * The longest permitted look-back, in seconds, per duration parameter.
     */
    private final Map<String, Long> maxTimeSecondsByParameter;

    /**
     * The set of permitted values per whitelisted parameter (lowercased).
     */
    private final Map<String, Set<String>> allowedByParameter;

    /**
     * Constructor.
     *
     * @param maxByParameter The maximums
     * @param minByParameter The minimums
     * @param maxTimeSecondsByParameter The max look-back durations
     * @param allowedByParameter The allowed value sets
     */
    public EffectiveLimits(
        Map<String, Integer> maxByParameter,
        Map<String, Integer> minByParameter,
        Map<String, Long> maxTimeSecondsByParameter,
        Map<String, Set<String>> allowedByParameter
    ) {
        this.maxByParameter = maxByParameter;
        this.minByParameter = minByParameter;
        this.maxTimeSecondsByParameter = maxTimeSecondsByParameter;
        this.allowedByParameter = allowedByParameter;
    }

    /**
     * Whether no limits apply at all.
     *
     * @return True if nothing is constrained
     */
    public boolean isEmpty() {
        return (
            maxByParameter.isEmpty() &&
            minByParameter.isEmpty() &&
            maxTimeSecondsByParameter.isEmpty() &&
            allowedByParameter.isEmpty()
        );
    }

    /**
     * The maximum permitted value for a numeric parameter.
     *
     * @param parameter The parameter name
     * @return The maximum, or empty if unconstrained
     */
    public Optional<Integer> max(String parameter) {
        return Optional.ofNullable(maxByParameter.get(parameter));
    }

    /**
     * The minimum permitted value for a numeric parameter.
     *
     * @param parameter The parameter name
     * @return The minimum, or empty if unconstrained
     */
    public Optional<Integer> min(String parameter) {
        return Optional.ofNullable(minByParameter.get(parameter));
    }

    /**
     * The single look-back cap that applies to this query's lower time bound,
     * derived from every configured max-time (since/before) entry. Time bounds
     * are a single axis — how far into the past the query may reach — so the
     * most-permissive (largest) configured window wins, consistent with how
     * limits merge across the nodes a sender holds.
     *
     * @return The longest permitted look-back in seconds, or empty if unconstrained
     */
    public Optional<Long> maxLookbackSeconds() {
        return maxTimeSecondsByParameter.values().stream().max(Long::compare);
    }

    /**
     * The set of permitted values for a whitelisted parameter.
     *
     * @param parameter The parameter name
     * @return The allowed values (lowercased), or empty if unconstrained
     */
    public Optional<Set<String>> allowedValues(String parameter) {
        return Optional.ofNullable(allowedByParameter.get(parameter));
    }
}
