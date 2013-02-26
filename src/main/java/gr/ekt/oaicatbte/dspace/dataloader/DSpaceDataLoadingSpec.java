/**
 * 
 */
package gr.ekt.oaicatbte.dspace.dataloader;

import java.util.List;

import gr.ekt.transformationengine.dataloaders.DataLoadingSpec;

/**
 * @author kstamatis
 *
 */
public class DSpaceDataLoadingSpec extends DataLoadingSpec {

	private List<String> sets;
	private int max;
	private int offset;
	
	private String resumptionToken;
	
	/**
	 * 
	 */
	public DSpaceDataLoadingSpec() {
	}

	public DSpaceDataLoadingSpec(List<String> sets, int max, int offset) {
		super();
		this.sets = sets;
		this.max = max;
		this.offset = offset;
	}
	
	public DSpaceDataLoadingSpec(String resumptionToken) {
		
	}

	/* (non-Javadoc)
	 * @see gr.ekt.transformationengine.dataloaders.DataLoadingSpec#generateNextLoadingSpec()
	 */
	@Override
	public DataLoadingSpec generateNextLoadingSpec() {
		return new DSpaceDataLoadingSpec(sets, max, offset+max);
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
