package gr.ekt.oaicatbte.dspace;

import java.util.Map;

import org.dspace.content.DCValue;

import gr.ekt.bte.core.AbstractFilter;
import gr.ekt.bte.core.Record;

public class CountryFilter extends AbstractFilter {

	public CountryFilter(String name) {
		super(name);
	}

	@Override
	public boolean isIncluded(Record rec) {

		DSpaceRecord record = (DSpaceRecord)rec;
		DCValue[] values = record.getDspaceValues();

		for (DCValue value : values){
			if (value!=null){
				if (value.schema.equals("hedi") && value.element.equals("country")) {
					if (value.value.equalsIgnoreCase("ελλάδα")){
						return true;
					}
					else{
						return false;
					}
				}
			}
		}
		return false;
	}
}