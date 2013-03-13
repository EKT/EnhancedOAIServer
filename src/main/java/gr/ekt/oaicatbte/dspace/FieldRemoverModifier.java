package gr.ekt.oaicatbte.dspace;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.DCValue;
import org.dspace.content.Item;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;

public class FieldRemoverModifier extends AbstractModifier {

	List<String> removals;

	public FieldRemoverModifier(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Record modify(MutableRecord rec) {

		DSpaceRecord record = (DSpaceRecord)rec;
		ArrayList<DCValue> removalValues = new ArrayList<DCValue>();
		DCValue[] values = record.getDspaceValues();

		ArrayList<DCValue> finalValues = new ArrayList<DCValue>();

		if (removals!=null){
			int counter = 0;
			for (String removal : removals){
				String[] parts = removal.split("\\.");
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

				removalValues.add(value);

				counter ++;
			}

			for (DCValue value : values){
				if (value!=null){
					boolean shouldBeRemoved = false;
					for (DCValue removalValue : removalValues){
						boolean sameQualifier = false;
						if (value.qualifier==null && removalValue.qualifier==null)
							sameQualifier = true;
						else if (value.qualifier!=null && removalValue.qualifier!=null && value.qualifier.equals(removalValue.qualifier))
							sameQualifier = true;
						else if ("*".equals(removalValue.qualifier))
							sameQualifier = true;

						if (value.schema.equals(removalValue.schema) && value.element.equals(removalValue.element) && sameQualifier){
							shouldBeRemoved = true;
							break;
						}
					}

					if (!shouldBeRemoved)
						finalValues.add(value);
				}
			}
			DCValue[] finalV = new DCValue[finalValues.size()];
			int counter1 = 0;
			for (DCValue val : finalValues){
				finalV[counter1] = val;
				counter1++;
			}

			record.setDspaceValues(finalV);
		}

		return record;
	}

	public List<String> getRemovals() {
		return removals;
	}

	public void setRemovals(List<String> removals) {
		this.removals = removals;
	}

}
