package uk.ac.ebi.arrayexpress.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.basex.BaseXServer;
import org.basex.core.cmd.CreateDB;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationJob;
import uk.ac.ebi.arrayexpress.components.BioSamplesGroup;
import uk.ac.ebi.arrayexpress.components.BioSamplesSample;
import uk.ac.ebi.arrayexpress.components.SearchEngine;
import uk.ac.ebi.arrayexpress.utils.file.FileUtilities;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentArrayDesigns;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesGroup;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentBiosamplesSample;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentExperiments;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentFiles;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironmentProtocols;
import uk.ac.ebi.arrayexpress.utils.saxon.search.Indexer;

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

//reloads the biosamples based on a news directory that contain the Lucene indexes and the Xml database
public class UpdateGlobalSetupBiosamplesJobFromDisk extends ApplicationJob {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void doExecute(JobExecutionContext jec) throws Exception {
		logger.info("Update GlobalSetup directory on  Biosamples thought disk data into the Application Server");
		File setupDirectory = null;
		File backDir = null;
		File setupTempDirectory = null;
		try {
		
			
			//This is the directory that will be used to update the GlobalSetup (must contain a Setup and SetupDB directories)
			String baseUpdateGlobalSetupDir = Application.getInstance().getPreferences()
					.getString("bs.update-global-setup-disk.fileSetup");		
			File baseUpdateGlobalSetupDirectory = new File(baseUpdateGlobalSetupDir);
			logger.debug("baseUpdateGlobalSetupDir->" + baseUpdateGlobalSetupDir);
			
			String baseUpdateGlobalSetupDBDir = Application.getInstance().getPreferences()
					.getString("bs.update-global-setup-disk.fileSetupDB");		
			File baseUpdateGlobalSetupDBDirectory = new File(baseUpdateGlobalSetupDBDir);
			logger.debug("baseUpdateGlobalSetupDBDir->" + baseUpdateGlobalSetupDBDir);
			
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
			File globalSetupDirectory = new File(globalSetupDir);

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
			String newDir = "backup_globalSetup_" + hostname +"_"+ tempDir;
			backDir = new File(backupDirectory + File.separator + newDir);
			if (backDir.mkdir()) {
				logger.info("Backup directory was created in [{}]",
						backDir.getAbsolutePath());

			} else {
				// TODO: rpe stop the process
				logger.error("Backup directory was NOT created in [{}]",
						backDir.getAbsolutePath());
				throw new Exception("Backup directory was NOT created in "
						+ backDir.getAbsolutePath());
			}

		
			
			//I will make a backup from what we have now on the /GlobalSetup and also the db if it exists
			File oldGlobalSetupDir = new File(backDir.getAbsolutePath()
						+ File.separator +"OldGlobalSetup");
				if (oldGlobalSetupDir.mkdir()) {
					logger.info(
							"oldGlobalSetupDir Backup directory was created in [{}]",
							oldGlobalSetupDir.getAbsolutePath());
					copyDirectory(globalSetupDirectory, oldGlobalSetupDir);					
				} else {
					logger.error(
							"oldGlobalSetupDir Backup directory was NOT created in [{}]",
							oldGlobalSetupDir.getAbsolutePath());
					throw new Exception(
							"oldGlobalSetupDir Backup directory was NOT created in ->"
									+ oldGlobalSetupDir.getAbsolutePath());
				}

		
				// File newSetupDir= new File(backDir.getAbsolutePath() +
				// "/newSetup" );
				// I need to change this because it's not possible to move
				// directories from a local disk (/tomcat/temp to NFS). So my
				// all temporary Setup will be created in the same place where
				// is th Setup Directory)
				// getParentFile() to create at the same level of Setup
				// directory
				File newGlobalSetupDir = new File(globalSetupDirectory.getParentFile()
						.getAbsolutePath() + File.separator + "new" + globalSetupLuceneDir);

				if (newGlobalSetupDir.exists()) {
					FileUtils.forceDelete(newGlobalSetupDir);
					
				}
				//TENHO DE COPIAR DE 1 DIRETORIA QUE VOU PASSAR COMO PARAMETRO
				
				if (newGlobalSetupDir.mkdir()) {
					logger.info("newGlobalSetupDir  directory was created in [{}]",
							newGlobalSetupDir.getAbsolutePath());
					//copy there the globalSetup
					copyDirectory(baseUpdateGlobalSetupDirectory, newGlobalSetupDir);
				} else {
					logger.error(
							"newGlobalSetupDir directory was NOT created in [{}]",
							newGlobalSetupDir.getAbsolutePath());
					throw new Exception(
							"newGlobalSetupDir  directory was NOT created in "
									+ newGlobalSetupDir.getAbsolutePath());
				}

				
				//new SetupDB global directory
				File newGlobalSetupDBDir = new File(globalSetupDBDirectory.getParentFile()
						.getAbsolutePath() + File.separator + "new" + globalSetupDBDir);

				if (newGlobalSetupDBDir.exists()) {
					FileUtils.forceDelete(newGlobalSetupDBDir);					
				}
				if (newGlobalSetupDBDir.mkdir()) {
					logger.info("newGlobalSetupDBDir  directory was created in [{}]",
							newGlobalSetupDBDir.getAbsolutePath());
					//copy there the globalSetup
					copyDirectory(baseUpdateGlobalSetupDBDirectory, newGlobalSetupDBDir);
				} else {
					logger.error(
							"newGlobalSetupDBDir directory was NOT created in [{}]",
							newGlobalSetupDBDir.getAbsolutePath());
					throw new Exception(
							"newGlobalSetupDBDir  directory was NOT created in "
									+ newGlobalSetupDBDir.getAbsolutePath());
				}
				
				
				// only after update the database I update the Lucenes Indexes
				logger.info("Deleting GlobalSetup Directories and renaming for the newOne -  application stills answering");

				deleteDirectory(globalSetupLuceneDirectory);
				// Rename file (or directory) /tmp/newSetup->  /tmp/Setup
				logger.info("Before file renamed!!!");
				boolean success2 = newGlobalSetupDir.renameTo(globalSetupLuceneDirectory);
				// FileUtilities.
				if (success2) {
					logger.info("newGlobalSetupDir was successfully renamed to [{}]!!!",
							newGlobalSetupDir.getAbsolutePath());
					
				} else {
					logger.error("newGlobalSetupDir was not successfully renamed to [{}]!!!",
							newGlobalSetupDir.getAbsolutePath());
					throw new Exception("newGlobalSetupDir was not successfully renamed to ->" +
							newGlobalSetupDir.getAbsolutePath());
				}
				logger.info("Deleting GlobalSetup Directory and renaming - End");
				
				
				logger.info("Before Global SetupDB renamed!!!");
				logger.info("newGlobalSetupDBDir is  in [{}]!!!",
						newGlobalSetupDBDir.getAbsolutePath());
				logger.info("GlobalSetupDBDir is  in [{}]!!!",
						globalSetupDBDirectory.getAbsolutePath());
				deleteDirectory(globalSetupDBDirectory);
				boolean successDB2 = newGlobalSetupDBDir.renameTo(globalSetupDBDirectory);
				// FileUtilities.
				if (successDB2) {
					logger.info("newGlobalSetupDBDir was successfully renamed to [{}]!!!",
							newGlobalSetupDBDir.getAbsolutePath());
					
				} else {
					logger.error("newGlobalSetupDBDir was not successfully renamed to [{}]!!!",
							newGlobalSetupDBDir.getAbsolutePath());
					throw new Exception("newGlobalSetupDBDir was not successfully renamed to [{}]!!!" +
							newGlobalSetupDBDir.getAbsolutePath());
				}
				logger.info("Deleting GlobalSetupDB Directory and renaming - End");

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

		}
		logger.info("End of creating a nem GlobalSetup on Biosamples");

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