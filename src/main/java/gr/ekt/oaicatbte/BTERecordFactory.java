/**
 * Copyright (c) 2007-2013, National Documentation Centre (EKT, www.ekt.gr)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *     Neither the name of the National Documentation Centre nor the
 *     names of its contributors may be used to endorse or promote
 *     products derived from this software without specific prior written
 *     permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
