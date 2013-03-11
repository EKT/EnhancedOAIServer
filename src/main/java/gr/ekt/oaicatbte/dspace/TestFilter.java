package gr.ekt.oaicatbte.dspace;

import java.util.Map;

import gr.ekt.bte.core.AbstractFilter;
import gr.ekt.bte.core.Record;

public class TestFilter extends AbstractFilter {

	public TestFilter(String name) {
		super(name);
	}

	@Override
	public boolean isIncluded(Record rec) {
		// TODO Auto-generated method stub
		return false;
	}
}
