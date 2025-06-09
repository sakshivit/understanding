package org.example.service;

import com.chubb.na.domain.data.product.dto.SICCode;
import com.chubb.na.domain.helper.JsonHelper;
import com.chubb.na.domain.utils.Commons;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CuwDerivator implements SicCodeDerivation {
    private static final Logger logger = Logger.getLogger(CuwDerivator.class);

    private JsonHelper jsonHelper;
    private JSONObject dataProductObject;
    private String source;

    public CuwDerivator(JSONObject dataProductObject, JsonHelper jsonHelper, String source) {
        this.dataProductObject = dataProductObject;
        this.jsonHelper = jsonHelper;
        this.source = source;
    }

    @Override
    public SICCode deriveSicCode() {
        logger.info("----- GETTING SIC CODE FROM ENRICHMENT SOURCE -----");
        SICCode expSicCodeObj = new SICCode();
        SICCode  policySicCodeObj = new SICCode();
        int highestDcl = -2;

        JSONArray enrichedSicCodes = jsonHelper.getEnrichedSicCodeObject(this.dataProductObject);

        try {
            if (enrichedSicCodes != null) {
                for (int i = 0; i < enrichedSicCodes.length(); i++) {
                    JSONObject enrichedSicCode = enrichedSicCodes.getJSONObject(i);

                    String srcCd = enrichedSicCode.optString("srcCd");
                    String dclString = enrichedSicCode.optString("dcl");
                    String value = enrichedSicCode.optString("value");

                    if(StringUtils.isNotBlank(source) && Commons.SOURCE_GENIUS.equalsIgnoreCase(source)) {
                        if(StringUtils.isNotBlank(srcCd) && Commons.SOURCE_GENIUS.equalsIgnoreCase(srcCd)) {
                            logger.info("----- SIC CODE FOUND FROM SOURCE GENIUS -----");
                            policySicCodeObj.setSicCode(value);
                            policySicCodeObj.setConfidence(dclString);

                            return policySicCodeObj;
                        }
                    } else {
                        int dcl = -1;
                        try {
                            dcl = Integer.parseInt(dclString);
                        } catch (NumberFormatException e) {
                            logger.error("----- DCL IS EMPTY. CANNOT CONVERT TO INT -----", e);
                        }

                        // Check if srcCd is "EXP Modelled" and if the dcl is higher than the current highest
                        if (Commons.SOURCE_EXP_MODELLED.equalsIgnoreCase(srcCd) && dcl > 50 && dcl > highestDcl) {
                            highestDcl = dcl;
                            expSicCodeObj.setSicCode(value);
                            expSicCodeObj.setConfidence(dclString);
                        }

                        if(Commons.SOURCE_POLICY.equalsIgnoreCase(srcCd)) {
                            policySicCodeObj.setSicCode(value);
                            policySicCodeObj.setConfidence(dclString);
                        }
                    }
                }

                if(expSicCodeObj != null && StringUtils.isBlank(expSicCodeObj.getSicCode())) {
                    return policySicCodeObj;
                }
            }
        } catch (JSONException e) {
            logger.error("----- ERROR WHILE GETTING SIC CODE FROM enriched_sic_code ----- " + e);
        }

        return expSicCodeObj;
    }
}

