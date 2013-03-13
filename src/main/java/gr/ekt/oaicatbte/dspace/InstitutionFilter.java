package gr.ekt.oaicatbte.dspace;

import java.util.Map;

import org.dspace.content.DCValue;

import gr.ekt.bte.core.AbstractFilter;
import gr.ekt.bte.core.Record;

public class InstitutionFilter extends AbstractFilter {

	public InstitutionFilter(String name) {
		super(name);
	}

	@Override
	public boolean isIncluded(Record rec) {

		DSpaceRecord record = (DSpaceRecord)rec;
		DCValue[] values = record.getDspaceValues();

		for (DCValue value : values){
			if (value!=null){
				if (value.schema.equals("uketdterms") && value.element.equals("institution")) {
					if (value.value.equals("Ιδρύματα Εξωτερικού") || value.value.equals("Νοσοκομεία - Φορείς Υγείας")){
						return false;
					}
				}
			}
		}
		return true;
	}
}
