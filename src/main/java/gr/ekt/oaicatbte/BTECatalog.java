package gr.ekt.oaicatbte;

import gr.ekt.transformationengine.core.Filter;
import gr.ekt.transformationengine.core.OutputGenerator;
import gr.ekt.transformationengine.core.TransformationEngine;
import gr.ekt.transformationengine.exceptions.UnimplementedAbstractMethod;
import gr.ekt.transformationengine.exceptions.UnknownClassifierException;
import gr.ekt.transformationengine.exceptions.UnknownInputFileType;
import gr.ekt.transformationengine.exceptions.UnsupportedComparatorMode;
import gr.ekt.transformationengine.exceptions.UnsupportedCriterion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.crosswalk.CrosswalkItem;
import ORG.oclc.oai.server.crosswalk.Crosswalks;
import ORG.oclc.oai.server.verb.BadArgumentException;
import ORG.oclc.oai.server.verb.BadResumptionTokenException;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import ORG.oclc.oai.server.verb.IdDoesNotExistException;
import ORG.oclc.oai.server.verb.NoItemsMatchException;
import ORG.oclc.oai.server.verb.NoSetHierarchyException;
import ORG.oclc.oai.server.verb.OAIInternalServerError;

public abstract class BTECatalog extends AbstractCatalog {

	// Define a static logger variable
	static Logger log = Logger.getLogger(BTECatalog.class);

	private ClassPathXmlApplicationContext context = null;

	//private OutputGenerator userOutputGenerator;

	@Override
	public String getRecord(String arg0, String arg1)
			throws IdDoesNotExistException, CannotDisseminateFormatException,
			OAIInternalServerError {

		return null;
	}

	@Override
	public Map listIdentifiers(String resumptionToken) throws BadResumptionTokenException,
	OAIInternalServerError {
		Object[] parts = decodeResumptionToken(resumptionToken);
		String from = (String)parts[0];
		String until = (String)parts[1];
		String set = (String)parts[2];
		String metadataPrefix = (String)parts[3];
		int offset = (Integer)parts[4];

		OAIDataLoadingSpec ls = new OAIDataLoadingSpec(set, 100, offset, from, until);

		try {
			return listRecords(ls, metadataPrefix, true, true);
		} catch (CannotDisseminateFormatException e) {
			e.printStackTrace();
			log.info("SHOULD NOT BE HERE!");
		}
		return null;
	}

	@Override
	public Map listIdentifiers(String from, String until, String set,
			String metadataPrefix) throws BadArgumentException,
			CannotDisseminateFormatException, NoItemsMatchException,
			NoSetHierarchyException, OAIInternalServerError {
		OAIDataLoadingSpec ls = new OAIDataLoadingSpec(set, 100, 0, from, until);

		try {
			return listRecords(ls, metadataPrefix, false, true);
		} catch (BadResumptionTokenException e) {
			e.printStackTrace();
			log.info("SHOULD NOT BE HERE!");
		}
		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public Crosswalks getCrosswalks() {
		Map crosswalksMap = new HashMap();

		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				return null;
			}
		}
		HashMap<String, Crosswalk> crosswalks = (HashMap<String, Crosswalk>)context.getBean("crosswalks");

		for (String schemaLabel : crosswalks.keySet()){
			Crosswalk crosswalk = crosswalks.get(schemaLabel);
			CrosswalkItem crosswalkItem = new CrosswalkItem(schemaLabel, crosswalk.getSchemaURL(), crosswalk.getNamespaceURL(), crosswalk);
			crosswalksMap.put(schemaLabel, crosswalkItem);
		}

