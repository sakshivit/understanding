package org.example.sim.beans;

import com.chubb.na.domain.sim.beans.SimIncidentDetails.CustomFields;
import com.chubb.na.domain.sim.beans.SimIncidentDetails.StatusWithType;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
public class SimIncidentDetails {
    private String createdTime;
    private SimLookUpNameInput product;
    private StatusWithType statusWithType;
    private SimLookUpNameInput queue;
    private CustomFields customFields;
    private SimAssignedTo assignedTo;

    private Integer id;
    private String subject;
    @JsonProperty("threads")
    private IncidentThread thread;

    @NoArgsConstructor
    @Data
    public static class StatusWithType {
        private SimLookUpNameInput status;
    }

    @NoArgsConstructor
    @Data
    public static class SimAssignedTo {
        private SimLookUpNameInput account = null;
    }

    @NoArgsConstructor
    @Data
    public static class CustomFields {
        private C c;
        @Data
        @NoArgsConstructor
        public static class C {
            private SimLookUpNameInput segment;
            private SimLookUpNameInput pending_reason;
            private SimLookUpNameInput transaction;
            private String chubb_mailbox;
            private boolean rush_indicator;
        }
    }
}
