package uk.ac.ebi.arrayexpress.components;

import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableDocumentContainer;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableString;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableStringList;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;
import uk.ac.ebi.arrayexpress.utils.saxon.DocumentSource;

import java.io.File;
import java.util.List;

public class Experiments extends ApplicationComponent implements DocumentSource
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String dataSource;
    private TextFilePersistence<PersistableDocumentContainer> experiments;
    private TextFilePersistence<PersistableStringList> experimentsInAtlas;
    private TextFilePersistence<PersistableString> species;
    private TextFilePersistence<PersistableString> arrays;
    private TextFilePersistence<PersistableString> experimentTypes;

    public Experiments()
    {
        super("Experiments");
    }

    public void initialize()
    {
        String tmpDir = System.getProperty("java.io.tmpdir");
        this.experiments = new TextFilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer()
                , new File(tmpDir, getPreferences().getString("ae.experiments.cache.filename"))
        );

        this.experimentsInAtlas = new TextFilePersistence<PersistableStringList>(
                new PersistableStringList()
                , new File(tmpDir, getPreferences().getString("ae.atlasexperiments.cache.filename"))
        );

        this.species = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(tmpDir, getPreferences().getString("ae.species.cache.filename"))

        );

        this.arrays = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(tmpDir, getPreferences().getString("ae.arrays.cache.filename"))
        );

        this.experimentTypes = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(tmpDir, getPreferences().getString("ae.exptypes.cache.filename"))
        );

        indexExperiments();
        ((SaxonEngine)getComponent("SaxonEngine")).registerDocumentSource(this);
    }

    public void terminate()
    {
    }

    // implementation of DocumentSource.getDocument()
    public String getDocumentURI()
    {
        return "experiments.xml";
    }

    // implementation of DocumentSource.getDocument()
    public synchronized DocumentInfo getDocument()
    {
        return this.experiments.getObject().getDocument();
    }

    public boolean isAccessible( String accession, String userId )
    {
        return false;
    }

    public boolean isInAtlas( String accession )
    {
        return this.experimentsInAtlas.getObject().contains(accession);
    }

    public String getSpecies()
    {
        return this.species.getObject().get();
    }

    public String getArrays()
    {
        return this.arrays.getObject().get();
    }

    public String getExperimentTypes()
    {
        return this.experimentTypes.getObject().get();
    }


    public String getDataSource()
    {
        if (null == this.dataSource) {
            this.dataSource = getPreferences().getString("ae.experiments.datasources");
        }

        return this.dataSource;
    }

    public void setDataSource( String dataSource )
    {
        this.dataSource = dataSource;
    }

    public void reload( String xmlString )
    {
        DocumentInfo doc = loadExperimentsFromString(xmlString);
        if (null != doc) {
            setExperiments(doc);
            buildSpeciesArraysExpTypes(doc);
            indexExperiments();
        }
    }

    public void setExperimentsInAtlas( List<String> expList )
    {
        this.experimentsInAtlas.setObject(new PersistableStringList(expList));
    }

    private synchronized void setExperiments( DocumentInfo doc )
    {
        if (null != doc) {
            this.experiments.setObject(new PersistableDocumentContainer(doc));
        } else {
            this.logger.error("Experiments NOT updated, NULL document passed");
        }
    }

    private DocumentInfo loadExperimentsFromString( String xmlString )
    {
        DocumentInfo doc = ((SaxonEngine)getComponent("SaxonEngine")).transform(xmlString, "preprocess-experiments-xml.xsl", null);
        if (null == doc) {
            this.logger.error("Transformation [preprocess-experiments-xml.xsl] returned an error, returning null");
            return null;
        }
        return doc;
    }

    private void indexExperiments()
    {
        try {
            ((SearchEngine)getComponent("SearchEngine")).getController().index("experiments", experiments.getObject().getDocument());
            //List<String> expDesign = Controller.getInstance().getTerms("experiments", "expdesign");
            //logger.debug("Retrieved experiment design list, size [{}]", expDesign.size());
        } catch (Throwable x) {
            this.logger.error("Caught an exception:", x);
        }
    }

    private void buildSpeciesArraysExpTypes( DocumentInfo doc )
    {
        String speciesString = ((SaxonEngine)getComponent("SaxonEngine")).transformToString(doc, "build-species-list-html.xsl", null);
        this.species.setObject(new PersistableString(speciesString));

        String arraysString = ((SaxonEngine)getComponent("SaxonEngine")).transformToString(doc, "build-arrays-list-html.xsl", null);
        this.arrays.setObject(new PersistableString(arraysString));

        String expTypesString = ((SaxonEngine)getComponent("SaxonEngine")).transformToString(doc, "build-exptypes-list-html.xsl", null);
        this.experimentTypes.setObject(new PersistableString(expTypesString));
    }
}
