package uk.ac.ebi.arrayexpress.jobs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationJob;
import uk.ac.ebi.arrayexpress.app.ApplicationPreferences;
import uk.ac.ebi.arrayexpress.components.BioSamplesGroup;
import uk.ac.ebi.arrayexpress.components.BioSamplesSample;
import uk.ac.ebi.arrayexpress.components.SearchEngine;
import uk.ac.ebi.arrayexpress.components.XmlDbConnectionPool;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesGroup;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesSample;

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


public class ReloadBiosamplesJobFromDisk extends ApplicationJob {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
public void doExecute(JobExecutionContext jec) throws Exception {
	logger.info("Reloading all Biosamples form disk data into the Application Server");
	File setupDirectory = null;
	File backDir = null;
	File setupTempDirectory = null;
	try {
		// Thread.currentThread().sleep(30000);//sleep for 1000 ms
		boolean updateActive = Application.getInstance().getPreferences()
				.getBoolean("bs.xmlupdate.active");
		logger.debug("Is Reloading Active?->" + updateActive);

		if (!updateActive) {
			logger.error("ReloadBiosamplesJobFromDisk is trying to execute and the configuration does not allow that");
			this.getApplication()
					.sendEmail(null,null,
							"BIOSAMPLES: WARNING",
							"ReloadBiosamplesJobFromDisk is trying to execute and the configuration does not allow that!");
			// throw new
			// Exception("ReloadBiosamplesJob is trying to execute and the configuration does not allow that!");
			return;
		}

		// I will create a backup directory, where I will backup the Actual
		// Setup directory, where I will put the new biosamples.xml and
		// where I will creste a new SetupDirectory based on the new
		// biosamples.xml
		String setupDir = Application.getInstance().getPreferences()
				.getString("bs.setupDirectory");
		logger.debug("setupDir->" + setupDir);

		setupDirectory = new File(setupDir);
		String backupDirectory = Application.getInstance().getPreferences()
				.getString("bs.backupDirectory");
		logger.debug("backupDirectory->" + backupDirectory);

		String globalSetupDir = Application.getInstance().getPreferences()
				.getString("bs.globalSetupDirectory");
		logger.debug("globalSetupDirectory->" + globalSetupDir);
		//File globalSetupDirectory = new File(globalSetupDir);

		String globalSetupDBDir = Application.getInstance().getPreferences()
				.getString("bs.globalSetupDBDirectory");
		logger.debug("globalSetupDBDirectory->" + globalSetupDBDir);
		File globalSetupDBDirectory = new File(globalSetupDir + File.separator + globalSetupDBDir);
		
		String globalSetupLuceneDir = Application.getInstance().getPreferences()
				.getString("bs.globalSetupLuceneDirectory");
		logger.debug("globalSetupLuceneDir->" + globalSetupLuceneDir);
		File globalSetupLuceneDirectory = new File(globalSetupDir + File.separator + globalSetupLuceneDir);
		
				
		String dbname = Application.getInstance().getPreferences()
				.getString("bs.xmldatabase.dbname");
		String dbPathDirectory = Application.getInstance().getPreferences()
				.getString("bs.xmldatabase.path");
		File dbDirectory = new File(dbPathDirectory + File.separator + dbname);
		logger.debug("dbPathDirectory->" + dbDirectory);

		// this variable will be used in the creation of the bakup
		// directory anda in the creation od the database backup
		Long tempDir = System.nanoTime();
		//I need the hostneme because I have 2 different production servers
	
		String hostname="";
		try {
		    hostname = InetAddress.getLocalHost().getHostName();
		    if (StringUtils.isEmpty( hostname)){	
		    	Random gerador = new Random();
		    	int number = gerador.nextInt();
		    	hostname=number+"";
		    }
		} catch (UnknownHostException e) {
		    // failed;  try alternate means.
		}
		String newDir = "backup_" + hostname +"_"+ tempDir;
		backDir = new File(backupDirectory + File.separator + newDir);
		if (backDir.mkdir()) {
			logger.info("Backup directory was created  in ->[{}]",
					backDir.getAbsolutePath());

		} else {
			// TODO: rpe stop the process
			logger.error("Backup directory was NOT created in [{}]",
					backDir.getAbsolutePath());
			throw new Exception("Backup directory was NOT created in "
					+ backDir.getAbsolutePath());
		}

		
		//I will make a backup from what we have now on the /tmp/Setup and also the database (in this case I do not use the globalSetup because the GlobalSetup has the new data that I want to upload
		File oldSetupDir = new File(backDir.getAbsolutePath()
					+ "/Old" + globalSetupLuceneDir);
			if (oldSetupDir.mkdir()) {
				logger.info(
						"OldSetup Backup directory was created in [{}]",
						oldSetupDir.getAbsolutePath());
				copyDirectory(setupDirectory, oldSetupDir);
			} else {
				logger.error(
						"OldSetup Backup directory was NOT created in [{}]",
						oldSetupDir.getAbsolutePath());
				throw new Exception(
						"oldSetupDir Backup directory was NOT created in "
								+ oldSetupDir.getAbsolutePath());
			}

			File oldSetupDBDir = new File(backDir.getAbsolutePath()
					+ "/Old" +globalSetupDBDir);
			if (oldSetupDBDir.mkdir()) {
				logger.info(
						"oldSetupDBDir Backup directory was created in [{}]",
						oldSetupDBDir.getAbsolutePath());
				copyDirectory(dbDirectory, oldSetupDBDir);
			} else {
				logger.error(
						"oldSetupDBDir Backup directory was NOT created in [{}]",
						oldSetupDBDir.getAbsolutePath());
				throw new Exception(
						"oldSetupDBDir Backup directory was NOT created in "
								+ oldSetupDBDir.getAbsolutePath());
			}
			
		
			

			
			// File newSetupDir= new File(backDir.getAbsolutePath() +
			// "/newSetup" );
			// I need to change this because it's not possible to move
			// directories from a local disk (/tomcat/temp to NFS). So my
			// all temporary Setup will be created in the same place where
			// is th Setup Directory)
			// getParentFile() to create at the same level of Setup
			// directory
		
			File newSetupDir = new File(setupDirectory.getParentFile()
					.getAbsolutePath() + File.separator + "new" + globalSetupLuceneDir);

			if (newSetupDir.exists()) {
				// I will force the delete of the NewSetupDir (I need this
				// because if for any reason the process fails once (before
				// it renames nesSetup to Setup), the next time the process
				// will always fail because the newSetup already exists
				FileUtils.forceDelete(newSetupDir);
				
			}
			if (newSetupDir.mkdir()) {
				logger.info("newSetupDir  directory was created in [{}]",
						newSetupDir.getAbsolutePath());
				//copy there the globalSetup
				copyDirectory(globalSetupLuceneDirectory, newSetupDir);
			} else {
				logger.error(
						"newSetupDir directory was NOT created in [{}]",
						newSetupDir.getAbsolutePath());
				throw new Exception(
						"newSetupDir  directory was NOT created in ->"
								+ newSetupDir.getAbsolutePath());
			}

			
			//new DB temp directory
			File newSetupDBDir = new File(dbDirectory.getParentFile()
					.getAbsolutePath() + File.separator +"new" + dbname );

			if (newSetupDBDir.exists()) {
				// I will force the delete of the NewSetupDir (I need this
				// because if for any reason the process fails once (before
				// it renames nesSetup to Setup), the next time the process
				// will always fail because the newSetup already exists
				FileUtils.forceDelete(newSetupDBDir);
				
			}
			if (newSetupDBDir.mkdir()) {
				logger.info("newSetupDBDir  directory was created in [{}]",
						newSetupDBDir.getAbsolutePath());
				//copy there the globalSetup
				copyDirectory(globalSetupDBDirectory, newSetupDBDir);
			} else {
				logger.error(
						"newSetupDBDir directory was NOT created in [{}]",
						newSetupDBDir.getAbsolutePath());
				throw new Exception(
						"newSetupDBDir  directory was NOT created in "
								+ newSetupDBDir.getAbsolutePath());
			}
			
				
			// only after update the database I update the Lucenes Indexes
			logger.info("Deleting Setup Directory and renaming - from now on the application is not answering");

			SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));

