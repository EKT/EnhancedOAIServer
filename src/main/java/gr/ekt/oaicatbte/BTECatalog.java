package gr.ekt.oaicatbte;

import gr.ekt.bte.core.AbstractFilter;
import gr.ekt.bte.core.DataLoader;
import gr.ekt.bte.core.DataLoadingSpec;
import gr.ekt.bte.core.LinearWorkflow;
import gr.ekt.bte.core.OutputGenerator;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.core.SimpleDataLoadingSpec;
import gr.ekt.bte.core.TransformationEngine;
import gr.ekt.bte.core.TransformationResult;
import gr.ekt.bte.core.TransformationSpec;
import gr.ekt.bte.core.Value;
import gr.ekt.bte.core.Workflow;
import gr.ekt.bte.exceptions.BadTransformationSpec;
import gr.ekt.bte.exceptions.EmptySourceException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

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
import ORG.oclc.oai.server.verb.NoMetadataFormatsException;
import ORG.oclc.oai.server.verb.NoSetHierarchyException;
import ORG.oclc.oai.server.verb.OAIInternalServerError;

public abstract class BTECatalog extends AbstractCatalog {

	final int MAX = 100;
	
	// Define a static logger variable
	static Logger log = Logger.getLogger(BTECatalog.class);

	private ClassPathXmlApplicationContext context = null;

