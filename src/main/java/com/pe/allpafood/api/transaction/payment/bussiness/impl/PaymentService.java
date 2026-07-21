package com.pe.allpafood.api.transaction.payment.bussiness.impl;

import com.pe.allpafood.api.core.utils.file.FileUtil;
import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.payment.dto.PaymentResult;
import com.pe.allpafood.api.transaction.payment.dto.generic.GenericPaymentDTO;
import com.pe.allpafood.api.transaction.payment.dto.TransactionDTO;
import com.pe.allpafood.api.transaction.payment.dto.mercadopago.PaymentResponseDTO;
import com.pe.allpafood.api.transaction.payment.processors.IPaymentProcessor;
import com.pe.allpafood.api.transaction.payment.processors.ProcessorFactory;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import com.pe.allpafood.api.transaction.payment.entities.VoucherEntity;
import com.pe.allpafood.api.transaction.billing.repository.InvoiceRepository;
import com.pe.allpafood.api.transaction.plan.bussiness.impl.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final static String FILE_DIRECTORY = "C:\\Users\\U19201672\\Documents\\proyects\\allpaFoodApi\\src\\main\\resources\\images";
    private final static String ENDPOINT_BASE_IMG = "https://allapaFood/";
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionService subscriptionService;
    private final ProcessorFactory processorFactory;

    @Transactional
    public void createPaymentVocuher(MultipartFile file, String userId,String invoiceId, String paymentMethod, String phoneNumber) {
        if (invoiceRepository.existByUserAndIdAndStatus(userId,invoiceId,"P")) throw new BusinessException("La siguiente factura no pertenece a este usuario.");

        try {
            VoucherEntity voucherEntity = new VoucherEntity();
            voucherEntity.setInvoiceId(invoiceId);
            voucherEntity.setPaymentMethod(paymentMethod);
            voucherEntity.setSendDate(TimeUtil.getPeruDateTime());
            voucherEntity.setPhoneNumber(phoneNumber);
            voucherEntity.setImageUrl(ENDPOINT_BASE_IMG.concat(invoiceId));

            invoiceRepository.insertPaymentVoucher(voucherEntity);
            FileUtil.saveFile(file,invoiceId,FILE_DIRECTORY);
        }catch (Exception e){
            throw new BusinessException("No se pudo guardar el comprobante de pago.");
        }
    }

    @Transactional
    public void createTransactionPayment(String userId, TransactionDTO transactionDTO) throws BusinessException {
        if (invoiceRepository.existByUserAndIdAndStatus(userId,transactionDTO.invoiceId(),"P")) throw new BusinessException("La siguiente factura no pertenece a este usuario.");

        try{
            chargePayment(transactionDTO);
            invoiceRepository.updateStatusInvoice(transactionDTO.invoiceId(),"C");
        }catch (Exception e){
            throw new BusinessException(e.getMessage());
        }

        List<DetailEntity<Float>> details =invoiceRepository.findDetailsByUserIdAndId(transactionDTO.invoiceId(),userId);
        if (this.subscribeUser(userId,details)) throw new BusinessException("No se encuentra el Plan en el detalle de la factura.");
    }

    public PaymentResult<PaymentResponseDTO> paymentPlanSubscription(GenericPaymentDTO request){
        IPaymentProcessor processor = processorFactory.getProcessor(request.processor());
        return processor.execute(request);
    }

    private void chargePayment(TransactionDTO transactionDTO) throws Exception{
    }

    private boolean subscribeUser(String userId, List<DetailEntity<Float>> details) {
        Integer planId = null;
        List<String> extraBenefits = new ArrayList<>();

        for (DetailEntity<Float> detail: details){
            if(detail.getName().equals("Plan")){
                planId = Integer.parseInt(detail.getId());
            }else{
                extraBenefits.add(detail.getName());
            }
        }

        if (planId==null) return false;

        subscriptionService.subscribeUserToPlan(userId,null,new ArrayList<>(), null,null);
        return true;
    }

}
