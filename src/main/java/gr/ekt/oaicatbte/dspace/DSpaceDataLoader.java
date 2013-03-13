/**
 * 
 */
package gr.ekt.oaicatbte.dspace;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DCDate;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
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

		try
		{
			if (context==null)
				context = new Context();

			if (spec.getIdentifier()!=null){ //Load just one record
				HarvestedItemInfo hii = Harvest.getSingle(context, spec.getIdentifier().replace("oai:", ""), false);

				Item item = hii.item;

				// Get all the metadata
				DCValue[] allDC = item.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
				DSpaceRecord record = new DSpaceRecord(allDC, item.getHandle());

				record.setDeleted(hii.withdrawn);

				List<String> setSpecs = getSetSpecs(item);
				record.setSets(setSpecs);

				record.setDatestamp(new DCDate(hii.datestamp).toString());

				recordSet.addRecord(record);

				try {
					context.complete();
					context = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return recordSet;
			}

			ArrayList<DSpaceObject> scopes = resolveSets(context, spec.getDataSetName());

			boolean includeAll = ConfigurationManager.getBooleanProperty("harvest.includerestricted.oai", true);

			List itemInfos2 = Harvest.harvest(context, scopes, decodeDate(spec.getFromDate(), null), decodeDate(spec.getUntilDate(), null), spec.getOffset(), spec.getNumberOfRecords(), true, true, true, includeAll); // Need items, containers + withdrawals*/

			for (Object itemInfo : itemInfos2){
				Item item = ((HarvestedItemInfo)itemInfo).item;
				DCValue[] allDC = item.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
				DSpaceRecord record = new DSpaceRecord(allDC, item.getHandle());

				HarvestedItemInfo hii = (HarvestedItemInfo)itemInfo;
				hii.item = item;
				record.setDeleted(hii.withdrawn);

				List<String> setSpecs = getSetSpecs(item);
				record.setSets(setSpecs);

				record.setDatestamp(new DCDate(hii.datestamp).toString());

				recordSet.addRecord(record);
			}
			
			try {
				if (context!=null) {
					context.complete();
					context = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}catch (Exception e){
			e.printStackTrace();
			try {
				if (context!=null){
					context.complete();
					context = null;
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}

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

	private List<String> getSetSpecs(Item item){
		List<String> setSpecs = new ArrayList<String>();

		try {
			Collection[] collections = item.getCollections();

			// Convert the DB Handle string 123.456/789 to the OAI-friendly
			// hdl_123.456/789
			for (int i=0; i<collections.length; i++){
				Collection collection = collections[i];
				String handle = "hdl_" + collection.getHandle();
				setSpecs.add(handle.replace('/', '_'));
			}
			return setSpecs;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return setSpecs;
	}

	private static String decodeDate(Date t, String format) throws ParseException
	{
		SimpleDateFormat df = null;

		// Choose the correct date format based on string length
		if (format==null){
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		}

		// Parse the date
		df.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
		return df.format(t);
	} 
}
