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
import org.dspace.search.HarvestedItemInfo;

import ORG.oclc.oai.server.verb.BadResumptionTokenException;
import gr.ekt.oaicatbte.OAIDataLoadingSpec;
import gr.ekt.transformationengine.core.DataLoader;
import gr.ekt.transformationengine.core.RecordSet;

/**
 * @author kstamatis
 *
 */
public class DSpaceDataLoader extends DataLoader {

	// Define a static logger variable
	static Logger log = Logger.getLogger(DSpaceDataLoader.class);

	Context context = null;
	
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

		RecordSet recordSet = new RecordSet();
		
		try
		{
			if (context==null)
				context = new Context();

			OAIDataLoadingSpec oaiDataLoadingSpec = (OAIDataLoadingSpec)this.getLoadingSpec();
			ArrayList<DSpaceObject> scopes = resolveSets(context, oaiDataLoadingSpec.getSet());

			boolean includeAll = ConfigurationManager.getBooleanProperty("harvest.includerestricted.oai", true);

			log.info("Calling harvesetr");
			List itemInfos2 = Harvest.harvest(context, scopes, null, null, oaiDataLoadingSpec.getOffset(), oaiDataLoadingSpec.getMax(), true, true, true, includeAll); // Need items, containers + withdrawals*/
			log.info("harvester ended: total size="+itemInfos2.size());
			
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
