package org.example.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import com.chubb.na.domain.helper.JsonHelper;
import com.chubb.na.domain.sim.SimService;
import com.chubb.na.domain.utils.Commons;

@ApplicationScoped
public class UpdateSimStatusService {
    private static final Logger logger = Logger.getLogger(UpdateSimStatusService.class);

    @Inject
    JsonHelper jsonHelper;
    @Inject
    SimService simService;

    public void updateIncidentDetails(JSONObject message, String simProduct, String transaction, String marketSegment) {
        Long incidentId = jsonHelper.getIncidentFromKafkaMessage(message);
        try {
            if(incidentId != 0L) {
                simService.updateIncidentDetails(incidentId, simProduct, transaction, marketSegment);
            } else {
                logger.info("-----updateIncidentDetails():: INCIDENT ID NOT PRESENT IN THE KAFKA MESSAGE FOR SUBMISSION: "+message.optString(Commons.SUBMISSION_NUMBER_FIELD)+" -----");
            }
        } catch(Exception e) {
            logger.error("ERROR while updating SIM status:- "+ e.getLocalizedMessage());
        }
    }

    public void updateIncidentQueue(JSONObject message, String queue) {
        Long incidentId = jsonHelper.getIncidentFromKafkaMessage(message);
        try {
            if(incidentId != 0L) {
                simService.updateIncidentQueue(incidentId, queue);
            } else {
                logger.info("-----updateIncidentDetails():: INCIDENT ID NOT PRESENT IN THE KAFKA MESSAGE FOR SUBMISSION: "+message.optString(Commons.SUBMISSION_NUMBER_FIELD)+" -----");
            }
        } catch(Exception e) {
            logger.error("ERROR while updating SIM queue:- "+ e.getLocalizedMessage());
        }
    }

    public void updateIncidentStatus(JSONObject message, String status) {
        Long incidentId = jsonHelper.getIncidentFromKafkaMessage(message);
        try {
            if(incidentId != 0L) {
                simService.updateIncidentStatus(incidentId, status);
            } else {
                logger.info("-----updateIncidentDetails():: INCIDENT ID NOT PRESENT IN THE KAFKA MESSAGE FOR SUBMISSION: "+message.optString(Commons.SUBMISSION_NUMBER_FIELD)+" -----");
            }
        } catch(Exception e) {
            logger.error("ERROR while updating SIM status:- "+ e.getLocalizedMessage());
        }
    }


}
