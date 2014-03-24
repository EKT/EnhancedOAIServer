package gr.ekt.oaicatbte;

import gr.ekt.bte.core.DataOutputSpec;
import gr.ekt.bte.core.OutputGenerator;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.core.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import ORG.oclc.oai.server.verb.OAIInternalServerError;


/**
 * @author Kosta Stamatis (kstamatis@ekt.gr)
 * @author Panagiotis Koutsourakis (kutsurak@ekt.gr)
 * @author Nikos Houssos (nhoussos@ekt.gr)
 * @copyright 2011 - National Documentation Center
 */
public class OAIOutputGenerator implements OutputGenerator {

    // Pattern containing all the characters we want to filter out / replace
    // converting a String to xml
    private static final Pattern invalidXmlPattern =
            Pattern.compile("([^\\t\\n\\r\\u0020-\\ud7ff\\ue000-\\ufffd\\u10000-\\u10ffff]+|[&<>])");

    // Pattern to test for only true dc elements.
    private static final Pattern dcElementPattern = Pattern
            .compile("(^(title|creator|subject|description|"
                     + "publisher|contributor|date|type|"
                     + "format|identifier|source|language|"
                     + "relation|coverage|rights)$)");

    private Crosswalk crosswalk = null;
    private boolean onlyHeader = false;
    private String schema = "";

    /**
     * Default constructor
     */
    public OAIOutputGenerator() {
    }

    public List<String> generateOutput(RecordSet recs) {
        return generateOutput(recs, null);
    }

    public List<String> generateOutput(RecordSet recs, DataOutputSpec spec) {

        ArrayList<String> tmp = new ArrayList<String>();

        for (Record record : recs.getRecords()){

            if (!hasMetadata(record)){
                continue;
            }

            //Identifier
            List<Value> identifiers = record.getValues("identifier");
            if (identifiers==null || identifiers.size()==0){
                try {
                    throw new OAIInternalServerError("Your implementation of BTE Record must return a Value for \"identifier\" field name!");
                } catch (OAIInternalServerError e) {
                    e.printStackTrace();
                    return null;
                }
            }
            String identifier = identifiers.get(0).getAsString();

            //isDeleted
            List<Value> isDels = record.getValues("isDeleted");
            boolean isDeleted = false;
            if (isDels==null || isDels.size()==0){
                try {
                    throw new OAIInternalServerError("Your implementation of BTE Record must return a Value (true or false string values) for \"isDeleted\" field name!");
                } catch (OAIInternalServerError e) {
                    e.printStackTrace();
                    return null;
                }
            }
            String isDel = isDels.get(0).getAsString();
            isDeleted = new Boolean(isDel);

            //Datestamp
            List<Value> datestamps = record.getValues("datestamp");
            if (datestamps==null || datestamps.size()==0){
                try {
                    throw new OAIInternalServerError("Your implementation of BTE Record must return a Value for \"datestamp\" field name!");
                } catch (OAIInternalServerError e) {
                    e.printStackTrace();
                    return null;
                }
            }
            String datestamp = datestamps.get(0).getAsString();

            //Sets
            List<Value> sets = record.getValues("setSpecs");
            if (sets==null || sets.size()==0){
                try {
                    throw new OAIInternalServerError("Your implementation of BTE Record must return a Value for \"setSpecs\" field name!");
                } catch (OAIInternalServerError e) {
                    e.printStackTrace();
                    return null;
                }
            }

            //Abouts
            List<Value> abouts = record.getValues("abouts");

            //Create output
            StringBuffer xmlRec = new StringBuffer();
            if (!onlyHeader)
                xmlRec.append("<record>");
            xmlRec.append("<header");

            if (isDeleted){
                xmlRec.append(" status=\"deleted\"");
            }

            xmlRec.append(">");
            xmlRec.append("<identifier>");
            xmlRec.append("oai:"+identifier);
            xmlRec.append("</identifier>");
            xmlRec.append("<datestamp>");
            xmlRec.append(datestamp);
            xmlRec.append("</datestamp>");

            for (Value value : sets){
                xmlRec.append("<setSpec>");
                xmlRec.append(value.getAsString());
                xmlRec.append("</setSpec>");
            }

            xmlRec.append("</header>");

            if (!onlyHeader){
                xmlRec.append("<metadata>");
                xmlRec.append(this.createMetadata(record));
                xmlRec.append("</metadata>");

                if (abouts!=null && abouts.size()>0){
                    Value value = abouts.get(0);
                    xmlRec.append("<about>");
                    xmlRec.append(value.getAsString());
                    xmlRec.append("</about>");

                }
            }

            if (!onlyHeader){
                xmlRec.append("</record>");
            }

            tmp.add(xmlRec.toString());
        }

        return tmp;
    }

    public String createMetadata(Record record)
    {
        if (crosswalk!=null){
            try {
                return crosswalk.createMetadata(record);
            } catch (CannotDisseminateFormatException e) {
                e.printStackTrace();
                return "";
            }
        }
        else {
            return "";
        }
    }

    public boolean hasMetadata(Record record)
    {
        return crosswalk.isAvailableFor(record);
    }

    public Crosswalk getCrosswalk() {
        return crosswalk;
    }


    public void setCrosswalk(Crosswalk crosswalk) {
        this.crosswalk = crosswalk;
    }


    public boolean isOnlyHeader() {
        return onlyHeader;
    }


    public void setOnlyHeader(boolean onlyHeader) {
        this.onlyHeader = onlyHeader;
    }

}
