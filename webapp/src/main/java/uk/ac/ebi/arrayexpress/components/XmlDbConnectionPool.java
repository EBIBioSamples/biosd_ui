package uk.ac.ebi.arrayexpress.components;

import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
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

public class XmlDbConnectionPool extends ApplicationComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String driverXml;
	private String connectionString;
	private Database db;
	private Collection coll;
    
    public void initialize() throws Exception
    {
    	HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
				.getInstance().getPreferences().getConfSubset("bs.xmldatabase");

		if (null != connsConf) {
			driverXml = connsConf.getString("driver");
//			connectionString = connsConf.getString("connectionstring");
			connectionString = connsConf.getString("base") + "://" + connsConf.getString("host") + ":" + connsConf.getString("port") + "/" + connsConf.getString("dbname");
			logger.debug("connectionString->"+connectionString);
		} else {
			logger.error("bs.xmldatabase Configuration is missing!!");
		}

		Class<?> c;
		try {
			
			//TODO: rpe: review this (DB open files)
			if(coll!=null){
				coll.close();
				db=null;
			}
			
			c = Class.forName(driverXml);

			// Class<?> c = Class.forName("org.exist.xmldb.DatabaseImpl");
			db = (Database) c.newInstance();
			DatabaseManager.registerDatabase(db);
			coll = DatabaseManager.getCollection(connectionString);

		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			logger.error("Exception:->[{}]", e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("Exception:->[{}]", e.getMessage());
			e.printStackTrace();
		}
		// Receive the database
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			logger.error("Exception:->[{}]", e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.error("Exception:->[{}]", e.getMessage());
			e.printStackTrace();
		}
    }

    public void terminate() throws Exception
    {
    	coll.close();
    }

    public Collection getCollection(  ) throws Exception
    {
	        return coll;
    }

    
    
    public String getDBInfo(String dbHost, int dbPort, String dbPassword, String originalDbName){
		String ret = "";
		try {

			logger.debug("* Create a client session int the Xml Database to get information about it.");


			Session session = new ClientSession(dbHost,
					dbPort, "admin", dbPassword);

			// ------------------------------------------------------------------------
			// Create a database
			logger.debug("* open database.");
			String logs = "";

			logs = session.execute("open " + originalDbName);
			logger.debug("open ...->" + logs);
			ret = session.execute("info db ");
			// logger.debug("info db ...->" + logs);
			logs = session.execute("close");
			logger.debug("close ...->" + logs);

			logger.debug("* Close the client session.");
			session.close();
			
			ret="<table><tr><td valign='top'><u>XmlDatabase</u></td><td>" + ret + "</td></tr></table>";
		}

		catch (Exception ex) {
			// Handle exceptions
			ret = "ERROR";
			logger.error("Exception:->[{}]", ex.getMessage());
			ex.printStackTrace();
		} finally {

		}
		return ret;
    	
    }   	
    
    public String getDBInfo(){
		String ret = "";
		try {

			logger.debug("* Create a client session int the Xml Database to get information about it.");

			String dbHost = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.host");
			String dbPort = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.port");
			String dbPassword = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.adminpassword");

			String originalDbName = Application.getInstance().getPreferences()
					.getString("bs.xmldatabase.dbname");
			ret=getDBInfo(dbHost, Integer.parseInt(dbPort), dbPassword, originalDbName);
		}

		catch (Exception ex) {
			// Handle exceptions
			ret = "ERROR - The BaseXServer is not available";
			logger.error("Exception:->[{}]", ex.getMessage());
			ex.printStackTrace();
		} finally {

		}
		return ret;
    	
    	
    }
    
    
    @Override
    public String getMetaDataInformation() {
    	return getDBInfo();
	}

	

}