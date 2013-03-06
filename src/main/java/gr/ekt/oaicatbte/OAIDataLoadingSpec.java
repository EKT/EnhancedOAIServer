/**
 * 
 */
package gr.ekt.oaicatbte;

import gr.ekt.transformationengine.dataloaders.DataLoadingSpec;

/**
 * @author kstamatis
 *
 */
public class OAIDataLoadingSpec extends DataLoadingSpec {

	private String set;
	private int max;
	private int offset;
	private String from;
	private String until;
	
	/**
	 * 
	 */
	public OAIDataLoadingSpec() {
	}

	public OAIDataLoadingSpec(String set, int max, int offset, String from, String until) {
		super();
		this.set = set;
		this.max = max;
		this.offset = offset;
		this.from = from;
		this.until = until;
	}
	
	public OAIDataLoadingSpec(String resumptionToken) {
		
	}

	/* (non-Javadoc)
	 * @see gr.ekt.transformationengine.dataloaders.DataLoadingSpec#generateNextLoadingSpec()
	 */
	@Override
	public DataLoadingSpec generateNextLoadingSpec() {
		return new OAIDataLoadingSpec(set, max, offset+max, from, until);
	}

	public String getSet() {
		return set;
	}

	public void setSets(String set) {
		this.set = set;
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

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getUntil() {
		return until;
	}

	public void setUntil(String until) {
		this.until = until;
	}	
}
