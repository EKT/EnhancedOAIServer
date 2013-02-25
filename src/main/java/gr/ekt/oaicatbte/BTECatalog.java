package gr.ekt.oaicatbte;

import gr.ekt.transformationengine.core.TransformationEngine;
import gr.ekt.transformationengine.exceptions.UnimplementedAbstractMethod;
import gr.ekt.transformationengine.exceptions.UnknownClassifierException;
import gr.ekt.transformationengine.exceptions.UnknownInputFileType;
import gr.ekt.transformationengine.exceptions.UnsupportedComparatorMode;
import gr.ekt.transformationengine.exceptions.UnsupportedCriterion;

import java.util.Map;
import java.util.Vector;

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

	@Override
	public String getRecord(String arg0, String arg1)
			throws IdDoesNotExistException, CannotDisseminateFormatException,
			OAIInternalServerError {

		return null;
	}

	@Override
	public Map listIdentifiers(String arg0) throws BadResumptionTokenException,
	OAIInternalServerError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map listIdentifiers(String arg0, String arg1, String arg2,
			String arg3) throws BadArgumentException,
			CannotDisseminateFormatException, NoItemsMatchException,
			NoSetHierarchyException, OAIInternalServerError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Vector getSchemaLocations(String arg0)
			throws IdDoesNotExistException, NoMetadataFormatsException,
			OAIInternalServerError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map listRecords(String from, String until, String set,
			String metadataPrefix) throws BadArgumentException,
			CannotDisseminateFormatException, NoItemsMatchException,
			NoSetHierarchyException, OAIInternalServerError {
		// TODO Auto-generated method stub

		//Instantiate a new BTE
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("app-context.xml");
		TransformationEngine te = (TransformationEngine) context.getBean("transformationEngine");
		try {
			te.transform("", "");
		} catch (UnknownClassifierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownInputFileType e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnimplementedAbstractMethod e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedComparatorMode e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedCriterion e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return super.listRecords(from, until, set, metadataPrefix);
	}

	@Override
	public Map listRecords(String resumptionToken)
			throws BadResumptionTokenException, OAIInternalServerError {
		// TODO Auto-generated method stub



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
		// TODO Auto-generated method stub
		return null;
	}
}
