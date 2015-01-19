package uk.ac.ebi.arrayexpress.components;

import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.autocompletion.AutocompleteData;
import uk.ac.ebi.arrayexpress.utils.autocompletion.AutocompleteStore;
import uk.ac.ebi.arrayexpress.utils.efo.EFONode;
import uk.ac.ebi.arrayexpress.utils.efo.IEFO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class Autocompletion extends ApplicationComponent {
	private Map<String, AutocompleteStore> autocompleteStore;

	private BioSamplesGroup bioSamplesGroup;
	private BioSamplesSample bioSamplesSample;
	private SearchEngine search;
	private IEFO efo;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public Autocompletion() {
	}

	public void initialize() throws Exception {

		// TODO:The EFO is repeated in each index (I dont know if it will works
		// so, for now, I will do it in a easiest way)
		this.autocompleteStore = new HashMap<String, AutocompleteStore>();

		this.bioSamplesGroup = (BioSamplesGroup) getComponent("BioSamplesGroup");
		this.bioSamplesSample = (BioSamplesSample) getComponent("BioSamplesSample");
		this.search = (SearchEngine) getComponent("SearchEngine");

		this.autocompleteStore.put(bioSamplesGroup.INDEX_ID,
				new AutocompleteStore());
		this.autocompleteStore.put(bioSamplesSample.INDEX_ID,
				new AutocompleteStore());

	}

	public void terminate() throws Exception {
	}

	private IEFO getEfo() {
		return this.efo;
	}

	public void setEfo(IEFO efo) {
		this.efo = efo;
	}

	public String getKeywords(String IndexId, String prefix, String field,
			Integer limit) {
		StringBuilder sb = new StringBuilder("");
		List<AutocompleteData> matches = getStore(IndexId).findCompletions(
				prefix, field, limit);
		for (AutocompleteData match : matches) {
			sb.append(match.getText()).append('|').append(match.getDataType())
					.append('|').append(match.getData()).append('\n');
		}
		return sb.toString();
	}

	public String getEfoChildren(String efoId) {
		StringBuilder sb = new StringBuilder();

		if (null != getEfo()) {
			EFONode node = getEfo().getMap().get(efoId);
			if (null != node) {
				Set<EFONode> children = node.getChildren();
				if (null != children) {
					for (EFONode child : children) {
					    //only report children of enough length
					    //this reduces noise in the expansion from collisions
					    if (child.getTerm().length() >= 5) {
    						sb.append(child.getTerm()).append("|o|");
    						if (child.hasChildren()) {
    							sb.append(child.getId());
    						}
    						sb.append("\n");
					    }
					}
				}
			}
		}
		return sb.toString();
	}

	// TODO: this in being called associated to BioSampleGroup, but now is
	// relates also to BioSamplesSample - I need to change this
	public void rebuild() throws IOException {
		rebuildBioSamplesGroup();
		rebuildBioSamplesSample();
	}

	public void rebuildBioSamplesGroup() throws IOException {
		if (getStore(bioSamplesGroup.INDEX_ID) != null) {
			getStore(bioSamplesGroup.INDEX_ID).clear();
		}

		Set<String> fields = search.getController().getFieldNames(
				bioSamplesGroup.INDEX_ID);
		for (String field : fields) {
			String fieldTitle = search.getController().getFieldTitle(
					bioSamplesGroup.INDEX_ID, field);
			if (!field.endsWith("sort") && null != fieldTitle && fieldTitle.length() > 0) {
				getStore(bioSamplesGroup.INDEX_ID).addData(
						new AutocompleteData(field,
								AutocompleteData.DATA_FIELD, fieldTitle));
			}
			String fieldType = search.getController().getFieldType(
					bioSamplesGroup.INDEX_ID, field);
			if (null != fieldType
					&& !"integer".equals(fieldType)
					&& search.getController().isFieldAutoCompletion(
							bioSamplesGroup.INDEX_ID, field)) {
				for (String term : search.getController().getTerms(
						bioSamplesGroup.INDEX_ID, field,
						"keywords".equals(field) ? 10 : 1)) {
					getStore(bioSamplesGroup.INDEX_ID).addData(
							new AutocompleteData(term,
									AutocompleteData.DATA_TEXT, field));
				}
			}
		}
		// TODO:The EFO is repeated in each index
		if (null != getEfo()) {
			addEfoNodeWithDescendants(bioSamplesGroup.INDEX_ID, IEFO.ROOT_ID);
		}
	}

	public void rebuildBioSamplesSample() throws IOException {
		logger.debug("Autocompletion-> BioSamplesSample()");
		if (getStore(bioSamplesSample.INDEX_ID) != null) {
			getStore(bioSamplesSample.INDEX_ID).clear();
		}
		Set<String> fields = search.getController().getFieldNames(
				bioSamplesSample.INDEX_ID);
		for (String field : fields) {
			String fieldTitle = search.getController().getFieldTitle(
					bioSamplesSample.INDEX_ID, field);
			if (!field.endsWith("sort") && null != fieldTitle && fieldTitle.length() > 0 ) {
				getStore(bioSamplesSample.INDEX_ID).addData(
						new AutocompleteData(field,
								AutocompleteData.DATA_FIELD, fieldTitle));
				//System.out.println("fiels->" +field+"-->fieldTitle" + fieldTitle );
			}
			String fieldType = search.getController().getFieldType(
					bioSamplesSample.INDEX_ID, field);
			if (null != fieldType
					&& !"integer".equals(fieldType)
					&& search.getController().isFieldAutoCompletion(
							bioSamplesSample.INDEX_ID, field)) {
				for (String term : search.getController().getTerms(
						bioSamplesSample.INDEX_ID, field,
						"keywords".equals(field) ? 10 : 1)) {
					getStore(bioSamplesSample.INDEX_ID).addData(
							new AutocompleteData(term,
									AutocompleteData.DATA_TEXT, field));
					//System.out.println("add->" +field+"-->" + term );
				}
			}
		}
		//I need to get all the different attributeNames

//		for (String term : search.getController().getTerms(
//				bioSamplesSample.INDEX_ID, "attributeNames",1)){
//			String newField="attribute<"+term+">";
//			getStore(bioSamplesSample.INDEX_ID).addData(
//					new AutocompleteData(newField,
//							AutocompleteData.DATA_FIELD, ""));
//			System.out.println("nem att:->" + newField);
//			
//		}
		
		
//		// System.out.println("autocompletion rebuild");
//		Iterator<String> itr = search.getController()
//				.getFields(bioSamplesSample.INDEX_ID).iterator();
//		while (itr.hasNext()) {
//			String field = itr.next();
//			//If exists on the configuration I Will igone it (it was added previously)
//			if (!field.endsWith("sort") && !search.getController().doesFieldExist(bioSamplesSample.INDEX_ID, field)) {
//				getStore(bioSamplesSample.INDEX_ID).addData(
//						new AutocompleteData(field,
//								AutocompleteData.DATA_FIELD, ""));
//				
//				for (String term : search.getController().getTerms(
//						bioSamplesSample.INDEX_ID, field,
//						"keywords".equals(field) ? 10 : 1)) {
//					getStore(bioSamplesSample.INDEX_ID).addData(
//							new AutocompleteData(term,
//									AutocompleteData.DATA_TEXT, field));
//					//System.out.println("add->" +field+"-->" + term );
//				}
//				//System.out.println("fieldName->" + field);
//			}
//		}

		// TODO:The EFO is repeated in each index
		if (null != getEfo()) {
			addEfoNodeWithDescendants(bioSamplesSample.INDEX_ID, IEFO.ROOT_ID);
		}
	}

	private void addEfoNodeWithDescendants(String indexId, String nodeId) {
		EFONode node = getEfo().getMap().get(nodeId);
		// make node expandable only if it has children and not organizational
		// class
		if (null != node) {
			getStore(indexId).addData(
					new AutocompleteData(node.getTerm(),
							AutocompleteData.DATA_EFO_NODE, node.hasChildren()
									&& !node.isOrganizationalClass() ? node
									.getId() : ""));

			if (node.hasChildren()) {
				for (EFONode child : node.getChildren()) {
					addEfoNodeWithDescendants(indexId, child.getId());
				}
			}
		}
	}

	private AutocompleteStore getStore(String indexId) {
		return this.autocompleteStore.get(indexId);
	}
}
