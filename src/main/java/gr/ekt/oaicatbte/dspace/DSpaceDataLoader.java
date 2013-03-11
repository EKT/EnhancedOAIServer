/**
 * 
 */
package gr.ekt.oaicatbte.dspace;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.dspace.search.HarvestedItemInfo;

import gr.ekt.bte.core.DataLoader;
import gr.ekt.bte.core.DataLoadingSpec;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.exceptions.EmptySourceException;

/**
 * @author kstamatis
 *
 */
public class DSpaceDataLoader implements DataLoader {

	// Define a static logger variable
	static Logger log = Logger.getLogger(DSpaceDataLoader.class);

	Context context = null;

	/**
	 * 
	 */
	public DSpaceDataLoader() {
	}

	public RecordSet getRecords() throws EmptySourceException {
		return getRecords(null);
	}

	public RecordSet getRecords(DataLoadingSpec spec)
			throws EmptySourceException {
		RecordSet recordSet = new RecordSet();

		System.out.println("TEST");
		
		try
		{
			if (context==null)
				context = new Context();

			if (spec.getIdentifier()!=null){ //Load just one record
				DSpaceObject dso = HandleManager.resolveToObject(context, spec.getIdentifier().replace("oai:", ""));
				if (dso.getType() == Constants.ITEM){
					HarvestedItemInfo itemInfo = new HarvestedItemInfo();
					itemInfo.item = (Item)dso;
					
					DSpaceRecord record = new DSpaceRecord((HarvestedItemInfo)itemInfo);
					recordSet.addRecord(record);
					
					return recordSet;
				}
				else {
					
				}
			}
				
			ArrayList<DSpaceObject> scopes = resolveSets(context, spec.getDataSetName());

			boolean includeAll = ConfigurationManager.getBooleanProperty("harvest.includerestricted.oai", true);

			List itemInfos2 = Harvest.harvest(context, scopes, null, null, spec.getOffset(), spec.getNumberOfRecords(), true, true, true, includeAll); // Need items, containers + withdrawals*/

			for (Object itemInfo : itemInfos2){
				DSpaceRecord record = new DSpaceRecord((HarvestedItemInfo)itemInfo);
				recordSet.addRecord(record);
			}

			log.info("OAI: items: " + itemInfos2.size());

		}catch (Exception e){
			e.printStackTrace();
		}
		/*finally {
			try {
				context.complete();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}*/

		return recordSet;
	}

	private ArrayList<DSpaceObject> resolveSets(Context context, String set){
		ArrayList<DSpaceObject> result = new ArrayList<DSpaceObject>();
		if (set == null) 
			return null;
		//for (String set : sets){

		/*if (set == null)
			{
				continue;
			}*/

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
		//}
		return result;
	}

}
