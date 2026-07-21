package com.pe.allpafood.api.transaction.billing.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.transaction.billing.entities.InvoiceAddressEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import com.pe.allpafood.api.transaction.billing.entities.InvoiceEntity;
import com.pe.allpafood.api.transaction.payment.entities.VoucherEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InvoiceRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public InvoiceEntity insertInvoice(InvoiceEntity invoice) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = """
            INSERT INTO tbl_invoice (
                description,
                emission_date,
                status,
                address_id,
                user_id,
                payment_method,
                details,
                tota_price,
                processor)
                VALUES (
                :description,
                :emissionDate,
                :status,
                :addressId,
                :userId,
                :paymentMethod,
                :details,
                :totalPrice,
                :processor);
        """;
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(invoice);
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        long id = keyHolder.getKey() != null ? keyHolder.getKey().intValue() : -1;
        invoice.setId(id);
        return invoice;
    }

    public void insertPaymentVoucher(VoucherEntity voucherEntity){

        String sql = "INSERT INTO tbl_pay_voucher (invoice_id, image_url, payment_method, phone_number, send_date) " +
                "VALUES (?, ?, ?, ?, ?);";

        jdbcTemplate.update(
                sql,
                voucherEntity.getInvoiceId(),
                voucherEntity.getImageUrl(),
                voucherEntity.getPaymentMethod(),
                voucherEntity.getPhoneNumber(),
                Timestamp.valueOf(voucherEntity.getSendDate())
        );
    }

    public void update(InvoiceEntity invoice){
        String sql = "UPDATE  tbl_invoice " +
                "SET metadata = :metadata , " +
                "payment_reference = :paymentReference WHERE id = :id;";

        namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(invoice));
    }

    public void updateStatusInvoice(String invoiceId, String status){
        String sql = "UPDATE  tbl_invoice SET  status = ? WHERE id = ?;";

        jdbcTemplate.update(sql,
                status,
                invoiceId);
    }

    public InvoiceAddressEntity findInvoiceAdressByUser(String userId){
        log.info("[findInvoiceAdressByUser] Starting {}",userId);
        try {
            String sql = "SELECT id, address, description FROM tbl_invoice_address WHERE user_id=? AND active = true;";
            return jdbcTemplate.queryForObject(sql,new BeanPropertyRowMapper<>(InvoiceAddressEntity.class),userId);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public List<DetailEntity<Float>> findDetailsByUserIdAndId(String invoiceId, String userId){
        String sql = "SELECT details FROM tbl_invoice WHERE id=? AND user_id=?;";
        String json = jdbcTemplate.queryForObject(sql,String.class,invoiceId,userId);
        return JsonUtil.convertToObjectList(json, new TypeReference<List<DetailEntity<Float>>>() {});
    }

    public boolean existByUserAndIdAndStatus(String userId, String invoiceId, String status){
        String sql = "SELECT COUNT(*) FROM tbl_invoice WHERE user_id=? AND id=? AND status = ?;";
        Integer count = jdbcTemplate.queryForObject(sql,Integer.class,userId,invoiceId,status);
        return count != null && count > 0;
    }

    public Integer insertInvoiceAddress(InvoiceAddressEntity invoiceAddressEntity) {
        String sql = "INSERT INTO tbl_invoice_address (user_id, description, address, active) " +
                "VALUES (:userId, :description, :address, :active);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(invoiceAddressEntity), keyHolder, new String[]{"id"});

        return keyHolder.getKey() != null ? keyHolder.getKey().intValue() : null;
    }

    public void updateActiveAddress(String userId, boolean active) {
        String sql = "UPDATE  tbl_invoice_address SET active = ? " +
                "WHERE user_id = ?;";

        jdbcTemplate.update(sql, active, userId);
    }


    public List<InvoiceEntity> findByUserId(String userId){
        String sql = "SELECT " +
                "i.id AS id," +
                "i.description AS description," +
                "i.emission_date AS emissionDate," +
                "i.status AS status," +
                "i.tota_price AS totalPrice," +
                "i.details AS details," +
                "a.address AS address," +
                "a.description AS addressDescription " +
                "FROM tbl_invoice i " +
                "INNER JOIN tbl_invoice_address a " +
                "ON i.address_id = a.id " +
                "WHERE i.user_id = ?;";
        return jdbcTemplate.query(sql,(rs, rowNum) -> {

            InvoiceEntity invoiceEntity = new InvoiceEntity();
            invoiceEntity.setId(rs.getLong("id"));
            invoiceEntity.setDescription(rs.getString("description"));
            invoiceEntity.setTotalPrice(rs.getFloat("totalPrice"));
            invoiceEntity.setStatus(rs.getString("status"));
            invoiceEntity.setEmissionDate(rs.getDate("emissionDate").toLocalDate());
            invoiceEntity.setDetailsEntity(JsonUtil.convertToObjectList(rs.getString("details"), new TypeReference<List<DetailEntity<Float>>>() {}));

            InvoiceAddressEntity invoiceAddress = new InvoiceAddressEntity();
            invoiceAddress.setAddress(rs.getString("address"));
            invoiceAddress.setDescription(rs.getString("addressDescription"));
            invoiceEntity.setInvoiceAddressEntity(invoiceAddress);
            return invoiceEntity;
            
        },userId);
    }
}
