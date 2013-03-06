package gr.ekt.oaicatbte.dspace.dataloader;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.core.Utils;
import org.dspace.search.HarvestedItemInfo;

import ORG.oclc.oai.server.verb.IdDoesNotExistException;
import ORG.oclc.oai.server.verb.NoMetadataFormatsException;
import ORG.oclc.oai.server.verb.NoSetHierarchyException;
import ORG.oclc.oai.server.verb.OAIInternalServerError;
import gr.ekt.oaicatbte.BTECatalog;

public class DSpaceBTEOAICatalog extends BTECatalog {

	/** log4j logger */
	private static Logger log = Logger.getLogger(DSpaceBTEOAICatalog.class);
	
	/** Prefix that all our OAI identifiers have */
	public final static String OAI_ID_PREFIX = "oai:"
		+ ConfigurationManager.getProperty("dspace.hostname") + ":";
	
	public DSpaceBTEOAICatalog(Properties properties)
	{
		// Don't need to do anything
	}
	
	@Override
	public void close() {
		super.close();
	}

	@Override
	public Vector getSchemaLocations(String identifier)
			throws IdDoesNotExistException, NoMetadataFormatsException,
			OAIInternalServerError {
		log.info(LogManager.getHeader(null, "oai_request",
				"verb=getSchemaLocations,identifier="
				+ ((identifier == null) ? "null" : identifier)));

		HarvestedItemInfo itemInfo = null;
		Context context = null;

		// Get the item from the DB
		try
		{
			context = new Context();

			// Valid identifiers all have prefix "oai:hostname:"
			if (identifier.startsWith(OAI_ID_PREFIX))
			{
				itemInfo = Harvest.getSingle(context, identifier
						.substring(OAI_ID_PREFIX.length()), // Strip prefix to
						// get raw handle
						false);
			}
		}
		catch (SQLException se)
		{
			// Log the error
			log.warn(LogManager.getHeader(context, "database_error", ""), se);

			throw new OAIInternalServerError(se.toString());
		}
		finally
		{
			if (context != null)
			{
				context.abort();
			}
		}

		if (itemInfo == null)
		{
			throw new IdDoesNotExistException(identifier);
		}
		else
		{
			if (itemInfo.withdrawn)
			{
				throw new NoMetadataFormatsException();
			}
			else
			{
				return getRecordFactory().getSchemaLocations(itemInfo);
			}
		}
	}

	@Override
	public  Map<String, String> listAllSets() throws NoSetHierarchyException,
			OAIInternalServerError {
		// TODO Auto-generated method stub
		log.info(LogManager.getHeader(null, "oai_request", "verb=listSets"));

		Context context = null;

		 Map<String, String> results = new HashMap<String, String>();
		
		try
		{
			context = new Context();

			Collection[] allCols = Collection.findAll(context);
			for (int i = 0; i < allCols.length; i++)
			{
				String collName = allCols[i].getMetadata("name");
				if(collName != null)
				{
					results.put(allCols[i].getHandle().replace('/', '_'), Utils.addEntities(collName));
				}
				else
				{
					results.put(allCols[i].getHandle().replace('/', '_'), "");
					// Warn that there is an error of a null set name
					log.info(LogManager.getHeader(null, "oai_error",
							"null_set_name_for_set_id_" + allCols[i].getHandle()));
				}
			}

			Community[] allComs = Community.findAll(context);
			for (int i = 0; i < allComs.length; i++)
			{
				String commName = allComs[i].getMetadata("name");
				if(commName != null)
				{
					results.put(allComs[i].getHandle().replace('/', '_'), Utils.addEntities(commName));
				}
				else
				{
					results.put(allComs[i].getHandle().replace('/', '_'), "");
					// Warn that there is an error of a null set name
					log.info(LogManager.getHeader(null, "oai_error",
							"null_set_name_for_set_id_" + allComs[i].getHandle()));
				}
			}
		}
		catch (SQLException se)
		{
			// Log the error
			log.warn(LogManager.getHeader(context, "database_error", ""), se);

			throw new OAIInternalServerError(se.toString());
		}
		finally
		{
			if (context != null)
			{
				context.abort();
			}
		}
		
		return results;
	}
}
