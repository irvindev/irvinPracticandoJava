package com.pe.allpafood.api.transaction.user.repository;

import com.pe.allpafood.api.transaction.user.entities.InformationEntity;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;


@Repository
@RequiredArgsConstructor
@Slf4j
public class ProfileRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public void saveProfile(ProfileEntity profile) {
        log.info("Init saveProfile con el user: {}",profile.getUserId());

        String sql = "INSERT INTO tbl_profile (" +
                "    user_id, name, lastname, born_date, district, address, information, recommended_plan_id\n" +
                ") \n" +
                "VALUES (\n" +
                "    :userId, COALESCE(:name, ''), COALESCE(:lastname, ''), :bornDate, :district, :address, :information, :recommendedPlanId\n" +
                ")\n" +
                "ON DUPLICATE KEY UPDATE\n" +
                "    name = IFNULL(:name, name),\n" +
                "    lastname = IFNULL(:lastname, lastname),\n" +
                "    born_date = IFNULL(:bornDate, born_date),\n" +
                "    district = IFNULL(:district, district),\n" +
                "    address = IFNULL(:address, address),\n" +
                "    information = IFNULL(:information, information)," +
                "    modified_at = IFNULL(:modifiedAt,modified_at)," +
                "    modified_by = IFNULL(:modifiedBy,modified_by)," +
                "    recommended_plan_id = IFNULL(:recommendedPlanId, recommended_plan_id);";
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(profile);
        namedParameterJdbcTemplate.update(sql, params);
    }

    public void updateBasicProfile(String name, String lastname,String gender,String userId, String image) {
        String sql = "UPDATE tbl_profile " +
                "SET information = JSON_SET(information, '$.gender', ?), name = ?, lastname = ?, image_url = ? " +
                "WHERE user_id = ?";

        jdbcTemplate.update( sql, gender, name, lastname, image, userId);
    }

    public ProfileEntity findByUserId(String userId) {
        try {
            log.info("Init find profile {}", userId);
            String sql = "SELECT * FROM tbl_profile WHERE user_id = :userId";
            SqlParameterSource params = new MapSqlParameterSource("userId", userId);
            return namedParameterJdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(ProfileEntity.class));
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public ProfileEntity findAgeAndHeightByUserId(String userId){
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource("user_id", userId);
        String sql = "SELECT " +
                "born_date," +
                "JSON_EXTRACT(information, '$.height') AS height " +
                "FROM " +
                "tbl_profile " +
                "WHERE user_id = :user_id";
        return namedParameterJdbcTemplate.queryForObject(sql, sqlParameterSource, (rs,rowNum)->{
            ProfileEntity userProfile = new ProfileEntity();
            userProfile.setUserId(userId);
            userProfile.setBornDate(rs.getDate("born_date").toLocalDate());
            InformationEntity information = new InformationEntity();
            information.setHeight(rs.getInt("height"));
            userProfile.setInformationDeserializer(information);
            return userProfile;
        });
    }

    public void updateAddressAndBornDate(String userId,String address,LocalDate borndate) {
        String sql = "UPDATE tbl_profile SET address=?, born_date=? WHERE user_id = ?;";
        jdbcTemplate.update(sql,address, Date.valueOf(borndate),userId);
    }

    public void updateMotorizedProfile(String userId, String name, String lastname, String district) {
        String sql = "UPDATE tbl_profile SET " +
                "name = IFNULL(:name, name), " +
                "lastname = IFNULL(:lastname, lastname), " +
                "district = IFNULL(:district, district) " +
                "WHERE user_id = :userId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("lastname", lastname)
                .addValue("district", district)
                .addValue("userId", userId);

        namedParameterJdbcTemplate.update(sql, params);
    }

    public List<ProfileEntity> getAilmentsRestrictionsByUserIds(List<String> userIds) {
        String sql = "SELECT user_id, name,lastname, JSON_UNQUOTE(JSON_EXTRACT(information, '$.alimentsRestrictions')) AS ailments_restrictions " +
                "FROM allpa_food_db.tbl_profile " +
                "WHERE user_id IN (:userIds) " +
                "AND JSON_UNQUOTE(JSON_EXTRACT(information, '$.alimentsRestrictions')) IS NOT NULL " +
                "AND JSON_UNQUOTE(JSON_EXTRACT(information, '$.alimentsRestrictions')) != ''";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userIds", userIds);

        return namedParameterJdbcTemplate.query(sql, parameters, (rs,rowNum)->{
            ProfileEntity userProfile = new ProfileEntity();
            userProfile.setUserId(rs.getString("user_id"));
            userProfile.setName(rs.getString("name"));
            userProfile.setLastname(rs.getString("lastname"));
            InformationEntity information = new InformationEntity();
            information.setAlimentsRestrictions(rs.getString("ailments_restrictions"));
            userProfile.setInformationDeserializer(information);
            return userProfile;
        });
    }

    public ProfileEntity findPrivacyDataByUserId(String userId) {
        String sql  = "SELECT " +
                " u.document_number AS document_number," +
                " u.email AS email," +
                " pr.name AS name," +
                " pr.lastname AS lastname," +
                " pr.address AS address," +
                " u.phone_number AS phone_number," +
                " pr.born_date  AS born_date " +
                "FROM tbl_user u INNER JOIN tbl_profile pr ON pr.user_id = u.id WHERE u.id=?;";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(ProfileEntity.class),userId);
    }

    public ProfileEntity findPersonalDataByUserId(String userId) {
        String sql  = "SELECT pr.name, pr.lastname, pr.image_url, pr.information, u.register_date  FROM tbl_user u INNER JOIN tbl_profile pr ON pr.user_id = u.id WHERE u.id=?;";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(ProfileEntity.class),userId);
    }
}