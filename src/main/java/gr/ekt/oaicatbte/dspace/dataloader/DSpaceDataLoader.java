/**
 * 
 */
package gr.ekt.oaicatbte.dspace.dataloader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see gr.ekt.transformationengine.core.DataLoader#loadData()
	 */
	@Override
	public RecordSet loadData() {
		// TODO Auto-generated method stub

		Context context = null;
		Map results = new HashMap();

		// List to put results in
		List records = new LinkedList();

		try
		{
			context = new Context();

			ArrayList<DSpaceObject> scopes = resolveSets(context, new ArrayList<String>(Arrays.asList("hdl_10442_2")));

			log.info("Scopes = " + scopes.size());
			
			boolean includeAll = ConfigurationManager.getBooleanProperty("harvest.includerestricted.oai", true);

			List itemInfos2 = Harvest.harvest(context, scopes, null, null, 0, 100, true, true, true, includeAll); // Need items, containers + withdrawals*/
		
			log.info("OAIIIIIIIII: items: " + itemInfos2.size());
		}catch (Exception e){

		}
		//Harvest.harvest(context, scopes, startDate, endDate, offset, limit, items, collections, withdrawn, nonAnon)

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

	private ArrayList<DSpaceObject> resolveSets(Context context, ArrayList<String> sets){
		ArrayList<DSpaceObject> result = new ArrayList<DSpaceObject>();
		if (sets == null) return null;
		for (String set : sets){

			if (set == null || set.trim().equals("all"))
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
					// TODO Auto-generated catch block
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
