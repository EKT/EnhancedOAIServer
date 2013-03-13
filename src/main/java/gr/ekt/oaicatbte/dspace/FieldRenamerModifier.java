package gr.ekt.oaicatbte.dspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

import org.dspace.content.DCValue;
import org.dspace.content.Item;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;

public class FieldRenamerModifier extends AbstractModifier {

	Map<String,String> renames;

	public FieldRenamerModifier(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Record modify(MutableRecord rec) {

		DSpaceRecord record = (DSpaceRecord)rec;
		Map<DCValue, DCValue> renameValues = new HashMap<DCValue, DCValue>();
		DCValue[] values = record.getDspaceValues();

		System.out.println("BEFORE: " + values.length);

		ArrayList<DCValue> finalValues = new ArrayList<DCValue>();

		if (renames!=null){
			for (String renameFrom : renames.keySet()){
				String[] parts = renameFrom.split("\\.");
				if (parts.length<2){
					continue;
				}
				String schema = parts[0].trim();
				String element = parts[1].trim();
				String qualifier = null;
				if (parts.length>2){
					qualifier = parts[2].trim();
				}

				DCValue value = new DCValue();
				value.schema = schema;
				value.element = element;
				if (qualifier==null){
					value.qualifier = null;
				}
				else if (qualifier.equals("*")){
					value.qualifier = Item.ANY;
				}
				else {
					value.qualifier = qualifier;
				}

				String[] parts2 = renames.get(renameFrom).split("\\.");
				if (parts2.length<2){
					continue;
				}
				String schema2 = parts2[0].trim();
				String element2 = parts2[1].trim();
				String qualifier2 = null;
				if (parts2.length>2){
					qualifier2 = parts2[2].trim();
				}

				DCValue value2 = new DCValue();
				value2.schema = schema2;
				value2.element = element2;
				if (qualifier2==null){
					value2.qualifier = null;
				}
				else if (qualifier2.equals("*")){
					value2.qualifier = Item.ANY;
				}
				else {
					value2.qualifier = qualifier2;
				}

				renameValues.put(value, value2);
			}

			for (DCValue valueFrom: renameValues.keySet()){
				DCValue valueTo = renameValues.get(valueFrom);

				for (DCValue value : values){
					if (value!=null){
						boolean sameQualifier = false;
						if (value.qualifier==null && valueFrom.qualifier==null)
							sameQualifier = true;
						else if (value.qualifier!=null && valueFrom.qualifier!=null && value.qualifier.equals(valueFrom.qualifier))
							sameQualifier = true;

						if (valueFrom.schema.equals(value.schema) && valueFrom.element.equals(value.element) && sameQualifier){
							value.schema = valueTo.schema;
							value.element = valueTo.element;
							value.qualifier = valueTo.qualifier;
						}
					}
				}
			}
		}

		return record;
	}

	public Map<String, String> getRenames() {
		return renames;
	}

	public void setRenames(Map<String, String> renames) {
		this.renames = renames;
	}

}
