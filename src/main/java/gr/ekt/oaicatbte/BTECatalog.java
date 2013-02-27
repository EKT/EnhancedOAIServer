package gr.ekt.oaicatbte;

import gr.ekt.oaicatbte.dspace.dataloader.DSpaceDataLoader;
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
		return null;
	}

	@Override
	public Map listRecords(String from, String until, String set,
			String metadataPrefix) throws BadArgumentException,
			CannotDisseminateFormatException, NoItemsMatchException,
			NoSetHierarchyException, OAIInternalServerError {

		//Instantiate a new BTE
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("app-context.xml");
		TransformationEngine te = (TransformationEngine) context.getBean("transformationEngine");

		ArrayList<String> sets = new ArrayList<String>();
		if (set!=null)
			sets.add(set);
		OAIDataLoadingSpec ls = new OAIDataLoadingSpec(sets, 100, 0);
		te.getDataLoader().setLoadingSpec(ls);

		try {
			List<String> results =  te.transform();

			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("records", results.iterator());
			HashMap<String, String> resumptionTokenMap = new HashMap<String, String>();
			resumptionTokenMap.put("resumptionToken", from+"/"+until+"/"+(set!=null?set:"")+"/"+metadataPrefix+"/"+(ls.getOffset()+ls.getMax()));

			returnMap.put("resumptionMap", resumptionTokenMap);
			log.info("Transformation Engine ended");

			return returnMap;

		} catch (UnknownClassifierException e) {
		} catch (UnknownInputFileType e) {
			e.printStackTrace();
		} catch (UnimplementedAbstractMethod e) {
			e.printStackTrace();
		} catch (UnsupportedComparatorMode e) {
			e.printStackTrace();
		} catch (UnsupportedCriterion e) {
			e.printStackTrace();
		}

		return super.listRecords(from, until, set, metadataPrefix);
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
		
		//Instantiate a new BTE
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("app-context.xml");
		TransformationEngine te = (TransformationEngine) context.getBean("transformationEngine");

		ArrayList<String> sets = new ArrayList<String>();
		if (set!=null)
			sets.add(set);
		OAIDataLoadingSpec ls = new OAIDataLoadingSpec(sets, 100, offset);
		te.getDataLoader().setLoadingSpec(ls);

		try {
			List<String> results =  te.transform();

			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("records", results.iterator());
			HashMap<String, String> resumptionTokenMap = new HashMap<String, String>();
			resumptionTokenMap.put("resumptionToken", from+"/"+until+"/"+(set!=null?set:"")+"/"+metadataPrefix+"/"+(ls.getOffset()+ls.getMax()));

			returnMap.put("resumptionMap", resumptionTokenMap);
			log.info("Transformation Engine ended");

			return returnMap;

		} catch (UnknownClassifierException e) {
			e.printStackTrace();
		} catch (UnknownInputFileType e) {
			e.printStackTrace();
		} catch (UnimplementedAbstractMethod e) {
			e.printStackTrace();
		} catch (UnsupportedComparatorMode e) {
			e.printStackTrace();
		} catch (UnsupportedCriterion e) {
			e.printStackTrace();
		}

		return super.listRecords(resumptionToken);
	}

	@Override
	public Map listSets() throws NoSetHierarchyException,
	OAIInternalServerError {

		//Return here all virtual sets

		return null;
	}

	/**
	 * Not yet supported - 
	 */
	@Override
	public Map listSets(String arg0) throws BadResumptionTokenException,
	OAIInternalServerError {
		return null;
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
}
