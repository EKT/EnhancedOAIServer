package gr.ekt.oaicatbte.dspace;

import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.Value;

import java.util.ArrayList;
import java.util.List;

import org.dspace.search.HarvestedItemInfo;


/**
 * 
 * @author Kosta Stamatis (kstamatis@ekt.gr) 
 * @author Nikos Houssos (nhoussos@ekt.gr) 
 * @copyright 2011 - National Documentation Center
 */
public class DSpaceRecord implements Record {

	private HarvestedItemInfo dspaceHarvestedItemInfo;
	
	/**
	 * 
	 */
	public DSpaceRecord() {
	}

	public List<Value> getValues(String field) {
		if (field.equals("identifier")){
			ArrayList<Value> result = new ArrayList<Value>();
			Value value = new Value() {
				public String getAsString() {
					return dspaceHarvestedItemInfo.item.getHandle();
				}
			};
			result.add(value);
			return result;
		}
		return null;
	}

	public MutableRecord makeMutable() {
		return null;
	}

	public boolean isMutable() {
		return false;
	}

	
	public DSpaceRecord(HarvestedItemInfo dspaceHarvestedItemInfo) {
		super();
		this.dspaceHarvestedItemInfo = dspaceHarvestedItemInfo;
	}

	public HarvestedItemInfo getDspaceHarvestedItemInfo() {
		return dspaceHarvestedItemInfo;
	}

	public void setDspaceHarvestedItemInfo(HarvestedItemInfo dspaceHarvestedItemInfo) {
		this.dspaceHarvestedItemInfo = dspaceHarvestedItemInfo;
	}
}
