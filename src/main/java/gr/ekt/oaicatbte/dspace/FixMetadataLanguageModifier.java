package gr.ekt.oaicatbte.dspace;

import org.dspace.content.DCValue;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;

public class FixMetadataLanguageModifier extends AbstractModifier {

	public FixMetadataLanguageModifier(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Record modify(MutableRecord rec) {

		DSpaceRecord record = (DSpaceRecord)rec;
		DCValue[] values = record.getDspaceValues();
		
		for (DCValue value : values){
			String language = value.language;
			if (language!=null){
				if (language.equalsIgnoreCase("el") || language.equalsIgnoreCase("gr")){
					value.language = "el";
				}
				else if (language.equalsIgnoreCase("en") || language.equalsIgnoreCase("other")){
					value.language = "en";
				}
				else {
					value.language = null;
				}
			}
		}
		
		return rec;
	}

}
