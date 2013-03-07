package gr.ekt.oaicatbte;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.content.DCValue;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;

import gr.ekt.oaicatbte.dspace.dataloader.DSpaceRecord;
import gr.ekt.transformationengine.core.OutputGenerator;
import gr.ekt.transformationengine.core.Record;
import gr.ekt.transformationengine.core.RecordSet;

/**
 * 
 * @author Kosta Stamatis (kstamatis@ekt.gr) 
 * @author Nikos Houssos (nhoussos@ekt.gr) 
 * @copyright 2011 - National Documentation Center
 */
public class OAIOutputGenerator extends OutputGenerator {

	// Pattern containing all the characters we want to filter out / replace
	// converting a String to xml
	private static final Pattern invalidXmlPattern =
			Pattern.compile("([^\\t\\n\\r\\u0020-\\ud7ff\\ue000-\\ufffd\\u10000-\\u10ffff]+|[&<>])");

	// Pattern to test for only true dc elements.
	private static final Pattern dcElementPattern = Pattern
			.compile("(^(title|creator|subject|description|"
					+ "publisher|contributor|date|type|"
					+ "format|identifier|source|language|"
					+ "relation|coverage|rights)$)");

	private Crosswalk crosswalk = null;
	private boolean onlyHeader = false;

	/**
	 * Default constructor
	 */
	public OAIOutputGenerator() {
		// TODO Auto-generated constructor stub
	}


	@Override
	public boolean generateOutput(RecordSet recordSet) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<String> generateOAIOutput(RecordSet recordSet) {
		ArrayList<String> tmp = new ArrayList<String>();

		for (Record record : recordSet.getRecords()){
			StringBuffer xmlRec = new StringBuffer();
			if (!onlyHeader)
				xmlRec.append("<record>");
			xmlRec.append("<header>");
			xmlRec.append("<identifier>");
			xmlRec.append("oai:"+ConfigurationManager.getProperty("dspace.hostname")+":"+((DSpaceRecord)record).getDspaceHarvestedItemInfo().item.getHandle());
			xmlRec.append("</identifier>");
			xmlRec.append("<datestamp>");
			xmlRec.append((new Date()).toString());
			xmlRec.append("</datestamp>");
			xmlRec.append("</header>");
			if (!onlyHeader){
				xmlRec.append("<metadata>");
				xmlRec.append(this.createMetadata((DSpaceRecord)record));
				xmlRec.append("</metadata>");
				xmlRec.append("</record>");
			}

			tmp.add(xmlRec.toString());
		}

		return tmp;
		//return true;
	}

	public String createMetadata(DSpaceRecord record)
	{
		if (crosswalk!=null){
			try {
				return crosswalk.createMetadata(record);
			} catch (CannotDisseminateFormatException e) {
				e.printStackTrace();
				return "";
			}
		}
		else {
			return "";
		}
	}


	public Crosswalk getCrosswalk() {
		return crosswalk;
	}


	public void setCrosswalk(Crosswalk crosswalk) {
		this.crosswalk = crosswalk;
	}


	public boolean isOnlyHeader() {
		return onlyHeader;
	}


	public void setOnlyHeader(boolean onlyHeader) {
		this.onlyHeader = onlyHeader;
	}

}
