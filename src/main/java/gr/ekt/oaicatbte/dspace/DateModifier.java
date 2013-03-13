package gr.ekt.oaicatbte.dspace;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.content.DCValue;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;

public class DateModifier extends AbstractModifier {

	public DateModifier(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Record modify(MutableRecord rec) {
		
		DSpaceRecord record = (DSpaceRecord)rec;
		DCValue[] values = record.getDspaceValues();
		
		for (DCValue value : values) {
			if (value.schema.equals("dc") && value.element.equals("date") && value.qualifier==null){
				if (this.format(value.value) != null){
					value.value = this.format(value.value);
				}
			}
		}
		
		return record;
	}

	private String format(String value) {
		Pattern[] patterns = {
				Pattern.compile("[0-9]{4}")
		};
		Matcher matcher;
		for (Pattern pattern : patterns) {
			matcher = pattern.matcher(value);
			if (matcher.find()) return matcher.group();
		}
		return null;
	}
	
}
