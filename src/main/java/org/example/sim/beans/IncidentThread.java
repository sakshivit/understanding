package org.example.sim.beans;

import com.chubb.na.domain.sim.beans.SimLookUpNameInput;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentThread {
    private String text;
    private SimLookUpNameInput entryType;
    private SimLookUpNameInput contentType;
    private SimLookUpNameInput channel;

    public IncidentThread() {
        entryType = new SimLookUpNameInput(1);
    }
}
