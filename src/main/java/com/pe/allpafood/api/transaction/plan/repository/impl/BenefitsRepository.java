package com.pe.allpafood.api.transaction.plan.repository.impl;

import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.BenefitsEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.SubscriptionPlanEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BenefitsRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<BenefitsEntity> finAll() {
        String sql = """
            SELECT DISTINCT
                s.id AS subscriptionPlanId,
                s.description,
                s.real_price,
                s.previous_price AS previousPrice,
                s.level,
                s.available," +
                b.detail AS benefitsDetail,
                s.properties
                FROM 
                    tbl_subscription_plan s
                LEFT JOIN 
                    tbl_benefits b
                ON 
                    s.id = b.subscription_plan_id WHERE s.available=true AND b.assigned_plan=true;""";
        return namedParameterJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BenefitsEntity.class));
    }
}
