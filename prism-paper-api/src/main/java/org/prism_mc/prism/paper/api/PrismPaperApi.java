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

package org.prism_mc.prism.paper.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.prism_mc.prism.api.Prism;
import org.prism_mc.prism.api.activities.Activity;
import org.prism_mc.prism.api.activities.ActivityQuery;
import org.prism_mc.prism.api.services.modifications.ModificationQueueResult;
import org.prism_mc.prism.api.services.modifications.ModificationRuleset;
import org.prism_mc.prism.paper.api.actions.PrismPaperActionFactory;

public interface PrismPaperApi extends Prism {
    /**
     * Get the Paper action factory.
     *
     * @return The Paper action factory
     */
    @Override
    PrismPaperActionFactory actionFactory();

    /**
     * Look up activities matching the given query asynchronously.
     *
     * <p>The returned future completes off the main thread on Prism's storage executor;
     * use {@code thenAcceptAsync} or hop yourself if you need to touch world state.</p>
     *
     * <p>For pagination, use {@link ActivityQuery}'s {@code limit} and {@code offset} fields
     * and re-call this method per page.</p>
     *
     * @param query The activity query
     * @return A future completing with the matching activities
     */
    CompletableFuture<List<Activity>> lookup(ActivityQuery query);

    /**
     * Count activities matching the given query asynchronously.
     *
     * <p>The returned future completes off the main thread on Prism's storage executor;
     * use {@code thenAcceptAsync} or hop yourself if you need to touch world state.</p>
     *
     * @param query The activity query
     * @return A future completing with the matching activity count
     */
    CompletableFuture<Integer> queryCount(ActivityQuery query);

    /**
     * Rollback activities matching the given query, using the server's default ruleset.
     *
     * <p>Equivalent to {@link #rollback(Object, ActivityQuery, ModificationRuleset)} with
     * {@link org.prism_mc.prism.api.services.modifications.ModificationQueueService#defaultModificationRuleset()}.</p>
     *
     * @param owner The owner — typically a Player or CommandSender; used for queue ownership and result lookup
     * @param query The activity query
     * @return A future completing when the rollback finishes, or failing if the queue is busy or the query throws
     */
    CompletableFuture<ModificationQueueResult> rollback(Object owner, ActivityQuery query);

    /**
     * Rollback activities matching the given query.
     *
     * <p>The future completes on a non-main thread. If your continuation touches world
     * state, hop to the appropriate scheduler (entity / region / global) before doing so.</p>
     *
     * <p>The future fails with {@link IllegalStateException} if a modification queue is
     * already running. If the query yields no results, the future completes with an
     * empty {@link ModificationQueueResult} whose {@code queue()} is {@code null}.</p>
     *
     * @param owner The owner
     * @param query The activity query
     * @param ruleset The modification ruleset to use
     * @return A future completing when the rollback finishes
     */
    CompletableFuture<ModificationQueueResult> rollback(Object owner, ActivityQuery query, ModificationRuleset ruleset);

    /**
     * Restore activities matching the given query, using the server's default ruleset.
     *
     * @param owner The owner
     * @param query The activity query
     * @return A future completing when the restore finishes
     */
    CompletableFuture<ModificationQueueResult> restore(Object owner, ActivityQuery query);

    /**
     * Restore activities matching the given query.
     *
     * <p>The future completes on a non-main thread. If your continuation touches world
     * state, hop to the appropriate scheduler (entity / region / global) before doing so.</p>
     *
     * <p>The future fails with {@link IllegalStateException} if a modification queue is
     * already running. If the query yields no results, the future completes with an
     * empty {@link ModificationQueueResult} whose {@code queue()} is {@code null}.</p>
     *
     * @param owner The owner
     * @param query The activity query
     * @param ruleset The modification ruleset to use
     * @return A future completing when the restore finishes
     */
    CompletableFuture<ModificationQueueResult> restore(Object owner, ActivityQuery query, ModificationRuleset ruleset);

    /**
     * Preview a rollback of activities matching the given query, using the server's default ruleset.
     *
     * <p>The future completes when planning finishes (the preview is staged). The queue
     * remains active afterward — call {@code apply()} on
     * {@link org.prism_mc.prism.api.services.modifications.ModificationQueueService#currentQueueForOwner(Object)}
     * to commit it, or {@link org.prism_mc.prism.api.services.modifications.ModificationQueueService#clearEverythingForOwner(Object)}
     * to cancel it.</p>
     *
     * @param owner The owner
     * @param query The activity query
     * @return A future completing when the preview is staged
     */
    CompletableFuture<ModificationQueueResult> preview(Object owner, ActivityQuery query);

    /**
     * Preview a rollback of activities matching the given query.
     *
     * <p>The future completes on a non-main thread. If your continuation touches world
     * state, hop to the appropriate scheduler (entity / region / global) before doing so.</p>
     *
     * <p>The future fails with {@link IllegalStateException} if a modification queue is
     * already running. If the query yields no results, the future completes with an
     * empty {@link ModificationQueueResult} whose {@code queue()} is {@code null}.</p>
     *
     * @param owner The owner
     * @param query The activity query
     * @param ruleset The modification ruleset to use
     * @return A future completing when the preview is staged
     */
    CompletableFuture<ModificationQueueResult> preview(Object owner, ActivityQuery query, ModificationRuleset ruleset);
}
