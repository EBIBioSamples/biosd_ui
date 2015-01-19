/**
 * 
 */
package uk.ac.ebi.arrayexpress.jobs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress.app.Application;

/**
 * @author rpslpereira
 * 
 */
public class DownloadBiosamplesXmlFileFromFile implements IDownloadBiosamplesXmlFile {



	private final Logger logger = LoggerFactory.getLogger(getClass());


	public boolean downloadXml(String downloadDirectory) throws Exception {
		
		String url = Application.getInstance().getPreferences()
				.getString("bs.xmlupdate.url");
		logger.info("Download file from ->"
				+ url);
		boolean ok = false;
		try {
			File sourceFile=new File(url);
			File destinationDirectory=new File(downloadDirectory);
			FileUtils.copyFileToDirectory(sourceFile, destinationDirectory);
			ok = true;
		} catch (Exception e) {
			logger.error("ERROR on copying file: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
			// return false;
		} 
		return ok;
	}

}
