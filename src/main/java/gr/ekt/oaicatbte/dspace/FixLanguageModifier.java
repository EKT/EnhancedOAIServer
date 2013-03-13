package gr.ekt.oaicatbte.dspace;

import java.util.ArrayList;

import org.dspace.content.DCValue;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;

public class FixLanguageModifier extends AbstractModifier {

	public FixLanguageModifier(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Record modify(MutableRecord rec) {
		
		DSpaceRecord record = (DSpaceRecord)rec;
		DCValue[] values = record.getDspaceValues();
		
		int index=-1;
		
		int counter = 0;
		for (DCValue value : values) {
			if (value.schema.equals("dc") && value.element.equals("language") && value.language.equals("el")){
				index = counter;
			}
			else if (value.schema.equals("dc") && value.element.equals("language") && value.language.equals("en")){
				String language = value.value;
				if (language.toLowerCase().equals("ελληνικά") || language.toLowerCase().equals("greek")){
					value.value = "gre";
					value.language = null;
				}
				else if (language.toLowerCase().equals("αγγλικά") || language.toLowerCase().equals("english")){
					value.value = "eng";
					value.language = null;
				}
				else if (language.toLowerCase().equals("γαλλικά") || language.toLowerCase().equals("french")){
					value.value = "fre";
					value.language = null;
				}
				else if (language.toLowerCase().equals("γερμανικά") || language.toLowerCase().equals("german")){
					value.value = "ger";
					value.language = null;
				}
				else if (language.toLowerCase().equals("ιταλικά") || language.toLowerCase().equals("italian")){
					value.value = "ita";
					value.language = null;
				}
				else if (language.toLowerCase().equals("ισπανικά") || language.toLowerCase().equals("spanish")){
					value.value = "esp";
					value.language = null;
				}
			}
			
			counter++;
		}
		
		if (index>0)
			values[index] = null;
		
		return record;
	}

}
