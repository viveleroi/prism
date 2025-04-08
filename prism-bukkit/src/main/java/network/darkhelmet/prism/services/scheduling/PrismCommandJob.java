package network.darkhelmet.prism.services.scheduling;

import network.darkhelmet.prism.PrismBukkit;
import org.bukkit.Bukkit;
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
            // Jobs via the scheduler are async, but commands must execute on the game thread
            Bukkit.getScheduler().runTask(PrismBukkit.instance().loaderPlugin(), () -> {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            });
        }
    }
}