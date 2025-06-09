package org.example.sim.beans;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SimLookUpNameInput {
    private Integer id;
    private String lookupName;

    public SimLookUpNameInput(String lookupName) {
        this.lookupName = lookupName;
    }

    public SimLookUpNameInput(Integer id) {
        this.id = id;
    }
}