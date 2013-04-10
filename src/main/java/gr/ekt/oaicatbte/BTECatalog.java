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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
		
		HashMap<String, HashMap<String, String>> resolved = resolveXSLTs();
		if (resolved!=null){
			for (String prefix : resolved.keySet()){
				HashMap<String, String> schemaDefList = resolved.get(prefix);
				Crosswalk crosswalk =  new EmptyCrosswalk(schemaDefList.get("namespace")+" "+schemaDefList.get("schema"));
				CrosswalkItem crosswalkItem = new CrosswalkItem(prefix, crosswalk.getSchemaURL(), crosswalk.getNamespaceURL(), crosswalk);
				crosswalksMap.put(prefix, crosswalkItem);
			}
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
				workflow.addStepBefore(filter);
			}
		}
		te.setWorkflow(workflow);

		try {

			TransformationResult result =  te.transform(transSpec);
			List<String> tempResults = result.getOutput();
			//TransformationResult result = new TransformationResult(null, null);
			//List<String> tempResults = new ArrayList<String>();

			Map<String, Object> returnMap = new HashMap<String, Object>();
			if (onlyHeader)
				returnMap.put("headers", tempResults.iterator());
			else {
				HashMap<String, ArrayList<String>> xslts = resolveXSLTsForMetadataPrefix(metadataPrefix);
				if (xslts == null || xslts.size()==0){
					returnMap.put("records",tempResults.iterator());
				}
				else {
					//These results need to be transformed using xslts on xslts ArrayList
					try {
						

						log.info("Result size = " + tempResults.size());
						ArrayList<String> finalresults = new ArrayList<String>();
						for (String res : tempResults) {

							int index = res.indexOf("><metadata>");
							if (index!=-1){
								String header = res.substring(0, index+11);
								res = res.substring(index+11);
								res = res.replace("</metadata></record>", "");

								//Need somehow to understand the metadata of the record
								
								String namespace = res.substring(res.indexOf("xsi:schemaLocation=\""));
								namespace = namespace.replace("xsi:schemaLocation=\"", "");
								namespace = namespace.substring(0, namespace.indexOf("\">"));
								namespace = namespace.split("\\s")[0];
								String prefix = resolvePrefixFromNamespace(namespace);
								
								ArrayList<String> allXSLTs = xslts.get(prefix);
								if (allXSLTs==null || allXSLTs.size()==0)
									throw new OAIInternalServerError("Mapping in Identify not correct");
								
								String toBeTransformed = res;

								for (String xsltUrlString : allXSLTs){
									InputStream xsltStream = new URL(xsltUrlString).openStream();
									StreamSource xsltFile = new StreamSource(xsltStream);


									byte[] barray = toBeTransformed.getBytes("UTF-8");
									InputStream is = new ByteArrayInputStream(barray);

									DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
									DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
									Document doc = dBuilder.parse(is);
									
									TransformerFactory xsltFactory = TransformerFactory.newInstance();
									Transformer transformer = xsltFactory.newTransformer(xsltFile);

									// Send transformed output to the console
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									StreamResult resultStream = new StreamResult(baos);

									//StreamSource xml = new StreamSource(is);

									// Apply the transformation
									DOMSource source = new DOMSource(doc);
									transformer.transform(source, resultStream);

									toBeTransformed = baos.toString();
								}

								finalresults.add(header + toBeTransformed + "</metadata></record>");
							}
						}	
						returnMap.put("records", finalresults.iterator());

					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerFactoryConfigurationError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}
			}
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

	@Override
	public String getDescriptions() {

		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				e.printStackTrace();
				return null;
			}
		}

		StringBuffer description = new StringBuffer();
		description.append("<description><metadataMapping xmlns=\"http://www.ekt.gr/OAI/metadata/mapping\"" +
				" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
				"  xsi:schemaLocation=\"http://www.ekt.gr/OAI/metadata/mapping http://devtom.ekt.gr:8889/metadatamapping.xsd\">");

		HashMap<String, Object> metadataMaps = (HashMap<String, Object>)context.getBean("metadata-maps");

		if (metadataMaps!=null) {
			HashMap<String, String> creatorMap = (HashMap<String, String>)metadataMaps.get("creator");
			if (creatorMap!=null){
				description.append("<creator>");
				if (creatorMap.containsKey("name")){
					description.append("<name>").append(creatorMap.get("name")).append("</name>");
				}
				if (creatorMap.containsKey("email")){
					description.append("<email>").append(creatorMap.get("email")).append("</email>");
				}
				if (creatorMap.containsKey("url")){
					description.append("<url>").append(creatorMap.get("url")).append("</url>");
				}
				if (creatorMap.containsKey("info")){
					description.append("<info>").append(creatorMap.get("info")).append("</info>");
				}
				description.append("</creator>");
			}

			ArrayList<HashMap<String, Object>> mappingsList = (ArrayList<HashMap<String, Object>>)metadataMaps.get("mappings");
			for (HashMap<String, Object> mapping : mappingsList){
				description.append("<mapping>");
				if (mapping.containsKey("source")){
					HashMap<String, String> source = (HashMap<String, String>)mapping.get("source");
					description.append("<sourceMetadataFormat>");

					description.append("<metadataPrefix>").append(source.get("metadataPrefix")).append("</metadataPrefix>");
					description.append("<schema>").append(source.get("schema")).append("</schema>");
					description.append("<metadataNamespace>").append(source.get("namespace")).append("</metadataNamespace>");

					description.append("</sourceMetadataFormat>");
				}

				if (mapping.containsKey("target")){
					HashMap<String, String> target = (HashMap<String, String>)mapping.get("target");
					description.append("<targetMetadataFormat>");

					description.append("<metadataPrefix>").append(target.get("metadataPrefix")).append("</metadataPrefix>");
					description.append("<schema>").append(target.get("schema")).append("</schema>");
					description.append("<metadataNamespace>").append(target.get("namespace")).append("</metadataNamespace>");

					description.append("</targetMetadataFormat>");
				}

				if (mapping.containsKey("lastModified")){
					description.append("<lastModified>").append(mapping.get("lastModified")).append("</lastModified>");
				}
				if (mapping.containsKey("xslt-url")){
					description.append("<xsltURL>").append(mapping.get("xslt-url")).append("</xsltURL>");
				}
				if (mapping.containsKey("additional-info")){
					description.append("<additionalInfo>");
						
					HashMap<String, Object> additionalInfo = (HashMap<String, Object>)mapping.get("additional-info");
					if (additionalInfo.containsKey("description")){
						description.append("<description>");
						description.append(additionalInfo.get("description"));
						description.append("</description>");
					}
					
					if (additionalInfo.containsKey("xsltArguments")){
						description.append("<xsltArguments>");
						HashMap<String, String> arguments = (HashMap<String, String>)additionalInfo.get("xsltArguments");
						for (String s : arguments.keySet()){
							description.append("<argument name=\""+s+"\" value=\""+arguments.get(s)+"\"/>");
						}
						description.append("</xsltArguments>");
					}
					
					description.append("</additionalInfo>");
				}
				
				description.append("</mapping>");
			}

			description.append("</metadataMapping></description>");

			return description.toString();
		}
		return null;
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

		HashMap<String, ArrayList<String>> xslts = resolveXSLTsForMetadataPrefix(metadataPrefix);
		if (xslts!=null && xslts.size()>0){
			metadataPrefix = "raw";
		}
		
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
	
	public HashMap<String, HashMap<String, String>> resolveXSLTs() {

		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				e.printStackTrace();
				return null;
			}
		}

		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();

		HashMap<String, String> result2 = new HashMap<String, String>();

		HashMap<String, Object> metadataMaps = (HashMap<String, Object>)context.getBean("metadata-maps");

		if (metadataMaps!=null){
			ArrayList<HashMap<String, Object>> mappingsList = (ArrayList<HashMap<String, Object>>)metadataMaps.get("mappings");
			for (HashMap<String, Object> mapping : mappingsList){
				HashMap<String, String> source = (HashMap<String, String>)mapping.get("source");
				HashMap<String, String> target = (HashMap<String, String>)mapping.get("target");
				String url = (String)mapping.get("xslt-url");

				HashMap<String, String> schemaDef = new HashMap<String, String>();
				schemaDef.put("schema", target.get("schema"));
				schemaDef.put("namespace", target.get("namespace"));
				result.put(target.get("metadataPrefix"), schemaDef);

			}

			return result;
		}
		return null;
	}
	
	public String resolvePrefixFromNamespace(String namespace) {

		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				e.printStackTrace();
				return null;
			}
		}

		HashMap<String, Object> metadataMaps = (HashMap<String, Object>)context.getBean("metadata-maps");

		if (metadataMaps!=null){
			ArrayList<HashMap<String, Object>> mappingsList = (ArrayList<HashMap<String, Object>>)metadataMaps.get("mappings");
			for (HashMap<String, Object> mapping : mappingsList){
				HashMap<String, String> source = (HashMap<String, String>)mapping.get("source");
				
				if (source.get("namespace").equals(namespace)){
					return source.get("metadataPrefix");
				}
				

			}
		}
		return null;
	}
	
	public HashMap<String, ArrayList<String>> resolveXSLTsForMetadataPrefix(String metadataPrefix) {

		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				e.printStackTrace();
				return null;
			}
		}

		HashMap<String, String> xslt = new HashMap<String, String>();
		HashMap<String, List<String>> maps = new HashMap<String, List<String>>();

		HashMap<String, Object> metadataMaps = (HashMap<String, Object>)context.getBean("metadata-maps");

		if (metadataMaps!=null){
			ArrayList<HashMap<String, Object>> mappingsList = (ArrayList<HashMap<String, Object>>)metadataMaps.get("mappings");
			for (HashMap<String, Object> mapping : mappingsList){
				HashMap<String, String> source = (HashMap<String, String>)mapping.get("source");
				HashMap<String, String> target = (HashMap<String, String>)mapping.get("target");
				String url = (String)mapping.get("xslt-url");

				xslt.put(source.get("metadataPrefix")+"_"+target.get("metadataPrefix"), (String)mapping.get("xslt-url"));
				if (maps.containsKey(target.get("metadataPrefix"))){
					maps.get(target.get("metadataPrefix")).add(source.get("metadataPrefix"));
				}
				else {
					ArrayList<String> tmp = new ArrayList<String>();
					tmp.add(source.get("metadataPrefix"));
					maps.put(target.get("metadataPrefix"), tmp);	
				}
			}
			
			HashMap<String, ArrayList<String>> finalResult = new HashMap<String, ArrayList<String>>();
			
			String[] path = new String[1000];
			for (Map.Entry<String, List<String>> tmp : maps.entrySet()){
				if (tmp.getKey().equals(metadataPrefix)){
					printPaths(tmp, path, 0, maps, xslt, null, finalResult); 
					break;
				}
			}
			
			return finalResult;
		}
		return null;
	}
	
	private void printPaths(Map.Entry<String, List<String>> node, String[] path, int pathLen, HashMap<String, List<String>> maps, HashMap<String, String> xslt, String leaf, HashMap<String, ArrayList<String>> result) { 

		if (node==null) {
			//printArray(path, pathLen, leaf);
			
			int i; 
			ArrayList<String> tmp = new ArrayList<String>();
			for (i=0; i<pathLen; i++) { 
				tmp.add(path[i]); 
			} 
			tmp.add(leaf);
			
			ArrayList<String> tmp2 = new ArrayList<String>();
			for (int j=tmp.size()-1; j>0; j--){
				String from = tmp.get(j);
				String to = tmp.get(j-1);
				String xsl = xslt.get(from+"_"+to);
				
				tmp2.add(xsl);
			}
			
			
			result.put(leaf, tmp2);
			
			return;
		}

		path[pathLen] = node.getKey(); 
		pathLen++;

		for (String s : node.getValue()){
			boolean found = false;
			for (Map.Entry<String, List<String>> tmp : maps.entrySet()){
				if (tmp.getKey().equals(s)){
					found = true;
					printPaths(tmp, path, pathLen, maps, xslt, null, result); 
					break;
				}
			}
			if (!found){
				printPaths(null, path, pathLen, maps, xslt, s, result); 
			}
		} 
	}

	private void printArray(String[] ints, int len, String leaf) { 
		int i; 
		for (i=0; i<len; i++) { 
			System.out.print(ints[i] + "_"); 
		} 
		System.out.println(leaf); 
	}
}
