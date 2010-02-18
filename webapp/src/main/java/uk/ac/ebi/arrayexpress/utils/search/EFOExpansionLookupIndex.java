package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class EFOExpansionLookupIndex implements IEFOExpansionLookup
    {
        // logging machinery
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private String indexLocation;
        private Directory indexDirectory;

        public EFOExpansionLookupIndex( String indexLocation )
        {
            this.indexLocation = indexLocation;
        }

        public void addMaps( Map<String, Set<String>> synonymMap, Map<String, Set<String>> efoMap )
        {
            // 1. create a joint set of keys from both maps
            Set<String> allTerms = new HashSet<String>();
            allTerms.addAll(synonymMap.keySet());
            allTerms.addAll(efoMap.keySet());

            // 2. iterate over the set
            try {
                this.indexDirectory = FSDirectory.open(new File(this.indexLocation));
                IndexWriter w = createIndex(this.indexDirectory, new LowercaseAnalyzer());

                for (String term : allTerms) {
                    Document d = new Document();

                    boolean hasJustAddedSomething = false;

                    if (synonymMap.containsKey(term)) {
                        Set<String> syns = synonymMap.get(term);
                        for (String syn : syns) {
                            if (allTerms.contains(syn)) {
                                this.logger.warn("Synonym [{}] for term [{}] is present as a different term itelf, skipping", syn, term);
                            } else {
                                addIndexField(d, "term", syn, true, true);
                                hasJustAddedSomething = true;
                            }
                        }
                    }

                    if (efoMap.containsKey(term)) {
                        Set<String> efoTerms = efoMap.get(term);
                        for (String efoTerm : efoTerms) {
                            addIndexField(d, "efo", efoTerm, false, true);
                            hasJustAddedSomething = true;
                        }
                    }

                    if (hasJustAddedSomething) {
                        addIndexField(d, "term", term, true, true);
                        addIndexDocument(w, d);
                    } else {
                        this.logger.warn("Data for term [{}] wasn't included as there were no synonyms or child terms found", term);
                    }
                }
                commitIndex(w);

            } catch (Exception x) {
                logger.error("Caught an exception:", x);
            }
        }

        public List<Set<String>> getExpansionTerms( Query origQuery )
        {
            List<Set<String>> expansion = new ArrayList<Set<String>>(2);

            try {
                IndexReader ir = IndexReader.open(this.indexDirectory, true);

                // to show _all_ available nodes
                IndexSearcher isearcher = new IndexSearcher(ir);
                Query q = overrideQueryField(origQuery, "term");
                logger.debug("Looking up synonyms for query [{}]", q.toString());

                TopDocs hits = isearcher.search(q, 128); // todo: wtf is this hardcoded?
                logger.debug("Query returned [{}] hits", hits.totalHits);

                for (ScoreDoc d : hits.scoreDocs) {
                    Document doc = isearcher.doc(d.doc);
                    String[] terms = doc.getValues("term");
                    String[] efo = doc.getValues("efo");
                    logger.debug("Synonyms [{}], EFO Terms [{}]", StringUtils.join(terms, ", "), StringUtils.join(efo, ", "));
                }

                isearcher.close();
                ir.close();
            } catch (Exception x) {
                logger.error("Caught an exception:", x);
            }

            return expansion;
        }



        private IndexWriter createIndex( Directory indexDirectory, Analyzer analyzer )
        {
            IndexWriter iwriter = null;
            try {
                iwriter = new IndexWriter(indexDirectory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
            } catch (Exception x) {
                this.logger.error("Caught an exception:", x);
            }

            return iwriter;
        }

        private void addIndexField( Document document, String name, String value, boolean shouldAnalyze, boolean shouldStore )
        {
            document.add(
                    new Field(
                            name
                            , value
                            , shouldStore ? Field.Store.YES : Field.Store.NO
                            , shouldAnalyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED
                    )
            );
        }

        private void addIndexDocument( IndexWriter iwriter, Document document )
        {
            try {
                iwriter.addDocument(document);
            } catch (Exception x) {
                this.logger.error("Caught an exception:", x);
            }
        }

        private void commitIndex( IndexWriter iwriter )
        {
            try {
                iwriter.optimize();
                iwriter.commit();
                iwriter.close();
            } catch (Exception x) {
                this.logger.error("Caught an exception:", x);
            }
        }

        private Query overrideQueryField( Query origQuery, String fieldName )
        {
            Query query = new TermQuery(new Term(""));

            try {
                if (origQuery instanceof PrefixQuery) {
                    Term term = ((PrefixQuery)origQuery).getPrefix();
                    query = new PrefixQuery(new Term(fieldName, term.text()));
                } else if (origQuery instanceof WildcardQuery) {
                    Term term = ((WildcardQuery)origQuery).getTerm();
                    query = new WildcardQuery(new Term(fieldName, term.text()));
                } else if (origQuery instanceof TermRangeQuery) {
                    TermRangeQuery trq = (TermRangeQuery)origQuery;
                    query = new TermRangeQuery(fieldName, trq.getLowerTerm(), trq.getUpperTerm(), trq.includesLower(), trq.includesUpper());
                } else if (origQuery instanceof FuzzyQuery) {
                    Term term = ((FuzzyQuery)origQuery).getTerm();
                    query = new FuzzyQuery(new Term(fieldName, term.text()));
                } else if (origQuery instanceof TermQuery) {
                    Term term = ((TermQuery)origQuery).getTerm();
                    query = new TermQuery(new Term(fieldName, term.text()));
                } else if (origQuery instanceof PhraseQuery) {
                    Term[] terms = ((PhraseQuery)origQuery).getTerms();
                    StringBuilder text = new StringBuilder();
                    for (Term t : terms) {
                        text.append(t.text()).append(' ');
                    }
                    query = new TermQuery(new Term(fieldName, text.toString().trim()));

                } else {
                    logger.error("Unsupported query type [{}]", origQuery.getClass().getCanonicalName());
                }
            } catch (Exception x) {
                logger.error("Caught an exception:", x);
            }


            return query;
        }
    }