package com.pe.allpafood.api.gateway.subscription_plan;


import com.pe.allpafood.api.transaction.plan.bussiness.IPlanService;
import com.pe.allpafood.api.transaction.plan.dto.SubscriptionPlanDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/catalog")
@RequiredArgsConstructor
@Slf4j
public class PublicEndpointsController {

    private final IPlanService subscriptionPlanService;

    @GetMapping("/subscription-plans/find-all")
    public ResponseEntity<List<SubscriptionPlanDTO>> getSubscriptionPlans(){
        return ResponseEntity.ok(subscriptionPlanService.getAll());
    }
}
