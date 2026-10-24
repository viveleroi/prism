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

package org.prism_mc.prism.paper.integrations.worldedit;

import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.Extent;
import java.lang.reflect.Method;
import lombok.experimental.UtilityClass;

/**
 * FAWE-specific logging hooks.
 */
@UtilityClass
public class FaweLoggingHelper {

    /**
     * FAWE's Extent#addProcessor, resolved via reflection since we compile against WorldEdit.
     */
    private static final Method ADD_PROCESSOR;

    static {
        try {
            ADD_PROCESSOR = Extent.class.getMethod("addProcessor", IBatchProcessor.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Register our processor with the edit session's extent.
     *
     * @param event The edit session event
     * @param recorder The change recorder
     */
    public static void addProcessor(EditSessionEvent event, WorldEditChangeRecorder recorder) {
        Extent extent = event.getExtent();

        Extent result;
        try {
            result = (Extent) ADD_PROCESSOR.invoke(extent, new PrismBlockChangeProcessor(recorder));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to add Prism's FAWE processor", e);
        }

        if (result != extent) {
            event.setExtent(result);
        }
    }
}
