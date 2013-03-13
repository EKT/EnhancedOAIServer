package gr.ekt.oaicatbte.dspace;

import org.dspace.content.DCValue;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;

public class FixIdentifierModifier extends AbstractModifier {

	public FixIdentifierModifier(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Record modify(MutableRecord rec) {
		DSpaceRecord record = (DSpaceRecord)rec;
	
		DCValue[] values = record.getDspaceValues();

		for (DCValue value : values) {
			if (value!=null){
				if (value.schema.equals("dc") && value.element.equals("identifier") && "uri".equals(value.qualifier)){
					value.value = value.value.replace("10442/", "10442/hedi/");
					value.qualifier = null;
				}
			}
		}

		return record;
	}

}
