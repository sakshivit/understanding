package org.example.service;

import com.chubb.na.domain.data.product.dto.SICCode;
import com.chubb.na.domain.helper.JsonHelper;
import com.chubb.na.domain.utils.Commons;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SicCodeService {

    @Inject
    JsonHelper jsonHelper;

    private static final Logger logger = Logger.getLogger(SicCodeService.class);

    public JSONObject enrichSicCode(JSONObject dataProductObject) {
        logger.info("----- ENRICHING SIC CODE -----");
        SICCode sicCodeObj;
        JSONObject sicCodeObject = jsonHelper.getSicCodeObject(dataProductObject);

        List<SicCodeDerivation> derivators = new ArrayList<>();

        // Check EML source - UW instructions, Email from broker
        derivators.add(new DatasourceDerivator(sicCodeObject, Commons.SOURCE_EML, Commons.DOC_TYPE_EML_AI, Commons.SOURCE_EML));

        // Get SIC code from EXP and CUW POLICY
        derivators.add(new CuwDerivator(dataProductObject, jsonHelper, Commons.SOURCE_EXP_MODELLED));

        // Check Acord source
        derivators.add(new DatasourceDerivator(sicCodeObject, Commons.SOURCE_ACORD, Commons.DOC_TYPE_ACORD_125,
                Commons.DOC_TYPE_ACORD_130));

        // Any Chubb application
        derivators.add(new DatasourceDerivator(sicCodeObject, Commons.SOURCE_APP_FORM,
                Commons.DOC_TYPE_APP_FORMS_DATA_AI, Commons.DOC_TYPE_APP_FORMS_DATA));

        // Broker application
        derivators.add((new DatasourceDerivator(sicCodeObject, Commons.SOURCE_MKTP,
                Commons.DOC_TYPE_MKTP_AI, Commons.SOURCE_MKTP)));

        // Any Other attachments
        derivators.add((new DatasourceDerivator(sicCodeObject, Commons.SOURCE_SCHEDULE_ITEMS,
                Commons.DOC_EXCEL_AI, Commons.EMPTY_STRING)));

        // Genius
        derivators.add(new CuwDerivator(dataProductObject, jsonHelper, Commons.SOURCE_GENIUS));

        for (SicCodeDerivation derivator : derivators) {
            sicCodeObj = derivator.deriveSicCode();

            if (sicCodeObj!= null && StringUtils.isNotBlank(sicCodeObj.getSicCode())) {
                logger.info("----- SIC CODE ENRICHED: " + sicCodeObj.getSicCode() + " -----");
                updateSicCode(dataProductObject, sicCodeObj);
                break;
            }
        }

        return dataProductObject;
    }

    protected void updateSicCode(JSONObject dataProductObject, SICCode sicCodeObj) {
        try {
            logger.info("----- UPDATING SIC CODE WITH VALUE: " + sicCodeObj.getSicCode() + " -----");
            JSONObject insured = dataProductObject.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0);

            insured.putOpt("SIC", sicCodeObj.getSicCode());
            insured.putOpt("SICConfidenceScore", sicCodeObj.getConfidence());
        } catch (Exception e) {
            logger.error("----- EXCEPTION WHILE UPDATING SIC CODE -----", e);
        }
    }

}
