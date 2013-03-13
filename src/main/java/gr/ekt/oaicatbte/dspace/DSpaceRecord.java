package gr.ekt.oaicatbte.dspace;

import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.Value;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DCValue;
import org.dspace.search.HarvestedItemInfo;


/**
 * 
 * @author Kosta Stamatis (kstamatis@ekt.gr) 
 * @author Nikos Houssos (nhoussos@ekt.gr) 
 * @copyright 2011 - National Documentation Center
 */
public class DSpaceRecord implements MutableRecord {

	private DCValue[] dspaceValues;
	private String handle;
	private boolean isDeleted = false;
	private List<String> sets;
	private String datestamp;

	/**
	 * 
	 */
	public DSpaceRecord(DCValue[] values, String handle) {
		this.dspaceValues = values;
		this.handle = handle;
	}

	public List<Value> getValues(String field) {
		if (field.equals("identifier")){
			ArrayList<Value> result = new ArrayList<Value>();
			Value value = new Value() {
				public String getAsString() {
					return handle;
				}
			};
			result.add(value);
			return result;
		}
		else if (field.equals("isDeleted")){
			ArrayList<Value> result = new ArrayList<Value>();
			Value value = new Value() {
				public String getAsString() {
					return (new Boolean(isDeleted)).toString();
				}
			};
			result.add(value);
			return result;
		}
		else if (field.equals("setSpecs")){
			ArrayList<Value> result = new ArrayList<Value>();
			for (String set : sets){
				final String s = set;
				Value value = new Value() {
					public String getAsString() {
						return s;
					}
				};
				result.add(value);
			}
			return result;
		}
		else if (field.equals("datestamp")){
			ArrayList<Value> result = new ArrayList<Value>();
			Value value = new Value() {
				public String getAsString() {
					return datestamp;
				}
			};
			result.add(value);
			return result;
		}
		return null;
	}

	public MutableRecord makeMutable() {
		return this;
	}

	public boolean isMutable() {
		return true;
	}

	public boolean addField(String field, List<Value> values) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addValue(String field, Value value) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeField(String field) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeValue(String field, Value value) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updateField(String field, List<Value> value) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updateValue(String field, Value old_value, Value new_value) {
		// TODO Auto-generated method stub
		return false;
	}

	public DCValue[] getDspaceValues() {
		return dspaceValues;
	}

	public void setDspaceValues(DCValue[] dspaceValues) {
		this.dspaceValues = dspaceValues;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public List<String> getSets() {
		return sets;
	}

	public void setSets(List<String> sets) {
		this.sets = sets;
	}

	public String getDatestamp() {
		return datestamp;
	}

	public void setDatestamp(String datestamp) {
		this.datestamp = datestamp;
	}
}
