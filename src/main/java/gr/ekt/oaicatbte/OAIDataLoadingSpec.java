/**
 * 
 */
package gr.ekt.oaicatbte;

import java.util.List;

import gr.ekt.transformationengine.dataloaders.DataLoadingSpec;

/**
 * @author kstamatis
 *
 */
public class OAIDataLoadingSpec extends DataLoadingSpec {

	private List<String> sets;
	private int max;
	private int offset;
	
	private String resumptionToken;
	
	/**
	 * 
	 */
	public OAIDataLoadingSpec() {
	}

	public OAIDataLoadingSpec(List<String> sets, int max, int offset) {
		super();
		this.sets = sets;
		this.max = max;
		this.offset = offset;
	}
	
	public OAIDataLoadingSpec(String resumptionToken) {
		
	}

	/* (non-Javadoc)
	 * @see gr.ekt.transformationengine.dataloaders.DataLoadingSpec#generateNextLoadingSpec()
	 */
	@Override
	public DataLoadingSpec generateNextLoadingSpec() {
		return new OAIDataLoadingSpec(sets, max, offset+max);
	}

	public List<String> getSets() {
		return sets;
	}

	public void setSets(List<String> sets) {
		this.sets = sets;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}	
}
