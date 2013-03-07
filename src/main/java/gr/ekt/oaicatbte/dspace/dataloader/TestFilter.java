package gr.ekt.oaicatbte.dspace.dataloader;

import java.util.Map;

import gr.ekt.transformationengine.core.Filter;
import gr.ekt.transformationengine.core.Record;
import gr.ekt.transformationengine.exceptions.UnimplementedAbstractMethod;
import gr.ekt.transformationengine.exceptions.UnsupportedComparatorMode;
import gr.ekt.transformationengine.exceptions.UnsupportedCriterion;

public class TestFilter extends Filter {

	public TestFilter() {
		// TODO Auto-generated constructor stub
	}

	public TestFilter(Map<String, String> map) {
		super(map);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean filter(Record record) throws UnimplementedAbstractMethod,
			UnsupportedComparatorMode, UnsupportedCriterion {
		// TODO Auto-generated method stub
		return true;
	}

}
