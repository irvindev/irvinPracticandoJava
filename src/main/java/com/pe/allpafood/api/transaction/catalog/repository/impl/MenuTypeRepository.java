package com.pe.allpafood.api.transaction.catalog.repository.impl;

import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MenuTypeRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void updateStatusByMenuId(Integer menuId, Integer status) {
        String query = """
                UPDATE tbl_menu_type SET
                status = :status
                WHERE menu_id = :menuId
                """;

        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource("menuId", menuId)
                .addValue("status", status);

        namedParameterJdbcTemplate.update(query, mapSqlParameterSource);
    }

    public void saveAll(List<MenuTypeEntity> entities) {

        String query = """
        INSERT INTO tbl_menu_type (
            menu_id,
            type,
            status
        )
        VALUES (
            :menuId,
            :type,
            :status
        )
        ON DUPLICATE KEY UPDATE
            status = VALUES(status)
        """;

        SqlParameterSource[] batch =
                SqlParameterSourceUtils.createBatch(entities);

        namedParameterJdbcTemplate.batchUpdate(query, batch);
    }


    public List<MenuTypeEntity> findByIds(List<Integer> ids) {
        String sql = """
                SELECT id, type FROM tbl_menu_type WHERE id IN (:ids) and status = 1
        """;
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource("ids", ids);
        mapSqlParameterSource.addValue("ids", ids);
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new BeanPropertyRowMapper<>(MenuTypeEntity.class));
    }
}
