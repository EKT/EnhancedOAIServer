package gr.ekt.oaicatbte.dspace;

import java.util.Map;

import org.dspace.content.DCValue;

import gr.ekt.bte.core.AbstractFilter;
import gr.ekt.bte.core.Record;

public class FullTextFilter extends AbstractFilter {

	public FullTextFilter(String name) {
		super(name);
	}

	@Override
	public boolean isIncluded(Record rec) {

		DSpaceRecord record = (DSpaceRecord)rec;
		DCValue[] values = record.getDspaceValues();

		for (DCValue value : values){
			if (value!=null){
				if (value.schema.equals("hedi") && value.element.equals("fulltext")) {
					if (value.value.equalsIgnoreCase("yes")){
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
