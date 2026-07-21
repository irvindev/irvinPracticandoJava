package com.pe.allpafood.api.transaction.plan.entities.benefits;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumeBenefits {
    private List<String> extraBenefits;
    private List<Integer> complements;
    private List<String> principalBenefits;
    private List<String> additional;
    private Map<String, Integer> orders;
}
