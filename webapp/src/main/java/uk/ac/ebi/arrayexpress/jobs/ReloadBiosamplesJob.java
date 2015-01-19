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


public class ReloadBiosamplesJob extends ApplicationJob {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void doExecute(JobExecutionContext jec) throws Exception {
		logger.info("Reloading all Biosamples data into the Application Server");
		File setupDirectory = null;
		File backDir = null;
		//File globalSetupDBDirectory = null;
		File setupTempDirectory = null;
		String hostname="NA";
		try {
		    hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		    logger.error("Host not available-> " + e.getMessage());
		}
		try {

			// Thread.currentThread().sleep(30000);//sleep for 1000 ms

			boolean updateActive = Application.getInstance().getPreferences()
					.getBoolean("bs.xmlupdate.active");
			logger.info("Is Reloading Active?->" + updateActive);

			if (!updateActive) {
				logger.error("ReloadBiosamplesJob is trying to execute and the configuration does not allow that");
				this.getApplication()
						.sendEmail(null,null,hostname + "->"+
								"BIOSAMPLES: WARNING",
								"ReloadBiosamplesJob is trying to execute and the configuration does not allow that!");
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
			logger.info("setupDir->" + setupDir);

			setupDirectory = new File(setupDir);
			String backupDirectory = Application.getInstance().getPreferences()
					.getString("bs.backupDirectory");
			logger.info("backupDirectory->" + backupDirectory);

			String globalSetupDir = Application.getInstance().getPreferences()
					.getString("bs.globalSetupDirectory");
			logger.info("globalSetupDirectory->" + globalSetupDir);
			File globalSetupDirectory = new File(globalSetupDir);

			String globalSetupDBDir = Application.getInstance().getPreferences()
					.getString("bs.globalSetupDBDirectory");
			logger.info("globalSetupDBDirectory->" + globalSetupDBDir);
			
			String globalSetupLuceneDir = Application.getInstance().getPreferences()
					.getString("bs.globalSetupLuceneDirectory");
			logger.info("globalSetupLuceneDir->" + globalSetupLuceneDir);
			
			
			//globalSetupDBDirectory=new File(globalSetupDBDir);
			
			String dbname = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.dbname");
			String dbPathDirectory = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.path");
			File dbDirectory = new File(dbPathDirectory +  File.separator  + dbname);
			logger.debug("dbPathDirectory->" + dbDirectory);
			
			// this variable will be used in the creation of the bakup
			// directory anda in the creation od the database backup
			Long tempDir = System.nanoTime();

			String newDir = "backup_" + tempDir;
			backDir = new File(backupDirectory + File.separator + newDir);
			if (backDir.mkdir()) {
				logger.info("Backup directory was created in [{}]",
						backDir.getAbsolutePath());

			} else {
				// TODO: rpe stop the process
				logger.error("Backup directory was NOT created in [{}]",
						backDir.getAbsolutePath());
				throw new Exception(hostname + "->"+"Backup directory was NOT created in "
						+ backDir.getAbsolutePath());
			}

			// DownloadBiosamplesXmlFileFromAGE dxml = new
			// DownloadBiosamplesXmlFileFromAGE();
			// I need to know which type of biosample updting process am I using
			String typeBioSampleUpdate = Application.getInstance()
					.getPreferences().getString("bs.xmlupdate.type");
			logger.debug("Type of Biosamples updating process->"
					+ typeBioSampleUpdate);
			IDownloadBiosamplesXmlFile dxml = DownloadBiosamplesXmlFileFactory
					.createDownloadBiosamplesXmlFile(typeBioSampleUpdate);
			File xmlDir = new File(backDir.getAbsolutePath() + "/XmlDownload");
			if (xmlDir.mkdir()) {
				logger.info("XmlDownload  directory was created in [{}]",
						xmlDir.getAbsolutePath());
			} else {
				logger.error("XmlDownload directory was NOT created in [{}]",
						xmlDir.getAbsolutePath());
				throw new Exception(hostname + "->"+
						"XmlDownload  directory was NOT created in "
								+ xmlDir.getAbsolutePath());
			}
			String downloadDirectory = xmlDir.getAbsolutePath();
			boolean downloadOk = dxml.downloadXml(downloadDirectory);

			if (downloadOk) {

					File oldSetupDir = new File(backDir.getAbsolutePath()
							+ "/OldSetup");
					if (oldSetupDir.mkdir()) {
						logger.info(
								"OldSetup Backup directory was created in [{}]",
								oldSetupDir.getAbsolutePath());
						copyDirectory(setupDirectory, oldSetupDir);
					} else {
						logger.error(
								"OldSetup Backup directory was NOT created in [{}]",
								oldSetupDir.getAbsolutePath());
						throw new Exception(hostname + "->"+
								"oldSetupDir Backup directory was NOT created in "
										+ oldSetupDir.getAbsolutePath());
					}
				
				// update of the xmlDatabase
				logger.info("DatabaseXml Creation");

				// File newSetupDir= new File(backDir.getAbsolutePath() +
				// "/newSetup" );
				// I need to change this because it's not possible to move
				// directories from a local disk (/tomcat/temp to NFS). So my
				// all temporary Setup will be created in the same place where
				// is th Setup Directory)
				// getParentFile() to create at the same level of Setup
				// directory
				File newSetupDir = new File(setupDirectory.getParentFile()
						.getAbsolutePath() + "/newSetup");
				
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
				} else {
					logger.error(
							"newSetupDir directory was NOT created in [{}]",
							newSetupDir.getAbsolutePath());
					throw new Exception(hostname + "->"+
							"newSetupDir  directory was NOT created in "
									+ newSetupDir.getAbsolutePath());
				}

				// update in a temporary database
				// index it in the newSetupDir
				updateXMLDatabase(xmlDir, newSetupDir, tempDir);
				logger.info("End of DatabaseXml Creation");
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
				File globalSetupLuceneDirectory = new File(globalSetupDir + File.separator + globalSetupLuceneDir);
				if (success2) {
					logger.info("newSetupDir was successfully renamed to [{}]!!!",
							setupDirectory.getAbsolutePath());
					// need to remove the globalSetupDirectory e copy the new
					// one to there
					if (globalSetupLuceneDirectory.exists()) {
						FileUtils.forceDelete(globalSetupLuceneDirectory);
					} else {
						logger.info(
								"globalSetupLuceneDirectory doesnt exist!! [{}]!!!",
								globalSetupLuceneDirectory.getAbsolutePath());
					}
					
					
				} else {
					logger.error("newSetupDir was not successfully renamed to [{}]!!!",
							setupDirectory.getAbsolutePath());
					throw new Exception(hostname + "->"+"newSetupDir was not successfully renamed to ->" +
							setupDirectory.getAbsolutePath());
				}
				logger.info("Deleting Setup Directory and renaming - End");

				// I do this to know the number of elements
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

				
				
				//Copy the data to GlobalSETUP (doing this here to reduce the downtime				
				FileUtils.copyDirectory(setupDirectory,
						globalSetupLuceneDirectory);
				logger.info(
						"XML DB was copied to globalSetupLuceneDirectory !! [{}]!!!",
						globalSetupLuceneDirectory.getAbsolutePath());			
				//I will also copy there the XML DB					
				File newSetupDBDir = new File(dbDirectory.getParentFile()
						.getAbsolutePath()  +  File.separator  + dbname );
				
				File globalSetupDBDirectory = new File(globalSetupDir +  File.separator  + globalSetupDBDir);
				if (globalSetupDBDirectory.exists()) {
					FileUtils.forceDelete(globalSetupDBDirectory);
				} else {
					logger.info(
							"globalSetupDBDirectory doesnt exist!! [{}]!!!",
							globalSetupDBDirectory.getAbsolutePath());
					throw new Exception(hostname + "->"+"globalSetupDBDirectory doesnt exist!! ->" +
							globalSetupDBDirectory.getAbsolutePath());
				}
				
				if (newSetupDBDir.exists()) {
					FileUtils.copyDirectory(newSetupDBDir,
							globalSetupDBDirectory);
					logger.info(
							"XML DB was copied to globalSetupDBDirectory !! [{}]!!!",
							globalSetupDBDirectory.getAbsolutePath());
				}
				else{
				logger.error(
						"New Xml DB doesnt exist!! [{}]!!!",
						newSetupDBDir.getAbsolutePath());
				throw new Exception(hostname + "->"+"New Xml DB doesnt exist!!->" +
						newSetupDBDir.getAbsolutePath());
				}
				
				//send an email saying that everything is ok (with some stats)
				this.getApplication()
				.sendEmail(null,null,hostname + "->"+
						"BIOSAMPLES: RELOAD samplegroups-> + " + ((IndexEnvironmentBiosamplesGroup) search.getController()
								.getEnvironment("biosamplesgroup")).getCountDocuments() + " samples->" +((IndexEnvironmentBiosamplesSample) search.getController()
										.getEnvironment("biosamplessample")).getCountDocuments(),
						"ReloadBiosamplesJob is finished!");
				

			} else {
				logger.debug("Something went wrong on Xml download");
				throw new Exception(hostname + "->"+" Something went wrong on Xml download");
				
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

		}
		logger.info("End of Reloading all Biosamples data into the Application Server");

		// I want to start using the new version of the data
		// (/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/StagingArea/4)
	}

