package com.pe.allpafood.api.transaction.payment.repository;

import com.pe.allpafood.api.transaction.payment.entities.PaySimpleEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PaymentRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public Long insertSimplePayment(PaySimpleEntity payment){
        String sql = "INSERT tbl_simple_payment " +
                "(phone_number, name, lastname, email, planId, amount, creation_date, status, address, district) " +
                "VALUE " +
                "(:phoneNumber,:name, :lastname,:email,:planId,:amount,:creationDate,:status, :address,:district)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(payment), keyHolder, new String[]{"id"});
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void updateStatusPayment(String status, String orderStatus, String message, String transactionId, LocalDateTime date, long orderId){
        String sql = "UPDATE tbl_simple_payment SET status = ?, order_status = ?, message_operation = ?, transaction_id = ?, payment_date = ? WHERE id = ? ;";
        jdbcTemplate.update(sql,status,orderStatus,message,transactionId, Timestamp.valueOf(date),orderId);
    }
}
