package com.pe.allpafood.api.transaction.order.repository.impl;

import com.pe.allpafood.api.transaction.order.entity.export.OrderExportRawRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class OrderExportRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<OrderExportRawRow> findOrdersByDate(LocalDate date) {
        String sql = """
            SELECT
                o.id AS order_id,
                o.menu_type_items,
                o.status,
                up.name AS client_name,
                up.lastname AS client_lastname,
                mp.name AS motorized_name,
                mp.lastname AS motorized_lastname,
                dp.district AS district,
                up.information AS client_information
            FROM tbl_order o
            INNER JOIN tbl_profile up ON up.user_id = o.user_id
            LEFT JOIN tbl_profile mp ON mp.user_id = o.delivery_user_id
            LEFT JOIN tbl_delivery_point dp ON dp.id = o.delivery_point_id
            WHERE o.delivery_date = :date
            ORDER BY o.id;
        """;

        MapSqlParameterSource params = new MapSqlParameterSource("date", date);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            OrderExportRawRow row = new OrderExportRawRow();
            row.setOrderId(rs.getLong("order_id"));
            row.setMenuTypeItemsJson(rs.getString("menu_type_items"));
            row.setStatus(rs.getString("status"));
            row.setClientName(rs.getString("client_name"));
            row.setClientLastname(rs.getString("client_lastname"));
            row.setMotorizedName(rs.getString("motorized_name"));
            row.setMotorizedLastname(rs.getString("motorized_lastname"));
            row.setDistrict(rs.getString("district"));
            row.setClientInformationJson(rs.getString("client_information"));
            return row;
        });
    }

    /** id (tbl_menu_type.id) -> type (tbl_menu_type.type). Un único roundtrip reutilizado para todas las órdenes del día. */
    public Map<Integer, String> findMenuTypesById() {
        String sql = "SELECT id, type FROM tbl_menu_type";
        Map<Integer, String> result = new HashMap<>();
        namedParameterJdbcTemplate.query(sql, rs -> {
            result.put(rs.getInt("id"), rs.getString("type"));
        });
        return result;
    }
}