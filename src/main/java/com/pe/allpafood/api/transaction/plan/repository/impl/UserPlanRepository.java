package com.pe.allpafood.api.transaction.plan.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;

import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.utils.dto.PageResult;
import com.pe.allpafood.api.core.utils.dto.PaginationUtil;
import com.pe.allpafood.api.gateway.admin.plans.dto.*;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;
import com.pe.allpafood.api.transaction.plan.entities.ObjectiveMetric;
import com.pe.allpafood.api.transaction.plan.entities.SummaryWeek;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;

import com.pe.allpafood.api.transaction.plan.repository.IUserPlanRepository;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserPlanRepository implements IUserPlanRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public UserPlanEntity findByUserId(String userId){
        String sql = """
                SELECT\s
                   benefits_id,\
                   objectives_registration,\
                   summary_week \
                FROM\s
                    tbl_plan_user WHERE user_id = ?;
                """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                UserPlanEntity userPlanEntity = new UserPlanEntity();
                userPlanEntity.setUserId(userId);
                userPlanEntity.setBenefitsId(rs.getInt("benefits_id"));

                List<ObjectiveMetric> objectives = JsonUtil.convertToObjectList(rs.getString("objectives_registration"), new TypeReference<>(){});
                userPlanEntity.setObjectivesRegistration(objectives);

                List<SummaryWeek> summaryWeek = JsonUtil.convertToObjectList(rs.getString("summary_week"), new TypeReference<>(){});
                userPlanEntity.setSummaryWeek(summaryWeek);
                return userPlanEntity;
            }, userId);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public Integer findBenefitsIdByUserIAndExpirationDate(String userId, LocalDate date){
        try{
            String sql = "SELECT b.id FROM tbl_benefits b INNER JOIN tbl_plan_user u ON b.id = u.benefits_id " +
                    "WHERE u.user_id = ? AND u.plan_expiration_date > ?;";
            return jdbcTemplate.queryForObject(sql, Integer.class, userId, Date.valueOf(date));
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public boolean existByUserIdAndExpDate(String userId, LocalDate today){
        String sql = "SELECT COUNT(*) FROM tbl_plan_user WHERE user_id = ? AND plan_expiration_date > ?;";

        try{
            Integer result = jdbcTemplate.queryForObject(sql,Integer.class,userId, Date.valueOf(today));
            return result != null  && result>0;
        }catch (EmptyResultDataAccessException e){
            return false;
        }
    }

    @Override
    public void updateNeedDay(UserPlanEntity userPlan) {
        String sql = "INSERT INTO tbl_plan_user (user_id, need_day) " +
                "VALUES (:userId, :needDay) " +
                "ON DUPLICATE KEY UPDATE " +
                "need_day =  IFNULL(:needDay, need_day);";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userPlan.getUserId());
        params.addValue("needDay", userPlan.getNeedDay());
        namedParameterJdbcTemplate.update(sql,params);
    }

    @Override
    public String findNeedDayByUserId(String userId) {
        String sql = "SELECT need_day FROM tbl_plan_user WHERE user_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public List<UserPlanEntity> getAllUserPlansAvailableByDate(LocalDate date) {
        String sql = """
            SELECT
                user_id,
                plan_init_date,
                plan_expiration_date,
                consumed_benefits,
                credits
            FROM tbl_plan_user pu
            WHERE plan_expiration_date > ?;
        """;
        return jdbcTemplate.query(sql,(rs, rowNum) -> {
            UserPlanEntity userPlanEntity = new UserPlanEntity();
            userPlanEntity.setUserId(rs.getString("user_id"));

            Date init = rs.getDate("plan_expiration_date");
            userPlanEntity.setPlanInitDate(init != null ? init.toLocalDate() : null);
            Date expiration = rs.getDate("plan_expiration_date");
            userPlanEntity.setPlanExpirationDate(expiration != null ? expiration.toLocalDate() : null);

            String consumedJson = rs.getString("consumed_benefits");
            userPlanEntity.setConsumedBenefits(JsonUtil.convertToObjectList(consumedJson, new TypeReference<>(){}));

            String creditsJson = rs.getString("credits");
             if (creditsJson != null)
                 userPlanEntity.setCredits(JsonUtil.convertToObjectList(creditsJson, new TypeReference<>(){}));

            return userPlanEntity;
        },Date.valueOf(date));
    }

    @Override
    public UserPlanEntity findBenefitsConsumptionByUserId(String userId){

        try {
            log.info("[findBenefitsConsumptionByUserId] Find Benefits... {}", userId);
            String sql = """
                SELECT\
                   benefits_id,
                   consumed_benefits,
                   plan_expiration_date,
                   plan_init_date,
                   credits
                FROM\s
                   tbl_plan_user WHERE user_id = ?;
                """;

            return jdbcTemplate.queryForObject(sql,(rs,rowNum) -> {
                UserPlanEntity userPlanEntity = new UserPlanEntity();
                userPlanEntity.setBenefitsId(rs.getInt(1));
                String json = rs.getString(2);
                userPlanEntity.setConsumedBenefits(JsonUtil.convertToObject(json, ConsumeBenefits.class));

                Date init = rs.getDate(3);
                userPlanEntity.setPlanExpirationDate(init != null ? init.toLocalDate() : null);

                Date expiration = rs.getDate(4);
                userPlanEntity.setPlanInitDate(init != null ? expiration.toLocalDate() : null);

                String json2 = rs.getString(5);

                if (json2 != null) userPlanEntity.setCredits(JsonUtil.convertToObject(json2, ConsumeBenefits.class));

                return userPlanEntity;
            },userId);
        }catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

    @Override
    public List<UserPlanEntity>  findAllDataByMajorDate(LocalDate date){

        try {
            String sql = """
                SELECT
                   pu.benefits_id,
                   pu.consumed_benefits,
                   pu.plan_expiration_date,
                   pu.plan_init_date,
                   pu.credits,
                   pu.user_id,
                   u.phone_number,
                   p.name
                FROM
                   tbl_plan_user pu
                INNER JOIN
                   tbl_user u
                ON u.id = pu.user_id
                INNER JOIN
                   tbl_profile p
                ON u.id = p.user_id
                WHERE pu.plan_expiration_date > ?;
                """;

            return getUserPlanEntities(date, sql);
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<UserPlanEntity>  findAllDataByRangeDate(LocalDate minor, LocalDate major){

        try {
            String sql = """
                SELECT
                   pu.benefits_id,
                   pu.consumed_benefits,
                   pu.plan_expiration_date,
                   pu.plan_init_date,
                   pu.credits,
                   pu.user_id,
                   u.phone_number,
                   p.name
                FROM
                   tbl_plan_user pu
                INNER JOIN
                   tbl_user u
                ON u.id = pu.user_id
                INNER JOIN
                   tbl_profile p
                ON u.id = p.user_id
                WHERE pu.plan_expiration_date BETWEEN ? AND ?;
                """;

            return jdbcTemplate.query(sql,(rs, rowNum) -> {
                UserPlanEntity userPlanEntity = new UserPlanEntity();
                userPlanEntity.setUserId(rs.getString("pu.user_id"));

                Date init = rs.getDate("pu.plan_expiration_date");
                userPlanEntity.setPlanInitDate(init != null ? init.toLocalDate() : null);
                Date expiration = rs.getDate("pu.plan_expiration_date");
                userPlanEntity.setPlanExpirationDate(expiration != null ? expiration.toLocalDate() : null);

                String consumedJson = rs.getString("pu.consumed_benefits");
                userPlanEntity.setConsumedBenefits(JsonUtil.convertToObjectList(consumedJson, new TypeReference<>(){}));

                String creditsJson = rs.getString("pu.credits");
                if (creditsJson != null)
                    userPlanEntity.setCredits(JsonUtil.convertToObjectList(creditsJson, new TypeReference<>(){}));

                UserEntity userEntity = new UserEntity();
                userEntity.setPhoneNumber(rs.getString("u.phone_number"));
                ProfileEntity profile = new ProfileEntity();
                profile.setName(rs.getString("p.name"));
                userEntity.setProfile(profile);
                userPlanEntity.setUser(userEntity);

                return userPlanEntity;
            },minor, major);
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<UserPlanEntity>  findAllDataByDate(LocalDate date){

        try {
            String sql = """
                SELECT
                   pu.benefits_id,
                   pu.consumed_benefits,
                   pu.plan_expiration_date,
                   pu.plan_init_date,
                   pu.credits,
                   pu.user_id,
                   u.phone_number,
                   p.name
                FROM
                   tbl_plan_user pu
                INNER JOIN
                   tbl_user u
                ON u.id = pu.user_id
                INNER JOIN
                   tbl_profile p
                ON u.id = p.user_id
                WHERE pu.plan_expiration_date = ?;
                """;

            return getUserPlanEntities(date, sql);
        }catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

    @NotNull
    private List<UserPlanEntity> getUserPlanEntities(LocalDate date, String sql) {
        return jdbcTemplate.query(sql,(rs, rowNum) -> {
            UserPlanEntity userPlanEntity = new UserPlanEntity();
            userPlanEntity.setUserId(rs.getString("pu.user_id"));

            Date init = rs.getDate("pu.plan_expiration_date");
            userPlanEntity.setPlanInitDate(init != null ? init.toLocalDate() : null);
            Date expiration = rs.getDate("pu.plan_expiration_date");
            userPlanEntity.setPlanExpirationDate(expiration != null ? expiration.toLocalDate() : null);

            String consumedJson = rs.getString("pu.consumed_benefits");
            userPlanEntity.setConsumedBenefits(JsonUtil.convertToObjectList(consumedJson, new TypeReference<>(){}));

            String creditsJson = rs.getString("pu.credits");
            if (creditsJson != null)
                userPlanEntity.setCredits(JsonUtil.convertToObjectList(creditsJson, new TypeReference<>(){}));

            UserEntity userEntity = new UserEntity();
            userEntity.setPhoneNumber(rs.getString("u.phone_number"));
            ProfileEntity profile = new ProfileEntity();
            profile.setName(rs.getString("p.name"));
            userEntity.setProfile(profile);
            userPlanEntity.setUser(userEntity);

            return userPlanEntity;
        },date);
    }

    @Override
    public List<ObjectiveMetric> getObjectiveRegistrationByUserId(String userId) {
        String sql = """
                SELECT\s
                   objectives_registration \
                FROM\s
                    tbl_plan_user WHERE user_id = ?;
                """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> JsonUtil.convertToObjectList(rs.getString("objectives_registration"), new TypeReference<>(){}), userId);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public void updateObjectiveMetrics(List<ObjectiveMetric> objectiveMetrics, String userId){
        String sql = "UPDATE tbl_plan_user SET objectives_registration = ? WHERE user_id = ?;";
        jdbcTemplate.update(sql, JsonUtil.convertToJsonString(objectiveMetrics),userId);
    }

    @Override
    public void updateBenefitsConsumption(String userId, ConsumeBenefits consumption){
        String sql = "UPDATE tbl_plan_user " +
                "SET consumed_benefits = ? " +
                "WHERE user_id = ?;";
        jdbcTemplate.update(sql, JsonUtil.convertToJsonString(consumption),userId);
    }

    @Override
    public void updateBenefitsAndCredits(UserPlanEntity userPlan){
        String sql = "UPDATE tbl_plan_user " +
                "SET consumed_benefits = ?, credits = ? " +
                "WHERE user_id = ?;";
        jdbcTemplate.update(sql,
                userPlan.getConsumedBenefits() != null
                        ? JsonUtil.convertToJsonString(userPlan.getConsumedBenefits())
                        : null,
                userPlan.getCredits() != null
                        ? JsonUtil.convertToJsonString(userPlan.getCredits())
                        : null,
                userPlan.getUserId());
    }

    @Override
    public void saveUserPlanRepository(String userId, UserPlanEntity userPlan) {

        String consumedBenefits = (userPlan.getConsumedBenefits() != null) ? JsonUtil.convertToJsonString(userPlan.getConsumedBenefits()) : null;
        String credits = (userPlan.getCredits() != null) ? JsonUtil.convertToJsonString(userPlan.getCredits()) : null;

        String sql = "INSERT INTO tbl_plan_user (" +
                "    user_id," +
                "    benefits_id," +
                "    plan_init_date," +
                "    plan_expiration_date," +
                "    consumed_benefits," +
                "    credits,"+
                "    need_day," +
                "    objectives_registration," +
                "    summary_week"+
                ") VALUES (" +
                "    ?, ?, ?, ?, ?, ?,'[]','[]','[]'" +
                ") ON DUPLICATE KEY UPDATE" +
                "    benefits_id = VALUES(benefits_id)," +
                "    plan_init_date = VALUES(plan_init_date)," +
                "    plan_expiration_date = VALUES(plan_expiration_date)," +
                "    consumed_benefits = VALUES(consumed_benefits)," +
                "    credits = VALUES(credits);";

        jdbcTemplate.update(sql,
                userId,
                userPlan.getBenefitsId(),
                userPlan.getPlanInitDate(),
                userPlan.getPlanExpirationDate(),
                consumedBenefits,
                credits
        );
    }

    @Override
    public PageResult<UserPlanDTO> findUsersWithPlan(String search, int page, int size) {
        int[] validated = PaginationUtil.validatePageAndSize(page, size);
        page = validated[0];
        size = validated[1];

        int offset = (page - 1) * size;

        StringBuilder countSql = new StringBuilder("""
        SELECT COUNT(DISTINCT u.id)
        FROM tbl_user u
        LEFT JOIN tbl_profile p
            ON p.user_id = u.id
        LEFT JOIN tbl_plan_user pu
            ON pu.user_id = u.id
          LEFT JOIN tbl_benefits b
                ON b.id = pu.benefits_id
        LEFT JOIN tbl_subscription_plan sp
                ON sp.id = b.subscription_plan_id
        LEFT JOIN tbl_user_role ur
            ON ur.user_id = u.id
        WHERE ur.role_id = 1 AND u.verified AND u.registration_completed AND u.profile_completed 
    """);

        StringBuilder dataSql = new StringBuilder("""
        SELECT
            u.id AS user_id,
            u.phone_number,
            u.email,
            u.document_number,
            u.status,
            u.register_date,

            p.user_id AS profile_user_id,
            p.name,
            p.lastname,
            p.born_date,
            p.district,
            p.address,
            p.information,
            p.image_url,
            p.recommended_plan_id,

            pu.benefits_id,
            pu.plan_init_date,
            pu.plan_expiration_date,
            pu.consumed_benefits,
            pu.credits,
            sp.id AS sub_plan_id,
            sp.description AS plan_description,
            inv.payment_method,
            inv.tota_price
            FROM tbl_user u
            LEFT JOIN tbl_profile p
                ON p.user_id = u.id
            LEFT JOIN tbl_plan_user pu
                ON pu.user_id = u.id
            LEFT JOIN tbl_benefits b
                    ON b.id = pu.benefits_id
            LEFT JOIN tbl_subscription_plan sp
                    ON sp.id = b.subscription_plan_id
            LEFT JOIN tbl_invoice inv
                ON inv.id = (
                    SELECT MAX(i.id)
                    FROM tbl_invoice i
                    WHERE i.user_id = u.id
                )
            LEFT JOIN tbl_user_role ur
                ON ur.user_id = u.id
                
            WHERE ur.role_id = 1 AND u.verified AND u.registration_completed AND u.profile_completed 
    """);

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            countSql.append("""
            AND (
                u.id LIKE ?
                OR u.email LIKE ?
                OR u.phone_number LIKE ?
                OR p.name LIKE ?
                OR p.lastname LIKE ?
            )
        """);

            dataSql.append("""
            AND (
                u.id LIKE ?
                OR u.email LIKE ?
                OR u.phone_number LIKE ?
                OR p.name LIKE ?
                OR p.lastname LIKE ?
            )
        """);

            String value = "%" + search.trim() + "%";
            params.add(value);
            params.add(value);
            params.add(value);
            params.add(value);
            params.add(value);
        }

        dataSql.append("""
        ORDER BY u.register_date DESC
        LIMIT ? OFFSET ?
    """);

        Long totalElements = jdbcTemplate.queryForObject(
                countSql.toString(),
                params.toArray(),
                Long.class
        );

        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(size);
        dataParams.add(offset);

        List<UserPlanDTO> content = jdbcTemplate.query(
                dataSql.toString(),
                dataParams.toArray(),
                (rs, rowNum) -> {
                    UserPlanDTO userPlanEntity = new UserPlanDTO();

                    userPlanEntity.setUserId(rs.getString("user_id"));
                    userPlanEntity.setBenefitsId(rs.getObject("benefits_id", Integer.class));
                    if (rs.getObject("benefits_id") != null){
                        BenefitsDTO benefitsEntity = new BenefitsDTO();
                        benefitsEntity.setId(userPlanEntity.getBenefitsId());
                        SubscriptionPlanDTO subscriptionPlanDTO = new SubscriptionPlanDTO();
                        subscriptionPlanDTO.setId(rs.getInt("sub_plan_id"));
                        subscriptionPlanDTO.setDescription(rs.getString("plan_description"));

                        benefitsEntity.setSubscriptionPlan(subscriptionPlanDTO);
                        userPlanEntity.setBenefits(benefitsEntity);
                    }

                    Date init = rs.getDate("plan_init_date");
                    userPlanEntity.setPlanInitDate(init != null ? init.toLocalDate() : null);

                    Date expiration = rs.getDate("plan_expiration_date");
                    userPlanEntity.setPlanExpirationDate(expiration != null ? expiration.toLocalDate() : null);

                    userPlanEntity.setPaymentMethod(
                        rs.getString("payment_method")
                    );

                    userPlanEntity.setTotalPrice(
                        rs.getBigDecimal("tota_price")
                    );


                    String consumedJson = rs.getString("consumed_benefits");
                    if (consumedJson != null) {
                        userPlanEntity.setConsumedBenefits(
                                JsonUtil.convertToObjectList(
                                        consumedJson,
                                        new TypeReference<ConsumeBenefits>() {}
                                )
                        );
                    }

                    String creditsJson = rs.getString("credits");
                    if (creditsJson != null) {
                        userPlanEntity.setCredits(
                                JsonUtil.convertToObjectList(
                                        creditsJson,
                                        new TypeReference<ConsumeBenefits>() {}
                                )
                        );
                    }
                    UserDTO user = new UserDTO();
                    user.setId(rs.getString("user_id"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                    user.setEmail(rs.getString("email"));
                    user.setDocumentNumber(rs.getString("document_number"));
                    user.setStatus(rs.getObject("status", Integer.class));

                    Date registerDate = rs.getDate("register_date");
                    user.setRegisterDate(registerDate != null ? registerDate.toLocalDate() : null);     

                    if (rs.getString("profile_user_id") != null){
                        ProfileDTO profile = new ProfileDTO();
                        profile.setUserId(rs.getString("profile_user_id"));
                        profile.setName(rs.getString("name"));
                        profile.setLastname(rs.getString("lastname"));

                        Date bornDate = rs.getDate("born_date");
                        profile.setBornDate(bornDate != null ? bornDate.toLocalDate() : null);

                        profile.setDistrict(rs.getString("district"));
                        profile.setAddress(rs.getString("address"));
                        profile.setInformation(rs.getString("information"));
                        profile.setRecommendedPlanId(rs.getObject("recommended_plan_id", Integer.class));

                        user.setProfile(profile);
                    }
                    userPlanEntity.setUser(user);

                    return userPlanEntity;
                }
        );

        return new PageResult<>(
                content,
                page,
                size,
                totalElements != null ? totalElements : 0L
        );
    }

    @Override
    public void updatePlanUser(UserPlanEntity entity) {

        String sql = """
        UPDATE tbl_plan_user
        SET
            plan_init_date = IFNULL(:planInitDate, plan_init_date),
            plan_expiration_date = IFNULL(:planExpirationDate, plan_expiration_date),
            benefits_id = IFNULL(:benefitsId, benefits_id),
            consumed_benefits = IFNULL(:consumedBenefitsJson, consumed_benefits),
            credits = IFNULL(:creditsJson, credits),
            modified_at = NOW(),
            modified_by = IFNULL(:modifiedBy, modified_by)
        WHERE user_id = :userId
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("benefitsId",entity.getBenefitsId())
                .addValue("userId", entity.getUserId())
                .addValue("planInitDate", entity.getPlanInitDate())
                .addValue("planExpirationDate", entity.getPlanExpirationDate())
                .addValue("consumedBenefitsJson", entity.getConsumedBenefitsJson())
                .addValue("creditsJson", entity.getCreditsJson())
                .addValue("modifiedBy", entity.getModifiedBy());
        log.info("params : {}", params);

        var upd = namedParameterJdbcTemplate.update(sql, params);
        log.info("upd {}", upd);
    }
}