			// I need to close the IndexReader otherwise it would not be
			// possible dor me to delete the Setup directory (this problem
			// only occurs on NFS);
			search.getController().getEnvironment("biosamplesgroup")
					.closeIndexReader();
			search.getController().getEnvironment("biosamplessample")
					.closeIndexReader();

			// remove the old setupdirectory /tmp/Setup is deleted
			deleteDirectory(setupDirectory);
			// Rename file (or directory) /tmp/newSetup->  /tmp/Setup
			logger.info("Before file renamed!!!");
			
			
			boolean success2 = newSetupDir.renameTo(setupDirectory);
			
			// FileUtilities.
			if (success2) {
				logger.info("newSetupDir was successfully renamed to [{}]!!!",
						setupDirectory.getAbsolutePath());
				
			} else {
				logger.error("newSetupDir was not successfully renamed to [{}]!!!",
						setupDirectory.getAbsolutePath());
				throw new Exception("newSetupDir was not successfully renamed to [{}]!!!" +
						setupDirectory.getAbsolutePath());
			}
			logger.info("Deleting Setup Directory and renaming - End");
			
			
			logger.info("Before file DBrenamed!!!");
			logger.info("newSetupDBDir is  in [{}]!!!",
					newSetupDBDir.getAbsolutePath());
			logger.info("dbDirectory is  in [{}]!!!",
					dbDirectory.getAbsolutePath());
			
			
			XmlDbConnectionPool xmlDb = ((XmlDbConnectionPool) getComponent("XmlDbConnectionPool"));
			xmlDb.terminate();
			deleteDirectory(dbDirectory);
			
			
			boolean successDB2 = newSetupDBDir.renameTo(dbDirectory);
			
