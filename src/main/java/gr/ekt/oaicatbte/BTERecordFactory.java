/**
 *
 */
package gr.ekt.oaicatbte;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import ORG.oclc.oai.server.catalog.RecordFactory;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;

/**
 * @author kstamatis
 *
 */
public class BTERecordFactory extends RecordFactory {

    /**
     * @param properties
     */
    public BTERecordFactory(Properties properties) {
        super(properties);
    }

    /**
     * @param crosswalkMap
     */
    public BTERecordFactory(HashMap crosswalkMap) {
        super(crosswalkMap);
    }

    /* (non-Javadoc)
     * @see ORG.oclc.oai.server.catalog.RecordFactory#fromOAIIdentifier(java.lang.String)
     */
    @Override
    public String fromOAIIdentifier(String arg0) {
        return null;
    }

    /* (non-Javadoc)
     * @see ORG.oclc.oai.server.catalog.RecordFactory#getAbouts(java.lang.Object)
     */
    @Override
    public Iterator getAbouts(Object arg0) {
        return null;
    }

    /* (non-Javadoc)
     * @see ORG.oclc.oai.server.catalog.RecordFactory#getDatestamp(java.lang.Object)
     */
    @Override
    public String getDatestamp(Object arg0) {
        return null;
    }

    /* (non-Javadoc)
     * @see ORG.oclc.oai.server.catalog.RecordFactory#getOAIIdentifier(java.lang.Object)
     */
    @Override
    public String getOAIIdentifier(Object arg0) {
        return null;
    }

    /* (non-Javadoc)
     * @see ORG.oclc.oai.server.catalog.RecordFactory#getSetSpecs(java.lang.Object)
     */
    @Override
    public Iterator getSetSpecs(Object arg0) throws IllegalArgumentException {
        return null;
    }

    /* (non-Javadoc)
     * @see ORG.oclc.oai.server.catalog.RecordFactory#isDeleted(java.lang.Object)
     */
    @Override
    public boolean isDeleted(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ORG.oclc.oai.server.catalog.RecordFactory#quickCreate(java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public String quickCreate(Object arg0, String arg1, String arg2)
            throws IllegalArgumentException, CannotDisseminateFormatException {
        // TODO Auto-generated method stub
        return null;
    }

}
