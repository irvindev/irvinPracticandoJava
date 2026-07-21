package com.pe.allpafood.api.transaction.payment.bussiness.impl;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.payment.dto.CouponInDTO;
import com.pe.allpafood.api.transaction.payment.dto.DiscountDTO;
import com.pe.allpafood.api.transaction.payment.entities.CouponEntity;
import com.pe.allpafood.api.transaction.payment.repository.CouponsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculateDiscount {

    private final CouponsRepository couponsRepository;

    public DiscountDTO calculateDiscount(CouponInDTO coupon){
        CouponEntity entity = couponsRepository.findByCouponCode(coupon.code());

        if (entity == null) throw new BusinessException("El código del cupón no existe.");
        return calculate(entity.getType(),entity.getValue(),coupon.amount());
    }

    private DiscountDTO calculate(String type, Integer value,Float amount){
        BigDecimal initialAmount = new BigDecimal(amount);
        BigDecimal discountedAmount = switch (type) {
            case "percentage" -> initialAmount.multiply(new BigDecimal(value));
            case "amount" -> new BigDecimal(value);
            default -> throw new BusinessException("No se puede aplicar el descuento con este cupón.");
        };

        if (initialAmount.compareTo(discountedAmount)<0)  throw new BusinessException("No se puede aplicar el descuento con este cupón.");

        BigDecimal finalAmount = initialAmount.subtract(discountedAmount);

        return new DiscountDTO(amount, finalAmount.floatValue(), discountedAmount.floatValue());
    }
}
