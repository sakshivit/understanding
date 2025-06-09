package org.example.sim.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SimNoteRequest {
    private SimLookUpNameInput contentType;
    private SimLookUpNameInput entryType;
    private String text;
}