		return new Crosswalks(crosswalksMap);
	}

	@Override
	public Map listRecords(String from, String until, String set,
			String metadataPrefix) throws BadArgumentException,
			CannotDisseminateFormatException, NoItemsMatchException,
			NoSetHierarchyException, OAIInternalServerError {

		OAIDataLoadingSpec ls = new OAIDataLoadingSpec(set, 100, 0, from, until);

		try {
			return listRecords(ls, metadataPrefix, false, false);
		} catch (BadResumptionTokenException e) {
			e.printStackTrace();
			log.info("SHOULD NOT BE HERE!");
		}
		return null;
	}

	@Override
	public Map listRecords(String resumptionToken)
			throws BadResumptionTokenException, OAIInternalServerError {

		Object[] parts = decodeResumptionToken(resumptionToken);
		String from = (String)parts[0];
		String until = (String)parts[1];
		String set = (String)parts[2];
		String metadataPrefix = (String)parts[3];
		int offset = (Integer)parts[4];

		OAIDataLoadingSpec ls = new OAIDataLoadingSpec(set, 100, offset, from, until);

		try {
			return listRecords(ls, metadataPrefix, true, false);
		} catch (CannotDisseminateFormatException e) {
			e.printStackTrace();
			log.info("SHOULD NOT BE HERE!");
		}
		return null;
	}

	@Override
	public Map listSets() throws NoSetHierarchyException,
	OAIInternalServerError {

		ArrayList<String> resultSets = new ArrayList<String>();

		//Return here all virtual sets
		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				throw new OAIInternalServerError(e.getMessage());
			}
		}
		List<HashMap<String, Object>> virtualSets = (List<HashMap<String, Object>>)context.getBean("virtual-sets");
		StringBuffer specB = null;
		for (HashMap<String, Object> set : virtualSets){
			String name = (String)set.get("name");
			String setSpec = (String)set.get("setSpec");

			specB = new StringBuffer("<set><setSpec>");
			specB.append(setSpec);
			specB.append("</setSpec>");
			specB.append("<setName>");
			specB.append(name);
			specB.append("</setName>");
			specB.append("</set>");

			resultSets.add(specB.toString());
		}

		//Get all the normal sets fron the extenders
		Map<String, String> sets = listAllSets();
		if (sets!=null){
			StringBuffer spec = null;
			for (String setSpec : sets.keySet()){
				String name = sets.get(setSpec);

				spec = new StringBuffer("<set><setSpec>");
				spec.append(setSpec);
				spec.append("</setSpec>");
				spec.append("<setName>");
				spec.append(name);
				spec.append("</setName>");
				spec.append("</set>");

				resultSets.add(spec.toString());
			}
		}

		Map results = new HashMap();
		results.put("sets", resultSets.iterator());

		for (String s : resultSets){
			System.out.println(s);
		}

		return results;
	}

	/**
	 * Not yet supported - We aren't supposed to get a list sets request with resumption token... are we?
	 */
	@Override
	public Map listSets(String arg0) throws BadResumptionTokenException,
	OAIInternalServerError {
		return null;
	}

	/**
	 * Abstract class - needs to be implemented by the extenders...
	 * @return
	 */
	public abstract Map<String, String> listAllSets() throws NoSetHierarchyException, OAIInternalServerError;

	private Map listRecords(OAIDataLoadingSpec dataLoadingSpec, String metadataPrefix, boolean resumption, boolean onlyHeader) throws CannotDisseminateFormatException, OAIInternalServerError, BadResumptionTokenException{
		//Instantiate a new BTE
		//if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				throw new OAIInternalServerError(e.getMessage());
			}
		//}

		Object teObject = context.getBean("transformationEngine");
		if (teObject==null || !(teObject instanceof TransformationEngine))
			throw new OAIInternalServerError("Could not find the transformation engine!");
		TransformationEngine te = (TransformationEngine)teObject;

		OutputGenerator outputGenerator = resolveOutpuGenerator(metadataPrefix, resumption, onlyHeader);
		List<Filter> filters = resolveFilters(dataLoadingSpec.getSet());

		te.getDataLoader().setLoadingSpec(dataLoadingSpec);
		te.setOutputGenerator(outputGenerator);

		if (filters!=null){
			for (Filter filter : filters){
				te.getWorkflow().addStep(filter);
			}
		}
		
		try {
			List<String> results =  te.transform();

			Map<String, Object> returnMap = new HashMap<String, Object>();
			if (onlyHeader)
				returnMap.put("headers", results.iterator());
			else
				returnMap.put("records", results.iterator());
			HashMap<String, String> resumptionTokenMap = new HashMap<String, String>();
			resumptionTokenMap.put("resumptionToken", dataLoadingSpec.getFrom()+"/"+dataLoadingSpec.getUntil()+"/"+(dataLoadingSpec.getSet()!=null?dataLoadingSpec.getSet():"")+"/"+metadataPrefix+"/"+(dataLoadingSpec.getOffset()+dataLoadingSpec.getMax()));

			returnMap.put("resumptionMap", resumptionTokenMap);
			log.info("Transformation Engine ended");

			return returnMap;

		} catch (UnknownClassifierException e) {
			throw new OAIInternalServerError(e.getMessage());
		} catch (UnknownInputFileType e) {
			throw new OAIInternalServerError(e.getMessage());
		} catch (UnimplementedAbstractMethod e) {
			throw new OAIInternalServerError(e.getMessage());
		} catch (UnsupportedComparatorMode e) {
			throw new OAIInternalServerError(e.getMessage());
		} catch (UnsupportedCriterion e) {
			throw new OAIInternalServerError(e.getMessage());
		}
	}

	private Object[] decodeResumptionToken(String token)
			throws BadResumptionTokenException
			{
		Object[] obj = new Object[5];
		StringTokenizer st = new StringTokenizer(token, "/", true);

		try
		{
			// Extract from, until, set, prefix
			for (int i = 0; i < 4; i++)
			{
				if (!st.hasMoreTokens())
				{
					throw new BadResumptionTokenException();
				}

				String s = st.nextToken();

				// If this value is a delimiter /, we have no value for this
				// part
				// of the resumption token.
				if (s.equals("/"))
				{
					obj[i] = null;
				}
				else
				{
					obj[i] = s;

					// Skip the delimiter
					st.nextToken();
				}

				log.debug("is: " + (String) obj[i]);
			}

			if (!st.hasMoreTokens())
			{
				throw new BadResumptionTokenException();
			}

			obj[4] = new Integer(st.nextToken());
		}
		catch (NumberFormatException nfe)
		{
			throw new BadResumptionTokenException();
		}
		catch (NoSuchElementException nsee)
		{
			throw new BadResumptionTokenException();
		}

		return obj;
			}

	private OutputGenerator resolveOutpuGenerator(String metadataPrefix, boolean resumption, boolean onlyHeader) throws CannotDisseminateFormatException, OAIInternalServerError, BadResumptionTokenException{

		/*try {
			Map<String, OutputGenerator> outputGenerators = context.getBeansOfType(OutputGenerator.class);
			userOutputGenerator = 
			return userOutputGenerator;
		} catch (BeansException e) {

		}*/

		OAIOutputGenerator userOutputGenerator = new OAIOutputGenerator();
		userOutputGenerator.setOnlyHeader(onlyHeader);

		Crosswalks crosswalks = getCrosswalks();
		if (!crosswalks.containsValue(metadataPrefix)){
			if (resumption)
				throw new BadResumptionTokenException();
			else 
				throw new CannotDisseminateFormatException(metadataPrefix);
		}
		Iterator crosswalksIterator = crosswalks.iterator();

		while (crosswalksIterator.hasNext()){
			Map.Entry<String, CrosswalkItem> entry = (Map.Entry<String, CrosswalkItem>) crosswalksIterator.next();
			String schema = entry.getKey();
			Crosswalk crosswalk = entry.getValue().getCrosswalk();

			if (schema.equals(metadataPrefix)){
				((OAIOutputGenerator)userOutputGenerator).setCrosswalk(crosswalk);
				break;
			}
		}

		return userOutputGenerator;
	}

	private List<Filter> resolveFilters(String set){
		
		if (set==null)
			return null;
		
		//Return here all virtual sets
		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				return null;
			}
		}
		List<HashMap<String, Object>> virtualSets = (List<HashMap<String, Object>>)context.getBean("virtual-sets");
		
		for (HashMap<String, Object> map: virtualSets){
			String setSpec = (String) map.get("setSpec");
			if (setSpec.equals(set)){
				List<Filter> filters = (ArrayList<Filter>)map.get("filters");
				return filters;
			}
		}
		
		return null;
		
	}

	/*public OutputGenerator getUserOutputGenerator() {
		return userOutputGenerator;
	}

	public void setUserOutputGenerator(OutputGenerator userOutputGenerator) {
		this.userOutputGenerator = userOutputGenerator;
	}*/



}
