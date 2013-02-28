package uk.ac.ebi.arrayexpress.components;

import java.io.File;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.file.FileUtilities;

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

// This classe is used to check if the Setup(and backup) Directory exists (this is necessary because aour application servers allways removed the temporary files when rebooted.
// If something is wrong I will sent an email to warn about that
public class SetupDirectoryCheck extends ApplicationComponent {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String setupDirectory;
	private String globalSetupDirectory;
	private String backupDirectory;

	public void initialize() throws Exception {
		HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
				.getInstance().getPreferences().getConfSubset("bs");

		String emailError = "";

		if (null != connsConf) {
			setupDirectory = connsConf.getString("setupDirectory");
			globalSetupDirectory = connsConf.getString("globalSetupDirectory");
			backupDirectory = connsConf.getString("backupDirectory");

			logger.debug("setupDirectory->" + setupDirectory);
			logger.debug("globalSetupDirectory->" + globalSetupDirectory);
			logger.debug("backupDirectory->" + backupDirectory);
		} else {
			logger.error("bs Configuration is missing!!");
		}
		try {
			File fileSetup = new File(setupDirectory);
			File fileGlobalSetup = new File(globalSetupDirectory);
			logger.debug("fileGlobalSetup->"
					+ fileGlobalSetup.getAbsolutePath());
			if (!fileGlobalSetup.exists()) {
				this.getApplication()
						.sendEmail(
								"BIOSAMPLES: ##### INITIALIZATION WARNING #######",
								"The GlobalSetupDirectory doesnt exist and it can cause problems (temporary directories removed during the servers restart)");
			}

			// Now I will try the backupdirectory
			File fileBackup = new File(backupDirectory);
			if (!fileBackup.exists()) {
				emailError += "The backupDirectory (" + backupDirectory
						+ ") doesnt exist!!!! ERROR\n";
				FileUtils.forceMkdir(fileBackup);
			}

			// setupDirectory test
			boolean exists = fileSetup.exists();
			if (!exists) {
				// send an email warning and copy it from a globalSetupDirectory
				emailError = "The Setup directory does not exist ("
						+ setupDirectory + ")\n";
				if (fileGlobalSetup.exists()) {
					emailError += "The Setup directory is being populated with the data from globalSetupDirectory ("
							+ globalSetupDirectory + ")\n";
					// FileUtils.copyDirectoryToDirectory(fileGlobalSetup,
					// fileSetupPreviousDirectory); (this also copies the own
					// directores
					FileUtilities.copyFolder(fileGlobalSetup, fileSetup);

				} else {
					emailError += "The globalSetupDirectory ("
							+ globalSetupDirectory
							+ ") also doesnt exist!!!! ERROR\n";
				}
				this.getApplication().sendEmail(
						"BIOSAMPLES: ##### INITIALIZATION ERROR #######",
						emailError);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.getApplication().sendEmail(
					"BIOSAMPLES: ##### INITIALIZATION ERROR #######",
					emailError);
			logger.error("Exception:->[{}]", e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void terminate() throws Exception {
		// TODO Auto-generated method stub

	}

}