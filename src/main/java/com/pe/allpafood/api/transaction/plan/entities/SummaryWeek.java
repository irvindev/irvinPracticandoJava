package com.pe.allpafood.api.transaction.plan.entities;

import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import lombok.Data;

import java.util.List;

@Data
public class SummaryWeek {
    private int dayId;
    private List<DetailEntity<Float>> metrics;
}
