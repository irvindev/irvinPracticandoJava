// Agregar este método en la clase SubscriptionPlanRepository.java
// Ubicación: src/main/java/com/pe/allpafood/api/transaction/plan/repository/impl/SubscriptionPlanRepository.java

@Override
public void updatePlanAndBenefits(SubscriptionPlanEntity entity) {
    // ============================================
    // 1. UPDATE tbl_subscription_plan
    // ============================================
    String sqlPlan = """
        UPDATE tbl_subscription_plan
        SET description = IFNULL(:description, description),
            real_price = IFNULL(:realPrice, real_price),
            previous_price = IFNULL(:previousPrice, previous_price),
            level = IFNULL(:level, level),
            properties = IFNULL(CAST(:properties AS JSON), properties),
            description_list = IFNULL(CAST(:descriptionList AS JSON), description_list)
        WHERE id = :id
        """;

    MapSqlParameterSource planParams = new MapSqlParameterSource()
            .addValue("id", entity.getId())
            .addValue("description", entity.getDescription())
            .addValue("realPrice", entity.getRealPrice())
            .addValue("previousPrice", entity.getPreviousPrice())
            .addValue("level", entity.getLevel())
            .addValue("properties", entity.getProperties())
            .addValue("descriptionList", entity.getDescriptionList());

    try {
        namedParameterJdbcTemplate.update(sqlPlan, planParams);
    } catch (Exception e) {
        throw new RuntimeException("Error actualizando plan con ID: " + entity.getId(), e);
    }

    // ============================================
    // 2. UPDATE tbl_benefits (si se proporciona)
    // ============================================
    if (entity.getBenefits() != null) {
        String sqlBenefits = """
            UPDATE tbl_benefits
            SET extra_benefits = IFNULL(CAST(:extraBenefits AS JSON), extra_benefits),
                principal_benefits = IFNULL(CAST(:principalBenefits AS JSON), principal_benefits)
            WHERE subscription_plan_id = :planId 
              AND assigned_plan = true
            LIMIT 1
            """;

        MapSqlParameterSource benefitsParams = new MapSqlParameterSource()
                .addValue("planId", entity.getId())
                .addValue("extraBenefits", entity.getBenefits().getExtraBenefitsJson())
                .addValue("principalBenefits", entity.getBenefits().getPrincipalBenefitsJson());

        try {
            namedParameterJdbcTemplate.update(sqlBenefits, benefitsParams);
        } catch (Exception e) {
            throw new RuntimeException("Error actualizando benefits para plan ID: " + entity.getId(), e);
        }
    }
}
