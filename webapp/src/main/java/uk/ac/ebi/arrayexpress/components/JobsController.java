package uk.ac.ebi.arrayexpress.components;

/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.jobs.*;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesGroup;

import java.text.ParseException;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.EverythingMatcher.allJobs;


public class JobsController extends ApplicationComponent
{
    // jobs group
    private static final String AE_JOBS_GROUP = "ae-jobs";

    // quartz scheduler
    private Scheduler scheduler;

    public void initialize() throws Exception
    {
        // create scheduler
        this.scheduler = new StdSchedulerFactory().getScheduler();


        addJob("reload-efo", ReloadOntologyJob.class);
        addJob("update-efo", UpdateOntologyJob.class);
        
        
        //rpe: job that reload all the data
        addJob("reload-all", ReloadBiosamplesJob.class);

        
        
        //rpe: job that reload all the data from disk (the data is already processed - lucene indexes and xmlDB)
       // addJob("reload-all-disk", ReloadBiosamplesJobFromDisk.class);
        
        
        //update the GlobalSetup directory based on a new one (passed by configuration)
        //addJob("update-global-setup-disk", UpdateGlobalSetupBiosamplesJobFromDisk.class);
        
        //rpe: job that reloads incremental data
       /// addJob("incremental-reload", IncrementalReloadBiosamplesJob.class);
        

        scheduleJob("update-efo", "bs.efo.update");
        scheduleJob("reload-all", "bs.reload-all.update");
//        scheduleJob("reload-all-disk", "bs.reload-all-disk.update");
//        scheduleJob("update-global-setup-disk", "bs.update-global-setup-disk.update");
        startScheduler();
    }

    public void terminate() throws Exception
    {
        terminateJobs();
    }

    public void executeJob( String name ) throws SchedulerException
    {
        getScheduler().triggerJob(new JobKey(name, AE_JOBS_GROUP));
    }

    public void executeJobWithParam( String name, String paramName, String paramValue ) throws SchedulerException
    {
        JobDataMap map = new JobDataMap();
        map.put(paramName, paramValue);
        getScheduler().triggerJob(new JobKey(name, AE_JOBS_GROUP), map);
    }

    public void addJobListener( JobListener jl ) throws SchedulerException
    {
        if (null != jl) {
            getScheduler().getListenerManager().addJobListener(jl, allJobs());
        }
    }

    public void removeJobListener( JobListener jl ) throws SchedulerException
    {
        if (null != jl) {
            getScheduler().getListenerManager().removeJobListener(jl.getName());
        }
    }

    public void scheduleJobAtStart( String name ) throws SchedulerException
    {
        Trigger atStartTrigger = newTrigger()
                .withIdentity(name + "_at_start_trigger", AE_JOBS_GROUP)
                .forJob(name, AE_JOBS_GROUP)
                .startNow()
                .build();
        getScheduler().scheduleJob(atStartTrigger);
    }

    private void startScheduler() throws SchedulerException
    {
        getScheduler().start();
    }

    private Scheduler getScheduler()
    {
        return scheduler;
    }

    private void addJob( String name, Class<? extends Job> c ) throws SchedulerException
    {
        JobDetail j = newJob(c)
                .withIdentity(name, AE_JOBS_GROUP)
                .storeDurably(true)
                .requestRecovery(false)
                .build();
        getScheduler().addJob(j, false);
    }

    private void scheduleJob( String name, String preferencePrefix ) throws ParseException, SchedulerException
    {
        String schedule = getPreferences().getString(preferencePrefix + ".schedule");
        Integer interval = getPreferences().getInteger(preferencePrefix + ".interval");
        Boolean atStart = getPreferences().getBoolean(preferencePrefix + ".atstart");
        
//        System.out.println("preferencePrefix->" + preferencePrefix);
//        System.out.println("Nmame->" + name);
//       System.out.println("schedule->" + schedule);
//       System.out.println(interval);
//       System.out.println(atStart);

        if (null != schedule && 0 < schedule.length()) {
            CronExpression cexp = new CronExpression(schedule);
            Trigger cronTrigger = newTrigger()
                    .withIdentity(name + "_schedule_trigger", AE_JOBS_GROUP)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cexp))
                    .forJob(name, AE_JOBS_GROUP)
                    .build();
            // schedule a job with JobDetail and Trigger
            getScheduler().scheduleJob(cronTrigger);
        }

        boolean hasScheduledInterval = false;

        if (null != interval) {
            scheduleIntervalJob(name, interval);
            hasScheduledInterval = true;
        }

        if ((null != atStart && atStart) && !hasScheduledInterval) {
            scheduleJobAtStart(name);
        }
    }

    private void scheduleIntervalJob( String name, Integer interval ) throws SchedulerException
    {
        Trigger intervalTrigger = newTrigger()
                .withIdentity(name + "_interval_trigger", AE_JOBS_GROUP)
                .forJob(name, AE_JOBS_GROUP)
                .withSchedule(simpleSchedule()
                        .withIntervalInMinutes(interval)
                        .repeatForever())
                .startNow()
                .build();
        getScheduler().scheduleJob(intervalTrigger);
    }

    private void terminateJobs() throws SchedulerException
    {
        getScheduler().pauseAll();

        List runningJobs = getScheduler().getCurrentlyExecutingJobs();
        for (Object jec : runningJobs) {
            JobDetail j = ((JobExecutionContext) jec).getJobDetail();
            getScheduler().interrupt(j.getKey());
        }

        getScheduler().shutdown(true);
    }
  
    
	

	 @Override
    public String getMetaDataInformation(){
    	
    	String ret="<u>Synchronization Process</u>:<br>";
    	try {
    		
			
			Trigger t = scheduler.getTrigger(new TriggerKey("reload-all_schedule_trigger", AE_JOBS_GROUP));
			if(t!=null){
			ret+="Previous Time->" + t.getPreviousFireTime();
			ret+="<br>Next Time->" + t.getNextFireTime();
			}
			else{
				ret+="Not executed yet!";
			}

			
			boolean isRunning=false;
			List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
		     
			for (JobExecutionContext jobExecutionContext : executingJobs)
		      {
		        JobDetail execJobDetail = jobExecutionContext.getJobDetail();
		        //ret+=execJobDetail;
		        if (execJobDetail.getKey().equals(scheduler.getJobDetail(new JobKey("reload-all",AE_JOBS_GROUP)).getKey()))
		        {
		          isRunning=true;
		        }
		      }
			ret+="<br>Is it running?->" + isRunning;
			
			
//			 List<JobExecutionContext> currentJobs = scheduler.getCurrentlyExecutingJobs();
//			    for (JobExecutionContext jobCtx: currentJobs){
//			   
//			        ret+="the job is already running - do nothing->" + jobCtx;
//			    }     
//			ret+="Is it running?->"+ scheduler.getJobDetail(new JobKey("reload-all", AE_JOBS_GROUP));
			
//			ret+=scheduler.getJobGroupNames();
			
//			scheduler.getContext().
			
//			for (JobKey iterable_element : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(AE_JOBS_GROUP))) {
//				ret+="Job->" + scheduler.getJobDetail(iterable_element).getDescription();
//				//ret+="Job->" + scheduler.getTriggersOfJob(iterable_element).;				
//			}
//			for(String group: scheduler.getTriggerGroupNames()) {
//			    // enumerate each trigger in group
//			    for(TriggerKey triggerKey : scheduler.getTriggerKeys(TriggerMatcher.jobGroupEquals(AE_JOBS_GROUP)) {
//			        ret+="Found trigger identified by: " + triggerKey;
//			    }
//			}
			

		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return ret;
    }
}