	public void updateXMLDatabase(File xmlDirectory, File newSetupDirectory,
			long tempDir) throws Exception {

		// Create a client session with host name, port, user name and password

		logger.info("* Create a client session int the Xml Database.");

		String dbHost = Application.getInstance().getPreferences()
				.getString("bs.xmldatabase.host");
		int dbPort = Integer.parseInt(Application.getInstance()
				.getPreferences().getString("bs.xmldatabase.port"));
		String dbPassword = Application.getInstance().getPreferences()
				.getString("bs.xmldatabase.adminpassword");

		String originalDbName = Application.getInstance().getPreferences()
				.getString("bs.xmldatabase.dbname");

		Session session = new ClientSession(dbHost, dbPort, "admin", dbPassword);

		// ------------------------------------------------------------------------
		// Create a database
		logger.info("* Create a database.");

		String tempDbName = originalDbName + "_" + tempDir;
		String logs = session.execute(new CreateDB(tempDbName, xmlDirectory
				.getAbsolutePath()));
		// .getAbsolutePath() + "/XmlFiles"));
		logger.info("CreateDB('" + tempDbName + "' ...->" + logs);

		logs = session.execute("CLOSE");
		logger.debug("CLOSE ...->" + logs);

		logger.info("Start Indexing ...");
		// I will create now the Lucene Indexes ...
		SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));
		// TODO: rpe change all this static values
		search.getController().indexFromXmlDB("biosamplesgroup", Indexer.RebuildCategories.REBUILD,
				newSetupDirectory.getAbsolutePath() + "/LuceneIndexes", dbHost,
				dbPort, dbPassword, tempDbName);

