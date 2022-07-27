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

package network.darkhelmet.prism.injection.factories;

import java.util.List;
import java.util.function.Consumer;

import network.darkhelmet.prism.api.activities.ActivityQuery;
import network.darkhelmet.prism.api.activities.IActivity;
import network.darkhelmet.prism.api.services.modifications.ModificationQueueResult;
import network.darkhelmet.prism.api.services.modifications.ModificationRuleset;
import network.darkhelmet.prism.services.modifications.Rollback;

public interface IRollbackFactory {
    /**
     * Create the rollback instance.
     *
     * @param modificationRuleset The ruleset
     * @param owner The owner
     * @param query The query
     * @param modifications The modifications
     * @param onEnd The on end callback
     * @return A rollback instance
     */
    Rollback create(
        ModificationRuleset modificationRuleset,
        Object owner,
        ActivityQuery query,
        List<IActivity> modifications,
        Consumer<ModificationQueueResult> onEnd);
}
