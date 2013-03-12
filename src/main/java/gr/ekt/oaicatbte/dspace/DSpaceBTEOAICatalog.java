package gr.ekt.oaicatbte.dspace;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.core.Utils;

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
					results.put("hdl_"+allCols[i].getHandle().replace('/', '_'), Utils.addEntities(collName));
				}
				else
				{
					results.put("hdl_"+allCols[i].getHandle().replace('/', '_'), "");
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
					results.put("hdl_"+allComs[i].getHandle().replace('/', '_'), Utils.addEntities(commName));
				}
				else
				{
					results.put("hdl_"+allComs[i].getHandle().replace('/', '_'), "");
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
