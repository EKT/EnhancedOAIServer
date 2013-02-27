package gr.ekt.oaicatbte.dspace.dataloader;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.content.DCValue;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;

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
	        xmlRec.append("<record><header>");
	        xmlRec.append("<identifier>");
	        xmlRec.append("oai:"+ConfigurationManager.getProperty("dspace.hostname")+":"+((DSpaceRecord)record).getDspaceHarvestedItemInfo().item.getHandle());
	        xmlRec.append("</identifier>");
	        xmlRec.append("<datestamp>");
	        xmlRec.append((new Date()).toString());
	        xmlRec.append("</datestamp>");
	        xmlRec.append("</header>");
	        xmlRec.append("<metadata>");
	        xmlRec.append(this.createMetadata((DSpaceRecord)record));
	        xmlRec.append("</metadata>");
	        xmlRec.append("</record>");
	        
	        tmp.add(xmlRec.toString());
		}

		return tmp;
		//return true;
	}

	   public String createMetadata(DSpaceRecord record)
	    {
	        Item item = record.getDspaceHarvestedItemInfo().item;

	        // Get all the DC
	        DCValue[] allDC = item.getMetadata("dc", Item.ANY, Item.ANY, Item.ANY);

	        StringBuffer metadata = new StringBuffer();

	        metadata
	                .append(
	                        "<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" ")
	                .append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ")
	                .append(
	                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
	                .append(
	                        "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">");

	        for (int i = 0; i < allDC.length; i++)
	        {
	            String element = allDC[i].element;
	            String qualifier = allDC[i].qualifier;

	            // Do not include description.provenance
	            boolean provenance = "description".equals(element)
	                    && "provenance".equals(qualifier);

	            // Include only OAI DC (guard against outputing invalid DC)
	            if (dcElementPattern.matcher(element).matches() && !provenance)
	            {
	                // contributor.author exposed as 'creator'
	                if ("contributor".equals(element) && "author".equals(qualifier))
	                {
	                    element = "creator";
	                }

	                String value = allDC[i].value;
	                
	                // Escape XML chars <, > and &
	                // Also replace all invalid characters with ' '
	                if (value != null)
	                {
	                	StringBuffer valueBuf = new StringBuffer(value.length());
	                	Matcher xmlMatcher = invalidXmlPattern.matcher(value.trim());
	                	while (xmlMatcher.find())
	                	{
	                		String group = xmlMatcher.group();
	                		
	                		// group will either contain a character that we need to encode for xml
	                		// (ie. <, > or &), or it will be an invalid character
	                		// test the contents and replace appropriately
	                		
	                		if (group.equals("&"))
	                			xmlMatcher.appendReplacement(valueBuf, "&amp;");
	                		else if (group.equals("<"))
	                   			xmlMatcher.appendReplacement(valueBuf, "&lt;");
	                		else if (group.equals(">"))
	                   			xmlMatcher.appendReplacement(valueBuf, "&gt;");
	                		else
	                			xmlMatcher.appendReplacement(valueBuf, " ");
	                	}
	                	
	                	// add bit of the string after the final match
	                	xmlMatcher.appendTail(valueBuf);
		
		                metadata.append("<dc:").append(element).append(">").append(
		                        valueBuf.toString()).append("</dc:").append(element).append(">");
	                }
	            }
	        }

	        metadata.append("</oai_dc:dc>");

	        return metadata.toString();
	    }
	
}