	@Override
	public Vector getSchemaLocations(String identifier)
			throws IdDoesNotExistException, NoMetadataFormatsException,
			OAIInternalServerError {

		DataLoader dataloader = resolveDataLoader();
		SimpleDataLoadingSpec dls = new SimpleDataLoadingSpec();
		dls.setIdentifier(identifier);
		dls.setNumberOfRecords(1);
		
		try {
			RecordSet recordSet = dataloader.getRecords(dls);

			if (recordSet==null || recordSet.getRecords().size()==0){
				throw new IdDoesNotExistException(identifier);
			}

			Record record = recordSet.getRecords().get(0);

			//isDeleted
			List<Value> isDels = record.getValues("isDeleted");
			boolean isDeleted = false;
			if (isDels==null || isDels.size()==0){
				try {
					throw new OAIInternalServerError("Your implementation of BTE Record must return a Value (true or false string values) for \"isDeleted\" field name!");
				} catch (OAIInternalServerError e) {
					e.printStackTrace();
					return null;
				}
			}
			String isDel = isDels.get(0).getAsString();
			isDeleted = new Boolean(isDel);

			if (isDeleted)
			{
				throw new NoMetadataFormatsException();
			}
			else
			{
				Vector v = new Vector();
				Iterator iterator = getCrosswalks().iterator();

				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry)iterator.next();
					CrosswalkItem crosswalkItem = (CrosswalkItem)entry.getValue();
					Crosswalk crosswalk = crosswalkItem.getCrosswalk();
					if (crosswalk.isAvailableFor(record)) {
						v.add(crosswalk.getSchemaLocation());
					}
				}
				
				return v;
			}

		} catch (EmptySourceException e) {
			throw new OAIInternalServerError(e.getMessage());
		}
	}
	
	@Override
	public String getRecord(String identifier, String metadataPrefix)
			throws IdDoesNotExistException, CannotDisseminateFormatException,
			OAIInternalServerError {

		TransformationSpec ts = new TransformationSpec(identifier);
		ts.setNumberOfRecords(1);
		Iterator<String> iter;
		try {
			iter = (Iterator<String>)listRecords(ts, metadataPrefix, false, false, false).get("records");
			if (iter.hasNext())
				return iter.next();
			return null;
		} catch (BadResumptionTokenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info("SHOULD NOT BE HERE!");
		}
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

		try {
			TransformationSpec ts = new TransformationSpec(MAX, offset, set, encodeDate(from), encodeDate(until));
			return listRecords(ts, metadataPrefix, true, true, true);
		} catch (CannotDisseminateFormatException e) {
			e.printStackTrace();
			log.info("SHOULD NOT BE HERE!");
		}catch (ParseException e) {
			throw new OAIInternalServerError(e.getMessage());
		}

		return null;
	}

	@Override
	public Map listIdentifiers(String from, String until, String set,
			String metadataPrefix) throws BadArgumentException,
			CannotDisseminateFormatException, NoItemsMatchException,
			NoSetHierarchyException, OAIInternalServerError {

		try {
			TransformationSpec ts = new TransformationSpec(MAX, 0, set, encodeDate(from), encodeDate(until));
			return listRecords(ts, metadataPrefix, false, true, true);
		} catch (BadResumptionTokenException e) {
			e.printStackTrace();
			log.info("SHOULD NOT BE HERE!");
		}
		catch (ParseException e) {
			throw new OAIInternalServerError(e.getMessage());
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

		try {
			TransformationSpec ts = new TransformationSpec(MAX, 0, set, encodeDate(from), encodeDate(until));
			return listRecords(ts, metadataPrefix, false, false, true);
		} catch (BadResumptionTokenException e) {
			e.printStackTrace();
			log.info("SHOULD NOT BE HERE!");
		}
		catch (ParseException e) {
			throw new OAIInternalServerError(e.getMessage());
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

		try {
			TransformationSpec ts = new TransformationSpec(MAX, offset, set, encodeDate(from), encodeDate(until));;
			return listRecords(ts, metadataPrefix, true, false, true);
		} catch (CannotDisseminateFormatException e) {
			e.printStackTrace();
			log.info("SHOULD NOT BE HERE!");
		}
		catch (ParseException e) {
			throw new OAIInternalServerError(e.getMessage());
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

	private Map listRecords(TransformationSpec transSpec, String metadataPrefix, boolean resumption, boolean onlyHeader, boolean shouldHaveResumptionToken) throws CannotDisseminateFormatException, OAIInternalServerError, BadResumptionTokenException{
		//Instantiate a new BTE
		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				throw new OAIInternalServerError(e.getMessage());
			}
		}

		TransformationEngine te = new TransformationEngine(null, null, null);

		OutputGenerator outputGenerator = resolveOutpuGenerator(metadataPrefix, resumption, onlyHeader);
		List<AbstractFilter> filters = resolveFilters(transSpec.getDataSetName());
		DataLoader dataloader = resolveDataLoader();
		Workflow workflow = resolveWorkflow();

		te.setDataLoader(dataloader);
		te.setOutputGenerator(outputGenerator);

		if (filters!=null){
			for (AbstractFilter filter : filters){
				workflow.addStep(filter);
			}
		}
		te.setWorkflow(workflow);

		try {
			TransformationResult result =  te.transform(transSpec);

			Map<String, Object> returnMap = new HashMap<String, Object>();
			if (onlyHeader)
				returnMap.put("headers", result.getOutput().iterator());
			else
				returnMap.put("records", result.getOutput().iterator());
			HashMap<String, String> resumptionTokenMap = new HashMap<String, String>();
			if (!result.getLastLog().getEndOfInput() && shouldHaveResumptionToken){
				try {
					resumptionTokenMap.put("resumptionToken", decodeDate(transSpec.getFromDate(), null)+"/"+decodeDate(transSpec.getUntilDate(), null)+"/"+(transSpec.getDataSetName()!=null?transSpec.getDataSetName():"")+"/"+metadataPrefix+"/"+(result.getLastLog().getFirstUnexaminedRecord()));
					returnMap.put("resumptionMap", resumptionTokenMap);
				} catch (ParseException e) {
					throw new OAIInternalServerError(e.getMessage());
				}
			}

			return returnMap;

		} catch (BadTransformationSpec e) {	

			throw new OAIInternalServerError(e.getMessage());
		}
	}

	private static Date encodeDate(String t) throws ParseException
	{
		SimpleDateFormat df;

		// Choose the correct date format based on string length
		if (t.length() == 10)
		{
			df = new SimpleDateFormat("yyyy-MM-dd");
		}
		else if (t.length() == 20)
		{
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		}
		else {
			// Not self generated, and not in a guessable format
			throw new ParseException("", 0);
		}

		// Parse the date
		df.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
		return df.parse(t);
	} 

	private static String decodeDate(Date t, String format) throws ParseException
	{
		SimpleDateFormat df = null;

		// Choose the correct date format based on string length
		if (format==null){
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		}

		// Parse the date
		df.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
		return df.format(t);
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

	private DataLoader resolveDataLoader() throws OAIInternalServerError{
		Map<String, DataLoader> dataloaders = context.getBeansOfType(DataLoader.class);
		if (dataloaders==null || dataloaders.size()==0){
			throw new OAIInternalServerError("No dataloader can be found on the configuration!");
		}

		if (dataloaders.size()>1){
			throw new OAIInternalServerError("More than one dataloaders found on the configuration! Cannot decide which one to use!");
		}

		DataLoader dataloader = dataloaders.values().iterator().next();

		return dataloader;
	}

	private Workflow resolveWorkflow() throws OAIInternalServerError{
		Map<String, Workflow> workflows = context.getBeansOfType(Workflow.class);
		if (workflows==null || workflows.size()==0){
			return new LinearWorkflow();
		}

		if (workflows.size()>1){
			throw new OAIInternalServerError("More than one workflows found on the configuration! Cannot decide which one to use!");
		}

		Workflow workflow = workflows.values().iterator().next();

		return workflow;
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

	private List<AbstractFilter> resolveFilters(String set){

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
				List<AbstractFilter> filters = (ArrayList<AbstractFilter>)map.get("filters");
				return filters;
			}
		}

		return null;

	}
}