			// FileUtilities.
			if (successDB2) {
				logger.info("newSetupDBDir was successfully renamed to [{}]!!!",
						dbDirectory.getAbsolutePath());
				
			} else {
				logger.error("newSetupDBDir was not successfully renamed to [{}]!!!",
						dbDirectory.getAbsolutePath());
				throw new Exception("newSetupDBDir was not successfully renamed to [{}]!!!" +
						dbDirectory.getAbsolutePath());
			}
			logger.info("Deleting SetupDB Directory and renaming - End");

			// I do this to know the number of elements
			xmlDb.initialize();
			((BioSamplesGroup) getComponent("BioSamplesGroup"))
					.reloadIndex();
			// TODO: rpe nowaday I need to do this to clean the xmldatabase
			// connection nad to reload the new index
			((IndexEnvironmentBiosamplesGroup) search.getController()
					.getEnvironment("biosamplesgroup")).setup();

			((BioSamplesSample) getComponent("BioSamplesSample"))
					.reloadIndex();
			// TODO: rpe nowadays I need to do this to clean the xmldatabase
			// connection nad to reload the new index
			((IndexEnvironmentBiosamplesSample) search.getController()
					.getEnvironment("biosamplessample")).setup();

			// / search.getController().getEnvironment("biosamplesgroup")
			// / .indexReader();
			// / //I need to setupIt to point to the new Database
			// / ((IndexEnvironmentBiosamplesGroup) search.getController()
			// / .getEnvironment("biosamplesgroup")).setup();
			// / search.getController().getEnvironment("biosamplessample").
			// / indexReader();
			// / ((IndexEnvironmentBiosamplesSample) search.getController()
			// / .getEnvironment("biosamplessample")).setup();
			// TODO: RPE Update the EFO!!??

	} catch (Exception e) {
		throw new RuntimeException(e);
	} finally {

	}
	logger.info("End of Reloading all Biosamples data into the Application Server");

	// I want to start using the new version of the data
	// (/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/StagingArea/4)
}



	void deleteDirectory(File f) throws IOException {
		FileUtils.deleteDirectory(f);
	}

	public void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {
		FileUtils.copyDirectory(sourceLocation, targetLocation);

	}

}