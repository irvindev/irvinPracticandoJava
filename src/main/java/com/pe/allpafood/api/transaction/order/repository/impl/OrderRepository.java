package com.pe.allpafood.api.transaction.order.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pe.allpafood.api.core.utils.dto.GeoPoint;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.enums.StatusDeliveryEnum;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import com.pe.allpafood.api.transaction.order.entity.OrderSummary;
import com.pe.allpafood.api.transaction.order.entity.OrderUserEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import com.pe.allpafood.api.transaction.order.entity.DeliveryPointEntity;
import com.pe.allpafood.api.transaction.order.entity.OrderEntity;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.order.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class OrderRepository implements IOrderRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Integer> getTotalOrdersByDeliveryUser(LocalDate deliveryDate) {

        String sql = """
        SELECT delivery_user_id, COUNT(*) AS total_orders
        FROM tbl_order
        WHERE delivery_date = :date
        GROUP BY delivery_user_id
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", java.sql.Date.valueOf(deliveryDate));

        return namedParameterJdbcTemplate.query(sql, params, rs -> {
            Map<String, Integer> result = new HashMap<>();
            while (rs.next()) {
                result.put(
                        rs.getString("delivery_user_id"),
                        rs.getInt("total_orders")
                );
            }
            return result;
        });
    }

    @Override
    public List<OrderUserEntity> findByDeliveryDateAndDeliveryUserId(LocalDate today, String deliveryUserId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("date", today);
        params.addValue("delivery_user_id",deliveryUserId);

        String sql = """
                SELECT
                     o.id AS order_id,
                     o.menu_type_items,
                     o.delivery_date,
                     o.status,
                     up.user_id AS clientUserId,
                     up.name,
                     up.lastname,
                     up.image_url AS userImage,
                     dp.id,
                     dp.address,
                     dp.description,
                        dp.district,
                     ST_AsText(dp.location) AS location
                 FROM tbl_order o
                 INNER JOIN tbl_delivery_point dp ON o.delivery_point_id = dp.id
                 INNER JOIN tbl_profile up ON dp.user_id = up.user_id
                 WHERE o.delivery_date = :date AND (:delivery_user_id IS NULL OR o.delivery_user_id = :delivery_user_id)
                ;""";

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            OrderEntity order = new OrderEntity();
            order.setId(rs.getLong("order_id"));
            String json = rs.getString("menu_type_items");
            order.setItems(JsonUtil.convertToObjectList(json, new TypeReference<>() {}));

            DeliveryPointEntity deliveryPoint = new DeliveryPointEntity();
            deliveryPoint.setId(rs.getLong("id"));
            deliveryPoint.setUserId(rs.getString("clientUserId"));
            deliveryPoint.setAddress(rs.getString("dp.address"));
            deliveryPoint.setDescription(rs.getString("dp.description"));
            deliveryPoint.setGeoLocation(new GeoPoint(rs.getString("location")));
            deliveryPoint.setDistrict(rs.getString("district"));
            order.setDeliveryUserId((String) params.getValue("delivery_user_id"));
            order.setDeliveryPoint(deliveryPoint);
            order.setDeliveryDate(rs.getDate("delivery_date").toLocalDate());
            order.setStatus(StatusDeliveryEnum.fromId(rs.getString("status")));

            ProfileEntity profile = new ProfileEntity();
            profile.setUserId(rs.getString("clientUserId"));
            profile.setName(rs.getString("up.name"));
            profile.setLastname(rs.getString("up.lastname"));
            profile.setImageUrl(rs.getString("userImage"));

            OrderUserEntity orderUserEntity = new OrderUserEntity();
            orderUserEntity.setOrderEntity(order);
            orderUserEntity.setUserProfile(profile);
            return orderUserEntity;
        });
    }

    @Override
    public List<OrderUserEntity> findByDeliveryDateAndStatus(LocalDate today,LocalDate endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("date", today);
        params.addValue("endDate", endDate);

        String sql = """
                SELECT
                     o.id AS order_id,
                     o.menu_type_items,
                     o.delivery_date,
                     o.status,
                     o.delivery_user_id,
                     up.user_id AS clientUserId,
                     up.name,
                     up.lastname,
                     up.image_url AS userImage,
                     dp.id,
                     dp.address,
                     dp.description,
                     ST_AsText(dp.location) AS location
                 FROM tbl_order o
                 INNER JOIN tbl_delivery_point dp ON o.delivery_point_id = dp.id
                 INNER JOIN tbl_profile up ON dp.user_id = up.user_id
                 WHERE (o.delivery_date BETWEEN :date AND :endDate)
                ;""";

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            OrderEntity order = new OrderEntity();
            order.setId(rs.getLong("order_id"));
            String json = rs.getString("menu_type_items");
            order.setItems(JsonUtil.convertToObjectList(json, new TypeReference<>() {}));

            DeliveryPointEntity deliveryPoint = new DeliveryPointEntity();
            deliveryPoint.setId(rs.getLong("id"));
            deliveryPoint.setUserId(rs.getString("clientUserId"));
            deliveryPoint.setAddress(rs.getString("dp.address"));
            deliveryPoint.setDescription(rs.getString("dp.description"));
            deliveryPoint.setGeoLocation(new GeoPoint(rs.getString("location")));

            order.setDeliveryUserId(rs.getString("o.delivery_user_id"));
            order.setDeliveryPoint(deliveryPoint);
            order.setDeliveryDate(rs.getDate("delivery_date").toLocalDate());
            order.setStatus(StatusDeliveryEnum.fromId(rs.getString("status")));

            ProfileEntity profile = new ProfileEntity();
            profile.setUserId(rs.getString("clientUserId"));
            profile.setName(rs.getString("up.name"));
            profile.setLastname(rs.getString("up.lastname"));
            profile.setImageUrl(rs.getString("userImage"));

            OrderUserEntity orderUserEntity = new OrderUserEntity();
            orderUserEntity.setOrderEntity(order);
            orderUserEntity.setUserProfile(profile);
            return orderUserEntity;
        });
    }

    @Override
    public void updateOrdersStatusAndMotorized(List<OrderEntity> orders, String statusBase, String userIdBase) {
        String sql = "UPDATE tbl_order SET status = :status, delivery_user_id= :motorized  " +
                "WHERE id = :orderId AND status = :statusBase AND (:userIdBase IS NULL OR delivery_user_id = :userIdBase)";
        SqlParameterSource[] batchParams = orders.stream()
                .map(update -> new MapSqlParameterSource()
                        .addValue("statusBase", statusBase)
                        .addValue("status", update.getStatus())
                        .addValue("motorized", update.getDeliveryUserId())
                        .addValue("userIdBase", userIdBase)
                        .addValue("status", update.getStatus())
                        .addValue("orderId", update.getId()))
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(sql, batchParams);
    }

    @Override
    public void updateOrdersStatus(OrderEntity order) {
        String sql = "UPDATE tbl_order SET status = :status WHERE id = :orderId AND delivery_user_id= :motorizedId;";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("status", order.getStatus());
        params.addValue("orderId", order.getId());
        params.addValue("motorizedId", order.getDeliveryUserId());
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public OrderEntity insertOrder(OrderEntity order) {
        String sql = """
            INSERT INTO tbl_order (user_id, menu_type_items, delivery_point_id, create_date, delivery_date, status, delivery_user_id)
            VALUES (:userId, :menuTypeItems, :deliveryPointId, :createDate, :deliveryDate, :status, :deliveryUserId)
        """;

        order.setMenuTypeItems(JsonUtil.convertToJsonString(order.getItems()));
        log.info("[insertOrder] Inserting order {}", order);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsInserted = namedParameterJdbcTemplate.update(
                sql,
                new BeanPropertySqlParameterSource(order),
                keyHolder,
                new String[]{"id"}
        );

        if (rowsInserted > 0) {
            Number key = keyHolder.getKey();
            if (key != null) {
                order.setId(key.longValue());
            }
            log.info("[insertOrder] Order inserted with ID {}", order.getId());
        } else {
            log.warn("[insertOrder] No order was inserted");
        }

        return order;
    }

    @Override
    public void updateOrder(OrderEntity order) {
        order.setMenuTypeItems(JsonUtil.convertToJsonString(order.getItems()));
        String sql = """
            UPDATE tbl_order
            SET menu_type_items = :menuTypeItems, delivery_point_id = IFNULL(:deliveryPointId, delivery_point_id)
            WHERE user_id = :userId and id = :id
        """;
        log.info("[updateOrder] Updating order {}", order);
        namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(order));
    }

    @Override
    public List<OrderEntity> findMenusByDateRangesAndUserId(String userId, LocalDate init, LocalDate end) {
        String sql = """
            SELECT
                o.id AS orderId,
                o.delivery_date,
                o.delivery_point_id,
                mt.id AS menuTypeId,
                m.id AS menuId,
                m.name AS menuName,
                mt.type AS menuType,
                m.properties
            FROM
                tbl_order o
            JOIN
                tbl_menu_type mt
            ON
                JSON_CONTAINS(o.menu_type_items, CAST(mt.id AS JSON), '$')
            JOIN
                tbl_user u
                ON u.id = o.user_id
            JOIN
                tbl_menu m
                ON m.id = mt.menu_id
            WHERE
                u.id = ?
            AND
                o.delivery_date BETWEEN ? AND ?  ORDER BY o.delivery_date;
        """;
        return jdbcTemplate.query(sql, rs ->{
            List<OrderEntity> orders = new ArrayList<>();
            Map<Long, OrderEntity> ordersMap = new HashMap<>();

            while (rs.next()) {
                long orderId = rs.getLong("orderId");
                LocalDate deliveryDate = rs.getDate("delivery_date").toLocalDate();
                Long deliveryPointId = rs.getLong("delivery_point_id");
                // Obtener o crear la orden
                OrderEntity order = ordersMap.computeIfAbsent(orderId, id -> {
                    OrderEntity newOrder = new OrderEntity();
                    newOrder.setId(orderId);
                    newOrder.setDeliveryDate(deliveryDate);
                    newOrder.setMenuTypes(new ArrayList<>());
                    newOrder.setDeliveryPointId(deliveryPointId);
                    orders.add(newOrder);
                    return newOrder;
                });

                // Crear el menú y agregarlo a la orden
                MenuTypeEntity menuTypeEntity = new MenuTypeEntity();
                MenuEntity menu = new MenuEntity();
                menu.setId(rs.getInt("menuId"));
                menu.setName(rs.getString("menuName"));
                menu.setProperties(JsonUtil.convertToObjectList(rs.getString("properties"), new TypeReference<List<DetailEntity<Float>>>() {}));
                menuTypeEntity.setType(rs.getString("menuType"));
                menuTypeEntity.setMenu(menu);
                menuTypeEntity.setMenuId(menu.getId());
                menuTypeEntity.setId(rs.getInt("menuTypeId"));
                order.getMenuTypes().add(menuTypeEntity);
            }

            return orders;
        }, userId, init, end);
    }

    @Override
    public List<OrderSummary> findByDeliveryDateOrderByMenuType(LocalDate deliveryDate) {
        String sql = """
                SELECT
                    o.id AS orderId,
                    mt.id AS menuTypeId,
                    mt.menu_id AS menuId,
                    m.name AS menuName,
                    mt.type AS menuType,
                    u.id AS userId,
                    u.corporate_user AS corporateUser
                FROM
                    tbl_order o
                JOIN
                    tbl_menu_type mt
                ON
                    JSON_CONTAINS(o.menu_type_items, CAST(mt.id AS JSON), '$')
                JOIN
                tbl_user u
                    ON u.id = o.user_id
                JOIN
                tbl_menu m
                    ON m.id = mt.menu_id
                WHERE
                    o.delivery_date = ?;""";
        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(OrderSummary.class),
                Date.valueOf(deliveryDate)
        );
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT count(*) FROM tbl_order WHERE id = ? AND status = 'P';";
        Integer count = jdbcTemplate.queryForObject(sql,Integer.class, id);
        return count != null && count>0;
    }

    @Override
    public OrderEntity findByDeliveryDateAndUserId(LocalDate deliveryDate, String userId) {
        try{
            String sql = "SELECT * FROM tbl_order WHERE user_id = ? AND delivery_date = ?;";
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(OrderEntity.class), userId, Date.valueOf(deliveryDate));
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public boolean existByDeliveryDateAndUserId(LocalDate deliveryDate, String userId) {
        String sql = "SELECT COUNT(*) FROM tbl_order WHERE user_id = ? AND delivery_date = ?;";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, Date.valueOf(deliveryDate));
        return count != null && count > 0;
    }

    @Override
    public OrderEntity findByIdAndUserId(String userId, Long id) {
        try {
            String sql = "SELECT id,delivery_point_id,delivery_date,menu_type_items FROM tbl_order WHERE id = ? AND user_id = ?;";
            return jdbcTemplate.queryForObject(sql,((rs, rowNum) -> {
                OrderEntity order = new OrderEntity();
                order.setId(rs.getLong("id"));

                DeliveryPointEntity deliveryPoint = new DeliveryPointEntity();
                deliveryPoint.setId(rs.getLong("delivery_point_id"));

                order.setDeliveryPoint(deliveryPoint);
                order.setDeliveryDate(rs.getDate("delivery_date").toLocalDate());

                String json = rs.getString("menu_type_items");
                List<Integer> menuItems= JsonUtil.convertToObjectList(json, new TypeReference<>() {});

                order.setItems(menuItems);
                return order;
            }), id, userId);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public List<String> findPhoneNumbersByOrderIds(List<Long> orderIds) {
        String sql = "SELECT u.phone_number " +
                "FROM tbl_order o " +
                "INNER JOIN tbl_user u ON o.user_id = u.id " +
                "WHERE o.id IN (:orderIds)";
        Map<String, Object> params = new HashMap<>();
        params.put("orderIds", orderIds);

        return namedParameterJdbcTemplate.queryForList(sql,params,String.class);
    }

    @Override
    public void updateByUserIdAndStatusAndMinDate(String userId, LocalDate deliveryDate, String status,Long deliveryPointId) {
        String sql = "UPDATE tbl_order SET delivery_point_id = ? WHERE delivery_date > ? AND user_id = ? AND status = ?;";
        jdbcTemplate.update(sql,deliveryPointId, Date.valueOf(deliveryDate),userId, status);
    }

    @Override
    public void deleteByOrderId(Long orderId) {
        String sql = "DELETE FROM tbl_order WHERE id = ?";
        jdbcTemplate.update(sql,orderId);
    }

}
