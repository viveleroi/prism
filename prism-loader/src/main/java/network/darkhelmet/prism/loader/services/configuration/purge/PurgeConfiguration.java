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

package network.darkhelmet.prism.loader.services.configuration.purge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

import network.darkhelmet.prism.loader.services.configuration.cache.DurationConfiguration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
public class PurgeConfiguration {
    @Comment("""
            Set the maximum number of records to delete in each "purge batch".
            Deleting records often locks every affected row which can block other
            queries or new data waiting for insert. If purging is causing performance
            issues or errors in the console, lower this value.
            Please see https://prism.readthedocs.io/en/latest/purges.html for more.
            """)
    private int limit = 5000;

    @Comment("""
            Configure the delay between purge cycles. Adding a delay helps break up
            the purge queries over time, which helps avoid purges dominating the db.
            """)
    private DurationConfiguration cycleDelay = new DurationConfiguration(2, TimeUnit.SECONDS);

    private List<CommandScheduleConfiguration> commandSchedules = new ArrayList<>();

    /**
     * Constructor.
     */
    public PurgeConfiguration() {
        commandSchedules.add(new CommandScheduleConfiguration());
    }
}
