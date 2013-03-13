package gr.ekt.oaicatbte.dspace;


import org.dspace.content.DCValue;

import gr.ekt.bte.core.AbstractFilter;
import gr.ekt.bte.core.Record;

public class AgriculturalDisciplineFilter extends AbstractFilter {

	public AgriculturalDisciplineFilter(String name) {
		super(name);
	}

	@Override
	public boolean isIncluded(Record rec) {

		DSpaceRecord record = (DSpaceRecord)rec;
		DCValue[] values = record.getDspaceValues();

		for (DCValue value : values){
			if (value!=null){
				if (value.schema.equals("thesis") && value.element.equals("degree") && value.qualifier.equals("discipline")) {
					if (value.value.equalsIgnoreCase("agricultural sciences")){
						return true;
					}
				}
			}
		}
		return false;
	}
}