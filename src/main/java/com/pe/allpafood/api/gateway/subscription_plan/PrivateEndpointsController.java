package com.pe.allpafood.api.gateway.subscription_plan;

import com.pe.allpafood.api.transaction.plan.bussiness.IPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/private/catalog")
@RequiredArgsConstructor
public class PrivateEndpointsController {

    private final IPlanService subscriptionPlanService;

    @GetMapping("/subscription-plans/recommended")
    public ResponseEntity<?> getSubscriptionPlans(@RequestAttribute("userId") String userId){
        return ResponseEntity.ok(subscriptionPlanService.getPlanRecommended(userId));
    }


}
