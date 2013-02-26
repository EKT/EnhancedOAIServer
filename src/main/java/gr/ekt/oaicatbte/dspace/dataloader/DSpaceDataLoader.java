/**
 * 
 */
package gr.ekt.oaicatbte.dspace.dataloader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

import ORG.oclc.oai.server.verb.BadResumptionTokenException;
import gr.ekt.transformationengine.core.DataLoader;
import gr.ekt.transformationengine.core.RecordSet;

/**
 * @author kstamatis
 *
 */
public class DSpaceDataLoader extends DataLoader {

	// Define a static logger variable
	static Logger log = Logger.getLogger(DSpaceDataLoader.class);

	/**
	 * 
	 */
	public DSpaceDataLoader() {
	}

	/* (non-Javadoc)
	 * @see gr.ekt.transformationengine.core.DataLoader#loadData()
	 */
	@Override
	public RecordSet loadData() {

		Context context = null;

		try
		{
			context = new Context();

			DSpaceDataLoadingSpec dspaceDataLoadingSpec = (DSpaceDataLoadingSpec)this.getLoadingSpec();
			ArrayList<DSpaceObject> scopes = resolveSets(context, dspaceDataLoadingSpec.getSets());

			boolean includeAll = ConfigurationManager.getBooleanProperty("harvest.includerestricted.oai", true);

			List itemInfos2 = Harvest.harvest(context, scopes, null, null, dspaceDataLoadingSpec.getOffset(), dspaceDataLoadingSpec.getMax(), true, true, true, includeAll); // Need items, containers + withdrawals*/
			
			log.info("OAI: items: " + itemInfos2.size());

		}catch (Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				context.complete();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

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

	private ArrayList<DSpaceObject> resolveSets(Context context, List<String> sets){
		ArrayList<DSpaceObject> result = new ArrayList<DSpaceObject>();
		if (sets == null) 
			return null;
		for (String set : sets){

			if (set == null)
			{
				continue;
			}

			DSpaceObject o = null;

			/*
			 * set specs are in form hdl_123.456_789 corresponding to
			 * hdl:123.456/789
			 */
			if (set.startsWith("hdl_"))
			{
				// Looks OK so far... turn second _ into /
				String handle = set.substring(4).replace('_', '/');
				try {
					o = HandleManager.resolveToObject(context, handle);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			// If it corresponds to a collection or a community, that's the set we
			// want
			if ((o != null) &&
					((o instanceof Collection) || (o instanceof Community))) 
			{
				result.add(o);
			}
		}
		return result;
	}

}
