package com.pe.allpafood.api.transaction.plan.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.transaction.plan.entities.benefits.BenefitsEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.SubscriptionPlanEntity;
import com.pe.allpafood.api.transaction.plan.repository.ISubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanRepository implements ISubscriptionPlanRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SubscriptionPlanEntity> findAvailablePlans() {
        String sql = "SELECT DISTINCT\n" +
                "    s.id AS subscriptionPlanId,\n" +
                "    s.description,\n" +
                "    s.real_price,\n" +
                "    s.previous_price AS previousPrice,\n" +
                "    s.level,\n" +
                "    s.available," +
                "    b.detail AS benefitsDetail,\n" +
                "    b.benefits_period,\n" +
                "    b.consumption_total,\n" +
                "    b.extra_benefits,\n" +
                "    b.principal_benefits,\n" +
                "    s.properties\n" +
                "FROM \n" +
                "    tbl_subscription_plan s\n" +
                "LEFT JOIN \n" +
                "    tbl_benefits b\n" +
                "ON \n" +
                "    s.id = b.subscription_plan_id WHERE s.available=true AND b.assigned_plan=true;\n";
        return jdbcTemplate.query(sql, this::mapSubscriptionRow);
    }

    public Integer findRecommendedPlanIdByUserId(String userId) {
        String sql = "SELECT recommended_plan_id FROM tbl_profile WHERE user_id = ?;";
        return jdbcTemplate.queryForObject(sql,Integer.class,userId);
    }

    @Override
    public SubscriptionPlanEntity findSubscriptionBenefitsById(int benefitId) {
        String sql = "SELECT sp.description, b.subscription_plan_id " +
                "FROM tbl_benefits b " +
                "INNER JOIN tbl_subscription_plan sp ON b.subscription_plan_id = sp.id " +
                "WHERE b.id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                SubscriptionPlanEntity subscriptionPlanEntity = new SubscriptionPlanEntity();
                subscriptionPlanEntity.setId(rs.getInt("subscription_plan_id"));
                subscriptionPlanEntity.setDescription(rs.getString("description"));
                return subscriptionPlanEntity;
            }, benefitId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public SubscriptionPlanEntity findPriceByPlanId(int planId) {
        String sql = "SELECT sp.real_price, sp.description, b.subscription_plan_id, sp.discount_amount " +
                "FROM tbl_benefits b " +
                "INNER JOIN tbl_subscription_plan sp ON b.subscription_plan_id = sp.id " +
                "WHERE sp.id = ? AND sp.available=true AND b.assigned_plan = true;";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            SubscriptionPlanEntity subscriptionPlanEntity = new SubscriptionPlanEntity();
            subscriptionPlanEntity.setDescription(rs.getString("description"));
            subscriptionPlanEntity.setId(rs.getInt("subscription_plan_id"));
            subscriptionPlanEntity.setRealPrice(rs.getFloat("real_price"));
            subscriptionPlanEntity.setDiscountAmount(rs.getFloat("discount_amount"));
            return subscriptionPlanEntity;
        }, planId);
    }

    @Override
    public BenefitsEntity findBenefitsByPlanId(int planId) {

        String sql = "SELECT b.id, b.benefits_period,b.consumption_total, b.extra_benefits, b.principal_benefits " +
                "FROM tbl_benefits b " +
                "WHERE b.subscription_plan_id = ? and assigned_plan = true;";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                BenefitsEntity benefitsEntity = new BenefitsEntity();
                benefitsEntity.setId(rs.getInt("id"));
                benefitsEntity.setConsumptionTotal(rs.getInt("consumption_total"));
                benefitsEntity.setBenefitsPeriod(rs.getInt("benefits_period"));

                String json = rs.getString("extra_benefits");
                benefitsEntity.setExtraBenefits(JsonUtil.convertToObjectList(json, new TypeReference<List<String>>() {}));
                String jsonPrincipal = rs.getString("principal_benefits");
                benefitsEntity.setPrincipalBenefits(JsonUtil.convertToObjectList(jsonPrincipal, new TypeReference<List<String>>() {}));
                return benefitsEntity;
            }, planId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Float findRealPriceByPlanId(int planId) {
        try {
            String sql = "SELECT real_price FROM tbl_subscription_plan WHERE id = ? ;";
            return jdbcTemplate.queryForObject(sql,Float.class,planId);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    private SubscriptionPlanEntity mapSubscriptionRow(ResultSet rs, int rowNum) throws SQLException {
        SubscriptionPlanEntity entity = new SubscriptionPlanEntity();
        entity.setId(rs.getInt(1));
        entity.setDescription(rs.getString(2));
        entity.setRealPrice(rs.getFloat(3));
        entity.setPreviousPrice(rs.getDouble(4));
        entity.setLevel(rs.getString(5));
        entity.setAvailable(rs.getBoolean(6));

        BenefitsEntity benefitsEntity = new BenefitsEntity();
        benefitsEntity.setDetail(JsonUtil.convertToObjectList(rs.getString(7), new TypeReference<List<DetailEntity<Integer>>>() {}));
        benefitsEntity.setBenefitsPeriod(rs.getInt(8));

        benefitsEntity.setConsumptionTotal(rs.getInt(9));

        benefitsEntity.setExtraBenefits(JsonUtil.convertToObjectList(rs.getString(10), new TypeReference<List<String>>() {}));
        benefitsEntity.setPrincipalBenefits(JsonUtil.convertToObjectList(rs.getString(11), new TypeReference<List<String>>() {}));

        entity.setBenefits(benefitsEntity);
        String propertiesJson = rs.getString(12);
        entity.setPropertiesEntity(JsonUtil.convertToObjectList(propertiesJson, new TypeReference<List<DetailEntity<Float>>>() {}));

        return entity;
    }


}
