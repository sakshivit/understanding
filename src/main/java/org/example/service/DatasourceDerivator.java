package org.example.service;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.chubb.na.domain.data.product.dto.SICCode;

public class DatasourceDerivator implements SicCodeDerivation {

    private static final Logger logger = Logger.getLogger(DatasourceDerivator.class);
    private JSONObject sicCodeObject;
    private String source;
    private String subDocType1;
    private String subDocType2;

    public DatasourceDerivator(JSONObject sicCodeObject, String source,
                               String subDocType1, String subDocType2) {
        this.sicCodeObject = sicCodeObject;
        this.source = source;
        this.subDocType1 = subDocType1;
        this.subDocType2 = subDocType2;
    }

    public SICCode deriveSicCode() {
        logger.info("----- GETTING SIC CODE FROM THE SOURCE: " + source + " -----");

        String sicCode = null;
        SICCode sicCodeObj = new SICCode();

        try {
            JSONArray dataSources = sicCodeObject.getJSONArray("dataSources");
            for (int i = 0; i < dataSources.length(); i++) {
                JSONObject dataSource = dataSources.getJSONObject(i);
                if (source.equalsIgnoreCase(dataSource.optString("source"))) {
                    JSONArray dependencies = dataSource.getJSONArray("dependency");

                    // Check for Sub Doc Type 1
                    sicCode = findValueBySubDocType(dependencies, subDocType1);

                    // If not found, check for Sub Doc Type 2
                    if (sicCode == null)
                        sicCode = findValueBySubDocType(dependencies, subDocType2);

                    if(StringUtils.isNotBlank(sicCode)) {
                        sicCodeObj.setSicCode(sicCode);
                        sicCodeObj.setConfidence(sicCodeObject.optString("finalScore"));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("----- ERROR WHILE GETTING SIC FROM SOURCE " + source + " ----- " + e);
        }

        return sicCodeObj;
    }

    protected String findValueBySubDocType(JSONArray dependencies, String subDocType) {
        String sicCode = null;

        try {
            for (int j = 0; j < dependencies.length(); j++) {
                JSONObject dependency = dependencies.getJSONObject(j);

                if (subDocType.equalsIgnoreCase(dependency.optString("subDocType")) &&
                        StringUtils.isNotBlank(dependency.optString("value"))) {
                    sicCode = dependency.optString("value");
                    break;
                }
            }
        } catch(Exception e) {
            logger.error("----- ERROR WHILE GETTING THE SIC CODE FROM THE SUB DOC TYPE " + subDocType + " -----" + e);
        }

        return sicCode;
    }
}
