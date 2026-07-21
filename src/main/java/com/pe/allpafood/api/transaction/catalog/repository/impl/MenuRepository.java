package com.pe.allpafood.api.transaction.catalog.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.transaction.catalog.entity.MenuCalendar;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeEntity;
import com.pe.allpafood.api.transaction.catalog.entity.MenuTypeGroup;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import com.pe.allpafood.api.transaction.catalog.repository.IMenuRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MenuRepository implements IMenuRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MenuCalendar> findMenuBetweenCalendarDate(String userId, Integer benefitId, LocalDate startDate, LocalDate endDate) {
        String sql = """
       SELECT
            mc.calendar_date,
            mt.id AS menu_type_id,
            m.id AS menu_id,
            m.name AS menu_name,
            m.description AS menu_description,
            m.properties AS properties,
            m.image_url AS image,
            mt.type AS type
        FROM tbl_menu_calendar mc
        JOIN JSON_TABLE(
            mc.menu_list, '$[*]' COLUMNS(menu_type_id INT PATH '$')
        ) jt
        JOIN tbl_menu_type mt ON (mt.id = jt.menu_type_id AND mt.status = 1)
        JOIN (
            SELECT menu_type
            FROM tbl_benefits,
                 JSON_TABLE(tbl_benefits.principal_benefits, '$[*]' COLUMNS(menu_type VARCHAR(255) PATH '$')) jt
            WHERE tbl_benefits.id = :benefitId
            UNION ALL
            SELECT menu_type
            FROM tbl_benefits,
                 JSON_TABLE(tbl_benefits.extra_benefits, '$[*]' COLUMNS(menu_type VARCHAR(255) PATH '$')) jt
            WHERE tbl_benefits.id = :benefitId
        ) s ON s.menu_type = mt.type
        LEFT JOIN tbl_order o
               ON o.user_id = :userId
                   AND o.delivery_date = mc.calendar_date
       LEFT JOIN tbl_menu m
               ON m.id = mt.menu_id
        WHERE mc.calendar_date BETWEEN :startDate AND :endDate
        AND o.id IS NULL ORDER BY calendar_date ASC;
       """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);
        params.addValue("benefitId", benefitId);
        params.addValue("userId",userId);


        return mapMenuCalendars(sql, params);
    }


    @Override
    public List<MenuCalendar> findMenuBetweenCalendarDate(LocalDate startDate, LocalDate endDate) {
        String sql = """
           SELECT
                mc.calendar_date,
                m.id AS menu_id,
                mt.id AS menu_type_id,
                m.name AS menu_name,
                m.description AS menu_description,
                m.properties AS properties,
                m.image_url AS image,
                mt.type AS type
            FROM tbl_menu_calendar mc
            JOIN JSON_TABLE(
                mc.menu_list, '$[*]' COLUMNS(menu_type_id INT PATH '$')
            ) jt
            JOIN tbl_menu_type mt ON mt.id = jt.menu_type_id
            INNER JOIN tbl_menu m ON m.id = mt.menu_id
            WHERE mt.status = 1 AND mc.calendar_date BETWEEN :startDate AND :endDate
            ORDER BY calendar_date;
       """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);
        return mapMenuCalendars(sql, params);
    }

    @Override
    public Map<String,Integer> findRandomMenusIdByDate(LocalDate date){
        String sql = """
                SELECT\s
                      mt.id AS menu_type_id,
                      mt.type AS type
                  FROM tbl_menu_type mt
                  JOIN tbl_menu_calendar mc ON JSON_CONTAINS(mc.menu_list, CAST(mt.id AS JSON), '$')
                  WHERE mc.calendar_date = ? AND mt.status = 1
                  AND mt.id = (
                      SELECT MIN(m2.id)\s
                      FROM tbl_menu_type m2\s
                      WHERE m2.type = mt.type and m2.status = 1
                  );
                """;

        return jdbcTemplate.query(sql, rs ->{
            Map<String, Integer> result = new HashMap<>();

            while (rs.next()) {
                result.put(rs.getString("type"), rs.getInt("menu_type_id"));
            }
            return result;
        }, date);
    }

    @Override
    public MenuEntity findById(Long id) {
        String sql = "SELECT * FROM tbl_menu WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToPlato, id);
    }

    @Override
    public List<MenuTypeEntity> findByDate(LocalDate date) {

        String sql = """
                SELECT
                    m.id,
                    m.type
                FROM tbl_menu_type m
                INNER JOIN tbl_menu_calendar mc
                    ON JSON_CONTAINS(mc.menu_list, CAST(m.id AS JSON))
                WHERE m.status = 1 AND
                 mc.calendar_date = :date;
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("date", date);

        return namedParameterJdbcTemplate.query(sql,params,new BeanPropertyRowMapper<>(MenuTypeEntity.class));
    }


    @Override
    public List<Integer> findIdsByTypes(List<Integer> ids, List<String> types) {
        if (ids.isEmpty() || types.isEmpty()) return Collections.emptyList();


        String sql = "SELECT id FROM tbl_menu_type WHERE id IN (:ids) AND type IN (:types) AND status = 1";

        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        params.put("types", types);

        return namedParameterJdbcTemplate.queryForList(sql,params,Integer.class);
    }

    @Override
    public List<MenuTypeEntity> findByTypesAndDate(LocalDate today, List<String> types) {
        if (types == null || types.isEmpty()) return Collections.emptyList();


        String sql = "SELECT " +
                "   tm.id," +
                "   tm.type " +
                "FROM " +
                "    tbl_menu_calendar AS cm " +
                "JOIN " +
                "    tbl_menu_type AS tm " +
                "ON " +
                "    JSON_CONTAINS(cm.menu_list, CAST(tm.id AS JSON), '$') " +
                "WHERE status = 1 " +
                "    AND cm.calendar_date = :today " +
                "    AND tm.type IN (:types);";

        Map<String, Object> params = new HashMap<>();
        params.put("today", today);
        params.put("types", types);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            MenuTypeEntity menu = new MenuTypeEntity();
            menu.setId(rs.getInt("id"));
            menu.setType(rs.getString("type"));
            return menu;
        });
    }

    @Override
    public List<MenuEntity> findAll() {
        String sql = """
            SELECT
                  m.*,
                  GROUP_CONCAT(mt.id ORDER BY mt.type SEPARATOR ',') AS menuTypeEntityIds,
                  GROUP_CONCAT(mt.type ORDER BY mt.type SEPARATOR ',') AS menuTypeIds
              FROM tbl_menu AS m
              LEFT JOIN tbl_menu_type AS mt
                  ON (mt.menu_id = m.id AND status = 1)
              GROUP BY m.id;
            ;
        """;
        return jdbcTemplate.query(sql, this::mapRowToPlato);
    }

    @Override
    public void updateMenu(MenuEntity menu) {
        String query = "UPDATE tbl_menu SET " +
                "name = :name, " +
                "description = :description, " +
                "properties = :propertiesJson, " +
                "price = :price, " +
                "previous_price = :previousPrice, " +
                "image_url = IFNULL(:imageUrl, image_url) " +
                "WHERE id = :id";  // Asegúrate de que MenuEntity tenga el campo `id`

        SqlParameterSource params = new BeanPropertySqlParameterSource(menu);

        namedParameterJdbcTemplate.update(query, params);
    }

    @Override
    public int insertMenu(MenuEntity menu){
        String query = "INSERT INTO tbl_menu (name, description, properties, price, previous_price, image_url) " +
                "VALUES (:name, :description, :propertiesJson, :price, :previousPrice, :imageUrl)";
        // Usa un SqlParameterSource para mapear los campos automáticamente
        SqlParameterSource params = new BeanPropertySqlParameterSource(menu);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(query, params, keyHolder, new String[]{"id"});

        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    private MenuEntity mapRowToPlato(ResultSet rs, int rowNum) throws SQLException {
        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setId(rs.getInt("id"));
        menuEntity.setName(rs.getString("name"));
        menuEntity.setDescription(rs.getString("description"));
        menuEntity.setImageUrl(rs.getString("image_url"));
        menuEntity.setPrice(rs.getFloat("price"));
        menuEntity.setPreviousPrice(rs.getFloat("previous_price"));
        var typesString = rs.getString("menuTypeIds");
        if(typesString != null && !typesString.isEmpty()) menuEntity.setTypes(List.of(typesString.split(",")));
        var menuTypeIdsString = rs.getString("menuTypeEntityIds");
        if (menuTypeIdsString != null && !menuTypeIdsString.isEmpty() && typesString != null && !typesString.isEmpty()) {
            String[] menuTypeIds = menuTypeIdsString.split(",");
            String[] menuTypes = typesString.split(",");

            List<MenuTypeEntity> menuTypeEntities = new ArrayList<>();
            for (int i = 0; i < Math.min(menuTypeIds.length, menuTypes.length); i++) {
                MenuTypeEntity menuTypeEntity = new MenuTypeEntity();
                menuTypeEntity.setId(Integer.parseInt(menuTypeIds[i]));
                menuTypeEntity.setMenuId(menuEntity.getId());
                menuTypeEntity.setType(menuTypes[i]);
                menuTypeEntity.setStatus(1);
                menuTypeEntities.add(menuTypeEntity);
            }
            menuEntity.setMenuTypes(menuTypeEntities);
        }

        String json = rs.getString("properties");
        List<DetailEntity<Float>> properties = JsonUtil.convertToObjectList(json, new TypeReference<List<DetailEntity<Float>>>() {});
        menuEntity.setProperties(properties);

        return menuEntity;
    }

    @Override
    public void deleteMenu(int id) {
        String query = "DELETE FROM tbl_menu WHERE id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public void scheduleMenuList(List<Integer> menus, LocalDate date) {
        String query = "INSERT INTO tbl_menu_calendar (calendar_date, menu_list) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE menu_list = VALUES(menu_list)";

        String ids = JsonUtil.convertToJsonString(menus);
        jdbcTemplate.update(query, Date.valueOf(date), ids);
    }

    @Override
    public List<MenuEntity> findByTypes(List<String> types) {
        String sql = """
                SELECT DISTINCT m.*
                FROM tbl_menu m
                INNER JOIN tbl_menu_type mt ON mt.menu_id = m.id
                WHERE mt.type IN (:types)
            """;
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("types", types);
        return namedParameterJdbcTemplate.query(sql,mapSqlParameterSource, this::mapRowToPlato);
    }

    @Override
    public List<MenuEntity> findByIds(List<Integer> ids) {
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id, name ,price, type FROM tbl_menu WHERE id IN (" + placeholders + ")";
        Object[] params = ids.toArray();
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MenuEntity.class), params);
    }


    private List<MenuCalendar> mapMenuCalendars(String sql, MapSqlParameterSource params) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(sql, params);
        log.debug("[findMenuBetweenCalendarDate] result {}",result );
        Map<LocalDate, Map<String, List<MenuTypeEntity>>> groupedData = result.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Date) row.get("calendar_date")).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                row -> (String) row.get("type"),
                                Collectors.mapping(this::mapMenuTypeEntity, Collectors.toList())
                        )
                ));

        return groupedData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Asegura orden ascendente por fecha
                .map(entry -> {
                    MenuCalendar menuCalendar = new MenuCalendar();
                    menuCalendar.setLocalDate(entry.getKey());

                    List<MenuTypeGroup> menuTypes = entry.getValue().entrySet().stream()
                            .map(typeEntry -> {
                                MenuTypeGroup menuTypeGroup = new MenuTypeGroup();
                                menuTypeGroup.setType(typeEntry.getKey());
                                menuTypeGroup.setMenuTypes(typeEntry.getValue());
                                return menuTypeGroup;
                            })
                            .toList();

                    menuCalendar.setMenuTypeGroups(menuTypes);
                    return menuCalendar;
                })
                .toList();
    }

    private MenuTypeEntity mapMenuTypeEntity(Map<String,Object> row){
        MenuTypeEntity menuTypeEntity = new MenuTypeEntity();
        MenuEntity menu = new MenuEntity();
        menu.setId(Integer.parseInt(row.get("menu_id").toString()));
        menu.setName((String) row.get("menu_name"));
        menu.setDescription((String) row.get("menu_description"));

        String json = (String) row.get("properties");
        menu.setProperties(JsonUtil.convertToObjectList(json, new TypeReference<List<DetailEntity<Float>>>() {}));
        menu.setImageUrl((String) row.get("image"));
        menu.setPrice((Float) row.get("price"));
        menuTypeEntity.setId(Integer.parseInt(row.get("menu_type_id").toString()));
        menuTypeEntity.setMenu(menu);
        menuTypeEntity.setType((String)row.get("type"));
        menuTypeEntity.setMenuId(Integer.parseInt(row.get("menu_id").toString()));
        return menuTypeEntity;
    }
}