		// / ((IndexEnvironmentBiosamplesGroup) search.getController()
		// / .getEnvironment("biosamplesgroup")).setup();
		// TODO: rpe this db path shoul not be static
		search.getController().indexFromXmlDB("biosamplessample", Indexer.RebuildCategories.REBUILD,
				newSetupDirectory.getAbsolutePath() + "/LuceneIndexes", dbHost,
				dbPort, dbPassword, tempDbName);
		// / ((IndexEnvironmentBiosamplesSample) search.getController()
		// / .getEnvironment("biosamplessample")).setup();
		logger.info("End Indexing ...");
		//

		// index
		logger.info("DatabaseXml Rename - From now on the database is not answering anymore!");
		logs = session.execute("ALTER DATABASE " + originalDbName + " "
				+ tempDbName + "_backup");
		logger.info("ALTER DATABASE " + originalDbName + " " + tempDbName
				+ "_backup" + "->" + logs);
		logs = session.execute("ALTER DATABASE " + tempDbName + " "
				+ originalDbName);

		logger.info("DatabaseXml Rename - End!");

		logger.info("* Close the client session.");
		session.close();

		// I neeed to reinitialize the XmldbConnectionPool otherwise I wiil be
		// looking to old data!
		search.getComponent("XmlDbConnectionPool").terminate();
		search.getComponent("XmlDbConnectionPool").initialize();

	}

	void deleteDirectory(File f) throws IOException {
		FileUtils.deleteDirectory(f);
	}

	public void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {
		FileUtils.copyDirectory(sourceLocation, targetLocation);

	}

}