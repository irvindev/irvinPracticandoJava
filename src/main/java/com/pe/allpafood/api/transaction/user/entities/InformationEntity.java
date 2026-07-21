package com.pe.allpafood.api.transaction.user.entities;

import lombok.Data;

@Data
public class InformationEntity {
    private String gender;
    private String nutritionalObjective;
    private int height;
    private int weight;
    private int trainingDays;
    private Character routine;
    private int trainingHours;
    private Character trainingLevel;
    private Boolean strengthTraining;
    private String alimentsRestrictions;
    private Boolean sugar;
}
