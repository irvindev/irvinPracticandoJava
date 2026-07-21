package com.pe.allpafood.api.transaction.order.repository.impl;

import com.pe.allpafood.api.core.utils.dto.GeoPoint;
import com.pe.allpafood.api.transaction.order.entity.DeliveryPointEntity;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Repository
@RequiredArgsConstructor
@Slf4j
public class DeliveryRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public Long insertDeliveryPointAddress(DeliveryPointEntity entity) {
        log.info("[insertDeliveryPointAddress] Insert DeliveryPointAddress");
        String sql = """
            INSERT INTO tbl_delivery_point (
                user_id,
                address,
                description,
                location,
                assigned,
                active,
                district
            ) VALUES (
                :userId,
                :address,
                :description,
                ST_GeomFromText(:location),
                :assigned,
                true,
                :district
            );
            """;

        BeanPropertySqlParameterSource parameters = new BeanPropertySqlParameterSource(entity);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void updateActiveByUserIdAndId(String userId, long deliveryPointId, boolean active){
        String sql = "UPDATE tbl_delivery_point SET active = ? WHERE user_id = ? AND id = ?;";
        jdbcTemplate.update(sql,active,userId,deliveryPointId);
    }

    public DeliveryPointEntity findByUserIdAndAssigned(String userId){
        String sql = """
            SELECT
                id,
                user_id,
                address,
                description,
                ST_ASTEXT(location) AS location,
                district
            FROM
                tbl_delivery_point
            WHERE user_id = :userId and active = true AND assigned = true
            LIMIT 1;
        """;

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource("userId", userId), new BeanPropertyRowMapper<>(DeliveryPointEntity.class));
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public Long findIdAByUserIdAndAssigned(String userId){
        String sql = "SELECT id FROM tbl_delivery_point WHERE user_id = :userId and active = true AND assigned = true LIMIT 1;";

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource("userId", userId), Long.class);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public boolean existsIdsAndUserId(List<Long> ids, String userId) {
        if (ids == null || ids.isEmpty()) return true; // or false, depending on your rule

        String sql = "SELECT COUNT(*) FROM tbl_delivery_point WHERE id IN (:ids) AND user_id = :userId";
        Map<String, Object> params = Map.of("ids", ids,"userId",userId);

        Long count = namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null && count == ids.size();
    }

    public List<DeliveryPointEntity> findByUserId(String userId){
        String sql = "SELECT id, user_id, address, description, ST_AsText(location) AS location, district, assigned FROM tbl_delivery_point WHERE user_id=? AND active = true;";
        return jdbcTemplate.query(sql, (rs,rowNum) -> {
            DeliveryPointEntity deliveryPoint = new DeliveryPointEntity();
            deliveryPoint.setId(rs.getLong("id"));
            deliveryPoint.setUserId(rs.getString("user_id"));
            deliveryPoint.setAddress(rs.getString("address"));
            deliveryPoint.setDescription(rs.getString("description"));
            String point = rs.getString("location");
            deliveryPoint.setGeoLocation(new GeoPoint(point));
            deliveryPoint.setAssigned(rs.getBoolean("assigned"));
            deliveryPoint.setDistrict(rs.getString("district"));
            return deliveryPoint;
        },userId);
    }

    public boolean existsByIdAndUserId(int id, String userId) {
        String sql = "SELECT COUNT(*) FROM tbl_delivery_point WHERE id = ? AND user_id = ? AND active = true;";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class , id, userId);
        return count != null && count > 0;
    }

    public void updateAssignedByUserId(String userId, boolean assigned){
        String sql = "UPDATE tbl_delivery_point SET assigned = ? WHERE user_id = ?;";
        jdbcTemplate.update(sql,assigned,userId);
    }

    public void updateAssignedByUserIdAndId(String userId, Long id, boolean assigned){
        String sql = "UPDATE tbl_delivery_point SET assigned = ? WHERE user_id = ? AND id = ?;";
        jdbcTemplate.update(sql,assigned,userId, id);
    }
}
