package gr.ekt.oaicatbte;

import gr.ekt.oaicatbte.dspace.dataloader.DSpaceBTEOAICatalog;

import java.util.Map;

import ORG.oclc.oai.server.verb.BadArgumentException;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import ORG.oclc.oai.server.verb.NoItemsMatchException;
import ORG.oclc.oai.server.verb.NoSetHierarchyException;
import ORG.oclc.oai.server.verb.OAIInternalServerError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
extends TestCase
{
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public AppTest( String testName )
	{
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( AppTest.class );
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp()
	{
		DSpaceBTEOAICatalog catalog = new DSpaceBTEOAICatalog(null);
		try {
			catalog.listRecords(null, null, null, "oai_dc");
			catalog.listSets();
		} catch (BadArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotDisseminateFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoItemsMatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSetHierarchyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAIInternalServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue( true );
	}
}
