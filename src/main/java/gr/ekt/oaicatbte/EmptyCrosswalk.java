package gr.ekt.oaicatbte;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;

public class EmptyCrosswalk extends Crosswalk {

	public EmptyCrosswalk(String schemaLocation) {
		super(schemaLocation);
		// TODO Auto-generated constructor stub
	}

	public EmptyCrosswalk(String schemaLocation, String contentType) {
		super(schemaLocation, contentType);
		// TODO Auto-generated constructor stub
	}

	public EmptyCrosswalk(String schemaLocation, String contentType,
			String docType) {
		super(schemaLocation, contentType, docType);
		// TODO Auto-generated constructor stub
	}

	public EmptyCrosswalk(String schemaLocation, String contentType,
			String docType, String encoding) {
		super(schemaLocation, contentType, docType, encoding);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String createMetadata(Object arg0)
			throws CannotDisseminateFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAvailableFor(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
