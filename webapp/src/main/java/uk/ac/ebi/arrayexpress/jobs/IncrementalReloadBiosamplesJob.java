package uk.ac.ebi.arrayexpress.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.basex.BaseXServer;
import org.basex.core.cmd.CreateDB;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

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

public class IncrementalReloadBiosamplesJob extends ApplicationJob {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void doExecute(JobExecutionContext jec) throws Exception {
		logger.info("Incremental Reload of BioSamples data into the Application Server");
		File setupDirectory = null;
		File backDir = null;
		File setupTempDirectory = null;
		try {

			boolean updateActive = Application.getInstance().getPreferences()
					.getBoolean("bs.xmlupdate.active");
			logger.debug("Is Reloading Active?->" + updateActive);

			if (!updateActive) {
				logger.error("IncrementalReloadBiosamplesJob is trying to execute and the configuration does not allow that");
				this.getApplication()
						.sendEmail(null,null,
								"BIOSAMPLES: WARNING",
								"IncrementalReloadBiosamplesJob is trying to execute and the configuration does not allow that!");
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

			// this variable will be used in the creation of the bakup
			// directory anda in the creation od the database backup
			Long tempDir = System.nanoTime();

			String newDir = "backup_" + tempDir;
			backDir = new File(backupDirectory + "/" + newDir);
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
				throw new Exception(
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
					throw new Exception(
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
				// is the Setup Directory)
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
					throw new Exception(
							"newSetupDir  directory was NOT created in "
									+ newSetupDir.getAbsolutePath());
				}
				// Now I need to copy the Setup luceneIndexes directory to the
				// new Setup
				FileUtils.copyDirectory(setupDirectory, newSetupDir);

				// update in a temporary database
				// index it in the newSetupDir
				updateIncrementalXMLDatabase(xmlDir, newSetupDir, tempDir);
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

				// remove the old setupdirectory
				deleteDirectory(setupDirectory);

				// Rename file (or directory)
				logger.info("Before file renamed!!!");
				boolean success2 = newSetupDir.renameTo(setupDirectory);
				// FileUtilities.
				if (success2) {
					logger.info(
							"newSetupDir was successfully renamed to [{}]!!!",
							setupDirectory.getAbsolutePath());
					// need to remove the globalSetupDirectory e copy the new
					// one to there
					File globalSetupDirectory = new File(globalSetupDir);
					if (globalSetupDirectory.exists()) {
						FileUtils.forceDelete(globalSetupDirectory);
					} else {
						logger.info(
								"globalSetupDirectory doesnt exist!! [{}]!!!",
								globalSetupDirectory.getAbsolutePath());
					}
					FileUtils.copyDirectory(setupDirectory,
							globalSetupDirectory);

				} else {
					logger.error(
							"newSetupDir was not successfully renamed to [{}]!!!",
							setupDirectory.getAbsolutePath());
					throw new Exception(
							"newSetupDir was not successfully renamed to [{}]!!!"
									+ setupDirectory.getAbsolutePath());
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

			} else {
				logger.debug("Something went wrong on Xml download");
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

		}
		logger.info("End of Reloading all Biosamples data into the Application Server");

		// I want to start using the new version of the data
		// (/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/StagingArea/4)
	}

	public void updateIncrementalXMLDatabase(File xmlDirectory,
			File newSetupDirectory, long tempDir) throws Exception {

		// Create a client session with host name, port, user name and password

		logger.debug("* Create a client session int the Xml Database.");

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
		logger.debug("* Create a database.");

		String tempDbName = originalDbName + "_" + tempDir;
		String logs = session.execute(new CreateDB(tempDbName, xmlDirectory
				.getAbsolutePath()));
		// .getAbsolutePath() + "/XmlFiles"));
		logger.debug("CreateDB('" + tempDbName + "' ...->" + logs);

		// Now I need to create a copy of the current one db
		String copyOriginalDbName = originalDbName + "_copy_" + tempDir;
		logs = session.execute("copy " + originalDbName + " "
				+ copyOriginalDbName);
		logger.debug("copy " + originalDbName + " " + copyOriginalDbName + "->"
				+ logs);
		// I need to do the XQuery to update the data

		String connectionStringCopyDatabase = Application.getInstance()
				.getPreferences().getString("bs.xmldatabase.base")
				+ "://" + dbHost + ":" + dbPort + "/" + copyOriginalDbName;
		updateXMLDatabaseWithIncrementalDatabase(connectionStringCopyDatabase,
				tempDbName);

		logs = session.execute("CLOSE");
		logger.debug("CLOSE ...->" + logs);

		logger.debug("Start Indexing ...");
		// I will create now the Lucene Indexes ...
		SearchEngine search = ((SearchEngine) getComponent("SearchEngine"));
		// TODO: rpe change all this static values
		search.getController().indexFromXmlDB("biosamplesgroup",
				Indexer.RebuildCategories.INCREMENTALREBUILD,
				newSetupDirectory.getAbsolutePath() + "/LuceneIndexes", dbHost,
				dbPort, dbPassword, tempDbName);

		// / ((IndexEnvironmentBiosamplesGroup) search.getController()
		// / .getEnvironment("biosamplesgroup")).setup();
		// TODO: rpe this db path shoul not be static
		search.getController().indexFromXmlDB("biosamplessample",
				Indexer.RebuildCategories.INCREMENTALREBUILD,
				newSetupDirectory.getAbsolutePath() + "/LuceneIndexes", dbHost,
				dbPort, dbPassword, tempDbName);
		// / ((IndexEnvironmentBiosamplesSample) search.getController()
		// / .getEnvironment("biosamplessample")).setup();
		logger.debug("End Indexing ...");
		//

		// index
		logger.info("DatabaseXml Rename - From now on the database is not answering anymore!");
		logs = session.execute("ALTER DATABASE " + originalDbName + " "
				+ tempDbName + "_backup");
		logger.debug("ALTER DATABASE " + originalDbName + " " + tempDbName
				+ "_backup" + "->" + logs);
		logs = session.execute("ALTER DATABASE " + copyOriginalDbName + " "
				+ originalDbName);

		logger.info("DatabaseXml Rename - End!");

		logger.debug("* Close the client session.");
		session.close();

		// I neeed to reinitialize the XmldbConnectionPool otherwise I wiil be
		// looking to old data!
		search.getComponent("XmlDbConnectionPool").terminate();
		search.getComponent("XmlDbConnectionPool").initialize();

	}

	public void updateXMLDatabaseWithIncrementalDatabase(
			String connectionString, String incrementalDatabase)
			throws XMLDBException {
		Collection coll = DatabaseManager.getCollection(connectionString);
		XPathQueryService service = (XPathQueryService) coll.getService(
				"XPathQueryService", "1.0");
		String query = " for $gu in doc(\"" + incrementalDatabase
				+ "\")//SampleGroup return "
				+ " if (count(/Biosamples/SampleGroup[@id=$gu/@id])>0) then "
				+ " if($gu/@delete=true) then "
				+ " delete node /Biosamples/SampleGroup[@id=$gu/@id] "
//				+ "else ()";
				+ " else replace node /Biosamples/SampleGroup[@id=$gu/@id] with $gu "
				+ " else insert node $gu into /Biosamples";
		

		logger.debug("connectionString->" +  connectionString  +"##Query update incremental Xml Db ->[{}]",query);
		service.query(query);
		coll.close();
	}

	void deleteDirectory(File f) throws IOException {
		FileUtils.deleteDirectory(f);
	}

	public void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {
		FileUtils.copyDirectory(sourceLocation, targetLocation);

	}

}