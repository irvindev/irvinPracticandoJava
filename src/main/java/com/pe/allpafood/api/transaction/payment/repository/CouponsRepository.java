package com.pe.allpafood.api.transaction.payment.repository;

import com.pe.allpafood.api.transaction.payment.entities.CouponEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponsRepository {

    private final JdbcTemplate jdbcTemplate;

    public CouponEntity findByCouponCode(String couponCode) {
        try{
            String sql = "select * from tbl_coupon where id = ?";
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(CouponEntity.class),couponCode);
        }catch (Exception e){
            return null;
        }
    }

}
