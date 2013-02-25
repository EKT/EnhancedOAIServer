/**
 * 
 */
package gr.ekt.oaicatbte.dspace.dataloader;

import gr.ekt.transformationengine.dataloaders.DataLoadingSpec;

/**
 * @author kstamatis
 *
 */
public class DSpaceDataLoadingSpec extends DataLoadingSpec {

	private String set;
	private String max;
	private String offset;
	
	/**
	 * 
	 */
	public DSpaceDataLoadingSpec() {
		// TODO Auto-generated constructor stub
	}

	public DSpaceDataLoadingSpec(String set, String max, String offset) {
		super();
		this.set = set;
		this.max = max;
		this.offset = offset;
	}

	/* (non-Javadoc)
	 * @see gr.ekt.transformationengine.dataloaders.DataLoadingSpec#generateNextLoadingSpec()
	 */
	@Override
	public DataLoadingSpec generateNextLoadingSpec() {
		// TODO Auto-generated method stub
		return new DSpaceDataLoadingSpec(set, max, offset+max);
	}

	public String getSet() {
		return set;
	}

	public void setSet(String set) {
		this.set = set;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}	
}
