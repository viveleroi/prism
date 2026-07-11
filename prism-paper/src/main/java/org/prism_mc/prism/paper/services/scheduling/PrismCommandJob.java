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

package org.prism_mc.prism.paper.services.scheduling;

import org.bukkit.Bukkit;
import org.prism_mc.prism.loader.services.logging.LoggingService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class PrismCommandJob implements Job {

    /**
     * Execute this job.
     *
     * @param context The context
     */
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String command = dataMap.getString("command");
        if (command != null) {
            // A paused server (empty for pause-when-empty-seconds) stops ticking the game thread, so
            // runGlobal tasks would queue up and all fire at once when it resumes. Skip instead.
            if (Bukkit.getServer().isPaused()) {
                LoggingService loggingService = (LoggingService) dataMap.get("loggingService");
                loggingService.debug("Skipping scheduled command because the server is paused: {0}", command);

                return;
            }

            PrismScheduler prismScheduler = (PrismScheduler) dataMap.get("prismScheduler");
            // Jobs via the scheduler are async, but commands must execute on the game thread
            prismScheduler.runGlobal(() -> {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            });
        }
    }
}
