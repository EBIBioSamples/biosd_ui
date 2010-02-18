package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.RegExpHelper;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableDocumentContainer;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;
import uk.ac.ebi.arrayexpress.utils.saxon.DocumentSource;

import java.io.File;

public class Files extends ApplicationComponent implements DocumentSource
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String rootFolder;
    private TextFilePersistence<PersistableDocumentContainer> files;
    private SaxonEngine saxon;

    public Files()
    {
        super("Files");
    }

    public void initialize()
    {
        saxon = (SaxonEngine)getComponent("SaxonEngine");

        files = new TextFilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer(),
                new File(getPreferences().getString("ae.files.persistence.file.location"))
        );
        
        saxon.registerDocumentSource(this);
    }

    public void terminate()
    {
        saxon = null;
    }

    // implementation of DocumentSource.getDocument()
    public String getDocumentURI()
    {
        return "files.xml";
    }

    // implementation of DocumentSource.getDocument()
    public synchronized DocumentInfo getDocument()
    {
        return this.files.getObject().getDocument();
    }

    private synchronized void setFiles( DocumentInfo doc )
    {
        if (null != doc) {
            this.files.setObject(new PersistableDocumentContainer(doc));
        } else {
            this.logger.error("Files NOT updated, NULL document passed");
        }
    }

    public void reload( String xmlString )
    {
        DocumentInfo doc = loadFilesFromString(xmlString);
        if (null != doc) {
            setFiles(doc);
        }
    }

    private DocumentInfo loadFilesFromString( String xmlString )
    {
        DocumentInfo doc = saxon.transform(xmlString, "preprocess-files-xml.xsl", null);
        if (null == doc) {
            this.logger.error("Transformation [preprocess-files-xml.xsl] returned an error, returning null");
            return null;
        }
        return doc;
    }

    public synchronized void setRootFolder( String folder )
    {
        if (null != folder && 0 < folder.length()) {
            if (folder.endsWith(File.separator)) {
                rootFolder = folder;
            } else {
                rootFolder = folder + File.separator;
            }
        } else {
            logger.error("setRootFolder called with null or empty parameter, expect probilems down the road");
        }
    }

    public String getRootFolder()
    {
        if (null == rootFolder) {
            rootFolder = getPreferences().getString("ae.files.root.location");
        }
        return rootFolder;
    }

    // returns true is file is registered in the registry
    public boolean doesExist( String accession, String name )
    {
        if (null != accession && accession.length() > 0) {
            return Boolean.parseBoolean(
                    saxon.evaluateXPathSingle(
                            getDocument()
                            , "exists(//folder[@accession = '" + accession + "']/file[@name = '" + name + "'])"
                    )
            );
        } else {
            return Boolean.parseBoolean(
                    saxon.evaluateXPathSingle(
                            getDocument()
                            , "exists(//file[@name = '" + name + "'])"
                    )
            );
        }
    }

    // returns absolute file location (if file exists, null otherwise) in local filesystem
    public String getLocation( String accession, String name )
    {
        String folderLocation;

        if (null != accession && accession.length() > 0) {
            folderLocation = saxon.evaluateXPathSingle(
                    getDocument()
                    , "//folder[@accession = '" + accession + "' and file/@name = '" + name + "']/@location"
            );
        } else {
            folderLocation = saxon.evaluateXPathSingle(
                    getDocument()
                    , "//folder[file/@name = '" + name + "']/@location"
            );
        }

        if (null != folderLocation && folderLocation.length() > 0) {
            return folderLocation + File.separator + name;
        } else {
            return null;
        }
    }

    public String getAccession( String fileLocation )
    {
        String[] nameFolder = new RegExpHelper("^(.+)/([^/]+)$", "i")
                .match(fileLocation);
        if (null == nameFolder || 2 != nameFolder.length) {
            logger.error("Unable to parse the location [{}]", fileLocation);
            return null;
        }

        return saxon.evaluateXPathSingle(
                getDocument()
                , "//folder[file/@name = '" + nameFolder[1] + "' and @location = '" + nameFolder[0] + "']/@accession"
            );
    }
}