package gr.ekt.oaicatbte.dspace;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.content.DCValue;
import org.dspace.content.Item;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;

/**
 * An OAICat Crosswalk implementation that extracts unqualified Dublin Core from
 * DSpace items into the oai_dc format.
 * 
 * @author Robert Tansley
 * @version $Revision: 3705 $
 */
public class OAIDCCrosswalk extends Crosswalk
{
	// Pattern containing all the characters we want to filter out / replace
	// converting a String to xml
	private static final Pattern invalidXmlPattern =
			Pattern.compile("([^\\t\\n\\r\\u0020-\\ud7ff\\ue000-\\ufffd\\u10000-\\u10ffff]+|[&<>])");

	public OAIDCCrosswalk(Properties properties)
	{
		super("http://www.openarchives.org/OAI/2.0/oai_dc/ "
				+ "http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
	}

	public boolean isAvailableFor(Object nativeItem)
	{
		// We have DC for everything
		return true;
	}

	public String createMetadata(Object nativeItem)
			throws CannotDisseminateFormatException
			{
		DSpaceRecord record = (DSpaceRecord)nativeItem;

		// Get all the DC
		DCValue[] allDC = record.getDspaceValues();

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
			String language = allDC[i].language;
			String qualifier = allDC[i].qualifier;
			String schema = allDC[i].schema;

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

				metadata.append("<dc:").append(element);
				//if (qualifier!=null){
				//	metadata.append("_"+qualifier);
				//}
				if (language!=null){
					metadata.append(" xmlns:lang=\""+language+"\"");
				}
				metadata.append(">").append(valueBuf.toString()).append("</dc:").append(element).append(">");
			}
		}

		metadata.append("</oai_dc:dc>");

		return metadata.toString();
	}
}
