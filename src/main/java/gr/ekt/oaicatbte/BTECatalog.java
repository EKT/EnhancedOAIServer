package gr.ekt.oaicatbte;

import gr.ekt.transformationengine.core.OutputGenerator;
import gr.ekt.transformationengine.core.TransformationEngine;
import gr.ekt.transformationengine.exceptions.UnimplementedAbstractMethod;
import gr.ekt.transformationengine.exceptions.UnknownClassifierException;
import gr.ekt.transformationengine.exceptions.UnknownInputFileType;
import gr.ekt.transformationengine.exceptions.UnsupportedComparatorMode;
import gr.ekt.transformationengine.exceptions.UnsupportedCriterion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.verb.BadArgumentException;
import ORG.oclc.oai.server.verb.BadResumptionTokenException;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import ORG.oclc.oai.server.verb.IdDoesNotExistException;
import ORG.oclc.oai.server.verb.NoItemsMatchException;
import ORG.oclc.oai.server.verb.NoMetadataFormatsException;
import ORG.oclc.oai.server.verb.NoSetHierarchyException;
import ORG.oclc.oai.server.verb.OAIInternalServerError;

public abstract class BTECatalog extends AbstractCatalog {

	// Define a static logger variable
	static Logger log = Logger.getLogger(BTECatalog.class);

	private ClassPathXmlApplicationContext context = null;

	@Override
	public String getRecord(String arg0, String arg1)
			throws IdDoesNotExistException, CannotDisseminateFormatException,
			OAIInternalServerError {

		return null;
	}

	@Override
	public Map listIdentifiers(String arg0) throws BadResumptionTokenException,
	OAIInternalServerError {
		return null;
	}

	@Override
	public Map listIdentifiers(String arg0, String arg1, String arg2,
			String arg3) throws BadArgumentException,
			CannotDisseminateFormatException, NoItemsMatchException,
			NoSetHierarchyException, OAIInternalServerError {
		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public Vector getSchemaLocations(String arg0)
			throws IdDoesNotExistException, NoMetadataFormatsException,
			OAIInternalServerError {
		
		//return getRecordFactory().getCrosswalks().;
		
		return null;
	}

	@Override
	public Map listRecords(String from, String until, String set,
			String metadataPrefix) throws BadArgumentException,
			CannotDisseminateFormatException, NoItemsMatchException,
			NoSetHierarchyException, OAIInternalServerError {

		OAIDataLoadingSpec ls = new OAIDataLoadingSpec(set, 100, 0, from, until);
		
		try {
			return listRecords(ls, metadataPrefix, false);
		} catch (BadResumptionTokenException e) {
			// TODO Auto-generated catch block
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
			return listRecords(ls, metadataPrefix, true);
		} catch (CannotDisseminateFormatException e) {
			// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
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

	private Map listRecords(OAIDataLoadingSpec dataLoadingSpec, String metadataPrefix, boolean resumption) throws CannotDisseminateFormatException, OAIInternalServerError, BadResumptionTokenException{
		//Instantiate a new BTE
		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				// TODO Auto-generated catch block
				throw new OAIInternalServerError(e.getMessage());
			}
		}

		Object teObject = context.getBean("transformationEngine");
		if (teObject==null || !(teObject instanceof TransformationEngine))
			throw new OAIInternalServerError("Could not find the transformation engine!");
		TransformationEngine te = (TransformationEngine)teObject;
		
		OutputGenerator outputGenerator = resolveOutpuGenerator(metadataPrefix, resumption);

		te.getDataLoader().setLoadingSpec(dataLoadingSpec);
		te.setOutputGenerator(outputGenerator);

		try {
			List<String> results =  te.transform();

			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("records", results.iterator());
			HashMap<String, String> resumptionTokenMap = new HashMap<String, String>();
			resumptionTokenMap.put("resumptionToken", dataLoadingSpec.getFrom()+"/"+dataLoadingSpec.getUntil()+"/"+(dataLoadingSpec.getSet()!=null?dataLoadingSpec.getSet():"")+"/"+metadataPrefix+"/"+(dataLoadingSpec.getOffset()+dataLoadingSpec.getMax()));

			returnMap.put("resumptionMap", resumptionTokenMap);
			log.info("Transformation Engine ended");

			return returnMap;

		} catch (UnknownClassifierException e) {
			//e.printStackTrace();
			throw new OAIInternalServerError(e.getMessage());
		} catch (UnknownInputFileType e) {
			//e.printStackTrace();
			throw new OAIInternalServerError(e.getMessage());
		} catch (UnimplementedAbstractMethod e) {
			//e.printStackTrace();
			throw new OAIInternalServerError(e.getMessage());
		} catch (UnsupportedComparatorMode e) {
			//e.printStackTrace();
			throw new OAIInternalServerError(e.getMessage());
		} catch (UnsupportedCriterion e) {
			//e.printStackTrace();
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

	private OutputGenerator resolveOutpuGenerator(String metadataPrefix, boolean resumption) throws CannotDisseminateFormatException, OAIInternalServerError, BadResumptionTokenException{

		if (context==null){
			try {
				context = new ClassPathXmlApplicationContext("app-context.xml");
			} catch (BeansException e) {
				// TODO Auto-generated catch block
				throw new OAIInternalServerError(e.getMessage());
			}
		}
		
		Map<String, Object> metadataPrefixMapping = (Map<String, Object>)context.getBean("metadata-prefix-mapping");

		if (!metadataPrefixMapping.containsKey(metadataPrefix)){
			if (resumption)
				throw new BadResumptionTokenException();
			else 
				throw new CannotDisseminateFormatException(metadataPrefix);
		}

		Object outputGeneratorObject = metadataPrefixMapping.get(metadataPrefix);
		if (!(outputGeneratorObject instanceof OutputGenerator)){
			throw new OAIInternalServerError("metadata-prefix-mapping should be a map of metadata prefixes to BTE OutputGenerators!");
		}

		return (OutputGenerator)outputGeneratorObject;
	}

}
