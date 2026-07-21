package com.pe.allpafood.api.transaction.user.repository;

import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.core.utils.dto.PageResult;
import com.pe.allpafood.api.core.utils.dto.PaginationUtil;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.user.entities.RoleEntity;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public boolean isVerifiedUser(String userId){
        String sql = "SELECT verified FROM tbl_user WHERE id = ?;";
        try {
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId));
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public String findProviderById(String userId){
        String sql = "SELECT provider FROM tbl_user WHERE id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql,String.class,userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public UserEntity findByEmailOrPhoneNumber(String username) {
        String sql = "SELECT * FROM tbl_user WHERE (email = ? OR phone_number = ?) and provider='user-pass';";
        try {
            return jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper<>(UserEntity.class),username, username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<String> findRolesByUserId(String userId){
        String sql = """
            SELECT
                r.role
            FROM tbl_role r
            INNER JOIN tbl_user_role ur
            ON r.id = ur.role_id
            WHERE ur.user_id = ?;
            """;
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    public List<UserEntity> findByRole(Integer role){
        String sql = """
            SELECT
                 u.id,
                 u.email,
                 u.phone_number,
                 u.document_number,
                p.name,
                p.lastname,
                p.district
             FROM tbl_user u
             INNER JOIN tbl_user_role ur
                 ON u.id = ur.user_id
             INNER JOIN  tbl_profile p ON p.user_id = u.id
             WHERE ur.role_id = ?
        """;
        return jdbcTemplate.query(sql,  (rs, rowNum) -> {
            UserEntity user = new UserEntity();
            user.setId(rs.getString("id"));
            user.setEmail(rs.getString("email"));
            user.setPhoneNumber(rs.getString("phone_number"));
            user.setDocumentNumber(rs.getString("document_number"));
            ProfileEntity profile = new ProfileEntity();
            profile.setName(rs.getString("name"));
            profile.setLastname(rs.getString("lastname"));
            profile.setDistrict(rs.getString("district"));
            user.setProfile(profile);
            return user;
        },role);
    }

    public void insertUserRole(String userId, Integer role){
        String sql = "INSERT tbl_user_role VALUES (?, ?);";
        jdbcTemplate.update(sql, userId,role);
    }

    public String findIdCorporateUser(String phone, String email, Integer corporationId) {
        String sql = "SELECT id FROM tbl_user WHERE (email=? OR phone_number = ?) AND corporate_user=true AND corporation_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, email, phone, corporationId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void insertByCsv(UserEntity user) {
        String sql = "INSERT INTO tbl_user (" +
                "id, " +
                "email," +
                "document_number, " +
                "phone_number, " +
                "registration_completed," +
                "profile_completed," +
                "verified, " +
                "provider," +
                "corporate_user," +
                "corporation_id," +
                "password" +
                ") " +
                "VALUES (?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                user.getId(),
                user.getEmail(),
                user.getDocumentNumber(),
                user.getPhoneNumber(),
                user.isRegistrationCompleted(),
                user.isProfileCompleted(),
                user.getVerified(),
                user.getProvider(),
                user.isCorporateUser(),
                user.getCorporationId(),
                user.getPassword());
    }


    public void insert(UserEntity user) {
        String sql = "INSERT INTO tbl_user (" +
                "id, " +
                "email," +
                "document_number, " +
                "phone_number, " +
                "registration_completed," +
                "profile_completed," +
                "verified, " +
                "provider," +
                "corporate_user," +
                "corporation_id," +
                "password" +
                ") " +
                "VALUES (?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                user.getId(),
                user.getEmail(),
                user.getDocumentNumber(),
                user.getPhoneNumber(),
                user.isRegistrationCompleted(),
                user.isProfileCompleted(),
                user.getVerified(),
                user.getProvider(),
                user.isCorporateUser(),
                user.getCorporationId(),
                user.getPassword());
    }

    public void insertByOauth(UserEntity user) {
        String sql = "INSERT INTO tbl_user (" +
                "id, " +
                "email, " +
                "registration_completed," +
                "profile_completed," +
                "verified, " +
                "provider," +
                "corporate_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getId(),
                user.getEmail(),
                user.isRegistrationCompleted(),
                user.isProfileCompleted(),
                user.getVerified(),
                user.getProvider(),
                user.isCorporateUser());
    }

    public void insertUserByCodeVerification(UserEntity user) {
        String sql = "INSERT INTO tbl_user (" +
                "id, " +
                "phone_number, " +
                "registration_completed," +
                "profile_completed," +
                "verified, " +
                "verification_code, " +
                "verification_code_exp, " +
                "provider," +
                "corporate_user," +
                "register_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                user.getId(),
                user.getPhoneNumber(),
                user.isRegistrationCompleted(),
                user.isProfileCompleted(),
                user.getVerified(),
                user.getVerificationCode(),
                Timestamp.valueOf(user.getVerificationCodeExp()),
                user.getProvider(),
                user.isCorporateUser(),
                TimeUtil.getPeruDateTime());
    }

    public void updateByPhoneVerification(UserEntity user) {
        String sql = "UPDATE tbl_user SET " +
                "email = ?, " +
                "password = ?," +
                "registration_completed = ?," +
                "document_number = ? " +
                "WHERE id = ?;";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getPassword(),
                user.isRegistrationCompleted(),
                user.getDocumentNumber(),
                user.getId());
    }

    public UserEntity findByPhoneNumber(String phoneNumber) {
        String sql = "SELECT id, phone_number,registration_completed, verified,verification_code, verification_code_exp FROM tbl_user WHERE phone_number = ? ;";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(UserEntity.class), phoneNumber);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public UserEntity findByEmail(String email) {
        String sql = "SELECT id, registration_completed, profile_completed FROM tbl_user WHERE email = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(UserEntity.class), email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    public void update(UserEntity userEntity) {

        String sql = """
        UPDATE tbl_user
        SET
            email = IFNULL(:email, email),
            phone_number = IFNULL(:phoneNumber, phone_number),
            status = IFNULL(:status, status),
            document_number = IFNULL(:documentNumber, document_number)
        WHERE id = :userId
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userEntity.getId())
                .addValue("email", userEntity.getEmail())
                .addValue("phoneNumber", userEntity.getPhoneNumber())
                .addValue("documentNumber", userEntity.getDocumentNumber())
                .addValue("status", userEntity.getStatus());

        namedParameterJdbcTemplate.update(sql, params);
    }
    public UserEntity findById(String id) {
        String sql = "SELECT id, verified,verification_code, verification_code_exp,email FROM tbl_user WHERE id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(UserEntity.class), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void updateUserVerificationCode(UserEntity user) {
        String sql = "UPDATE tbl_user SET " +
                " verified = ?, " +
                " verification_code = ?," +
                " verification_code_exp = ? " +
                "WHERE id = ?";

        jdbcTemplate.update(sql, user.getVerified(), user.getVerificationCode(),
                null, user.getId());
    }

    public void deleteByUserId(String userId) {
        String sql = "DELETE FROM tbl_user WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void deleteRoleByUserIdAndRoleId(String userId, Integer roleId) {
        String sql = "DELETE FROM tbl_user_role WHERE user_id = ? AND role_id = ?;";
        jdbcTemplate.update(sql, userId, roleId);
    }

    public void updateProfileCompleted(String userId,boolean profileCompleted){
        String sql = "UPDATE tbl_user SET profile_completed = ? WHERE id = ?;";
        jdbcTemplate.update(sql, profileCompleted,userId);
    }

    public void updateDocAndPhoneNumber(String userId, String docNumber, String phoneNumber){
        String sql = "UPDATE tbl_user SET document_number = ?, phone_number=? WHERE id = ?;";
        jdbcTemplate.update(sql, docNumber, phoneNumber,userId);
    }

    public void updateMotorizedUser(String userId, String email, String phoneNumber, String documentNumber, String password){
        String sql = "UPDATE tbl_user SET " +
                "email = IFNULL(:email, email), " +
                "phone_number = IFNULL(:phoneNumber, phone_number), " +
                "document_number = IFNULL(:documentNumber, document_number), " +
                "password = IFNULL(:password, password) " +
                "WHERE id = :userId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("phoneNumber", phoneNumber)
                .addValue("documentNumber", documentNumber)
                .addValue("password", password)
                .addValue("userId", userId);

        namedParameterJdbcTemplate.update(sql, params);
    }

    public String findPasswordById(String userId){
        String sql = "SELECT password FROM tbl_user WHERE id = ?;";
        return jdbcTemplate.queryForObject(sql, String.class, userId);
    }

    public void updatePasswordById(String id, String password){
        String sql = "UPDATE tbl_user SET password = ? WHERE id = ?;";
        jdbcTemplate.update(sql, password,id);
    }

    public PageResult<UserEntity> findUsers(String email, Integer roleId, int page, int size) {
        int[] validated = PaginationUtil.validatePageAndSize(page, size);
        page = validated[0];
        size = validated[1];

        int offset = (page-1) * size;

        StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(DISTINCT u.id)
            FROM tbl_user u
            INNER JOIN tbl_user_role ur ON ur.user_id = u.id
            INNER JOIN tbl_role r ON r.id = ur.role_id
            INNER JOIN tbl_profile p ON p.user_id = u.id
            WHERE 1 = 1
        """);

        StringBuilder dataSql = new StringBuilder("""
            SELECT
                u.id,
                u.password,
                u.registration_completed,
                u.profile_completed,
                u.phone_number,
                u.verified,
                u.verification_code,
                u.provider,
                u.email,
                u.document_number,
                u.verification_code_exp,
                u.corporate_user,
                u.corporation_id,
                u.register_date,
                u.status,
                ur.role_id,
                r.role,
                p.user_id AS profile_user_id,
                p.name,
                p.lastname,
                p.born_date,
                p.district,
                p.address,
                p.information,
                p.image_url,
                p.recommended_plan_id
            FROM tbl_user u
            INNER JOIN tbl_user_role ur ON ur.user_id = u.id
            INNER JOIN tbl_role r ON r.id = ur.role_id
            INNER JOIN tbl_profile p ON p.user_id = u.id
            WHERE 1 = 1
        """);

        List<Object> params = new ArrayList<>();

        if (email != null && !email.trim().isEmpty()) {
            countSql.append(" AND u.email LIKE ? ");
            dataSql.append(" AND u.email LIKE ? ");
            params.add("%" + email.trim() + "%");
        }

        if (roleId != null) {
            countSql.append(" AND ur.role_id = ? ");
            dataSql.append(" AND ur.role_id = ? ");
            params.add(roleId);
        }

        dataSql.append(" ORDER BY u.register_date DESC LIMIT ? OFFSET ? ");

        Long totalElements = jdbcTemplate.queryForObject(
                countSql.toString(),
                params.toArray(),
                Long.class
        );

        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(size);
        dataParams.add(offset);
        List<UserEntity> content = jdbcTemplate.query(
                dataSql.toString(),
                dataParams.toArray(),
                rs -> {
                    Map<String, UserEntity> userMap = new LinkedHashMap<>();

                    while (rs.next()) {
                        String userId = rs.getString("id");

                        UserEntity user = userMap.get(userId);

                        if (user == null) {
                            user = new UserEntity();
                            user.setId(userId);
                            user.setRegistrationCompleted(rs.getBoolean("registration_completed"));
                            user.setProfileCompleted(rs.getBoolean("profile_completed"));
                            user.setPhoneNumber(rs.getString("phone_number"));
                            user.setVerified(rs.getObject("verified", Boolean.class));
                            user.setVerificationCode(rs.getString("verification_code"));
                            user.setProvider(rs.getString("provider"));
                            user.setEmail(rs.getString("email"));
                            user.setDocumentNumber(rs.getString("document_number"));
                            user.setStatus(rs.getInt("status"));
                            if (rs.getTimestamp("verification_code_exp") != null) {
                                user.setVerificationCodeExp(
                                        rs.getTimestamp("verification_code_exp").toLocalDateTime()
                                );
                            }

                            user.setCorporateUser(rs.getBoolean("corporate_user"));
                            user.setCorporationId(rs.getObject("corporation_id", Integer.class));
                            user.setRoles(new HashSet<>());

                            String profileUserId = rs.getString("profile_user_id");
                            if (profileUserId != null) {
                                ProfileEntity profile = new ProfileEntity();
                                profile.setUserId(profileUserId);
                                profile.setName(rs.getString("name"));
                                profile.setLastname(rs.getString("lastname"));

                                if (rs.getDate("born_date") != null) {
                                    profile.setBornDate(rs.getDate("born_date").toLocalDate());
                                }

                                profile.setDistrict(rs.getString("district"));
                                profile.setAddress(rs.getString("address"));
                                profile.setInformation(rs.getString("information"));
                                profile.setImageUrl(rs.getString("image_url"));
                                profile.setRecommendedPlanId(
                                        rs.getObject("recommended_plan_id", Integer.class)
                                );

                                user.setProfile(profile);
                            }

                            userMap.put(userId, user);
                        }

                        Integer currentRoleId = rs.getObject("role_id", Integer.class);
                        if (currentRoleId != null) {
                            RoleEntity role = RoleEntity.builder()
                                    .id(currentRoleId)
                                    .role(rs.getString("role"))
                                    .build();

                            user.getRoles().add(role);
                        }
                    }

                    return new ArrayList<>(userMap.values());
                }
        );

        return new PageResult<>(
                content,
                page,
                size,
                totalElements != null ? totalElements : 0L
        );
    }

    public int updateUserStatus(String userId, int status) {
        String sql = """
            UPDATE tbl_user
            SET status = ?, register_date = register_date
            WHERE id = ?
        """;

        return jdbcTemplate.update(sql, status, userId);
    }
}