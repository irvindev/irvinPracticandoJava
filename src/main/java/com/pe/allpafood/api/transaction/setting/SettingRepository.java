package com.pe.allpafood.api.transaction.setting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class SettingRepository {

    private static final String QUERY_FIND_SETTING = "SELECT value FROM tbl_settings WHERE name = ?;";
    private final JdbcTemplate jdbcTemplate;

    public Map<String,List<String>> findSettingMenus(){
        String sql = "SELECT * FROM tbl_settings WHERE name = 'menu_type_variable' OR name = 'menu_type_default'";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        Map<String, List<String>> settingsMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String name = (String) row.get("name");
            List<String> value = JsonUtil.convertToObjectList((String) row.get("value"), new TypeReference<List<String>>() {});
            settingsMap.put(name, value);
        }

        return settingsMap;
    }


    public List<String> findSettingValueByName(String settingName){
        String resultSet = jdbcTemplate.queryForObject(QUERY_FIND_SETTING,String.class,settingName);
        return JsonUtil.convertToObjectList(resultSet, new TypeReference<List<String>>() {});
    }

    public String findByName(String settingName) {
        try {
            return jdbcTemplate.queryForObject(
                    QUERY_FIND_SETTING,
                    String.class,
                    settingName
            );
        }catch (DataAccessException e){
            return null;
        }

    }

    public List<DetailEntity<Float>> findSettingDetailValueByName(String settingName){
        String resultSet = jdbcTemplate.queryForObject(QUERY_FIND_SETTING,String.class,settingName);
        return JsonUtil.convertToObjectList(resultSet, new TypeReference<List<DetailEntity<Float>>>() {});
    }

    public Integer findRecommendedPlanId(String settingName, String objective){
        String sql = """
                SELECT
                    jt.planId
                FROM
                    tbl_settings,
                    JSON_TABLE(
                        value,
                        '$[*]'
                        COLUMNS(
                            planId INT PATH '$.planId',
                            objectiveId VARCHAR(50) PATH '$.objectiveId'
                        )
                    ) AS jt
                WHERE
                    name = ?
                    AND jt.objectiveId = ?;
                """;
        return jdbcTemplate.queryForObject(sql,Integer.class,settingName,objective);
    }

}
