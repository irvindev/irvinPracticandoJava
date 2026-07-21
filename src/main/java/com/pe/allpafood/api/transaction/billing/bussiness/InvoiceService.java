package com.pe.allpafood.api.transaction.billing.bussiness;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.allpafood.api.transaction.billing.entities.TxStatusEnum;
import com.pe.allpafood.api.transaction.billing.repository.InvoiceRepository;
import com.pe.allpafood.api.transaction.billing.dto.InvoiceDTO;
import com.pe.allpafood.api.transaction.catalog.bussiness.impl.MenuService;
import com.pe.allpafood.api.core.enums.StatusDeliveryEnum;
import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.transaction.notification.bussiness.impl.NotificationAsyncExecutor;
import com.pe.allpafood.api.transaction.notification.entity.WhatsappRequest;
import com.pe.allpafood.api.transaction.notification.entity.email.EmailAddress;
import com.pe.allpafood.api.transaction.notification.entity.email.EmailMessage;
import com.pe.allpafood.api.transaction.notification.utils.MessageTemplateUtils;
import com.pe.allpafood.api.transaction.payment.bussiness.impl.PaymentService;
import com.pe.allpafood.api.transaction.payment.dto.PaymentResult;
import com.pe.allpafood.api.transaction.payment.dto.generic.GenericPaymentDTO;
import com.pe.allpafood.api.transaction.payment.dto.mercadopago.*;
import com.pe.allpafood.api.transaction.plan.bussiness.IPlanService;
import com.pe.allpafood.api.transaction.plan.bussiness.impl.UserPlanService;
import com.pe.allpafood.api.transaction.plan.dto.UserSubscriptionDTO;
import com.pe.allpafood.api.transaction.plan.entities.UserPlanEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.BenefitsEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.ConsumeBenefits;
import com.pe.allpafood.api.transaction.plan.repository.impl.SubscriptionPlanRepository;
import com.pe.allpafood.api.transaction.user.dto.DeliveryDTO;
import com.pe.allpafood.api.transaction.plan.entities.benefits.DetailEntity;
import com.pe.allpafood.api.transaction.plan.entities.benefits.SubscriptionPlanEntity;
import com.pe.allpafood.api.transaction.billing.entities.InvoiceAddressEntity;
import com.pe.allpafood.api.transaction.billing.entities.InvoiceEntity;
import com.pe.allpafood.api.transaction.catalog.entity.MenuEntity;
import com.pe.allpafood.api.transaction.plan.bussiness.impl.SubscriptionService;
import com.pe.allpafood.api.transaction.setting.SettingRepository;
import com.pe.allpafood.api.transaction.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    @Value("${notification.service.whatsapp.template-subscription}")
    private String templateSubscription;

    private final ObjectMapper objectMapper;

    private final static String INVOICE_DESCRIPTION = "Pago de Plan vía web.";
    private final static String PENDING_STATUS = "P";
    private final static String COMPLETED_STATUS = "C";
    private final static List<String> PAYMENT_METHODS = Arrays.asList("card","yape");
    private final InvoiceRepository invoiceRepository;
    private final UserPlanService userPlanService;
    private final IPlanService planService;
    private final MenuService menuService;
    private final SettingRepository settingRepository;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final ProfileRepository profileRepository;
    private final NotificationAsyncExecutor notificationAsyncExecutor;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional
    public InvoiceDTO registerInvoice(String userId, String ip,UserSubscriptionDTO dto) throws JsonProcessingException {
        log.info("[registerInvoice] Starting {}", userId);

        if (!PAYMENT_METHODS.contains(dto.paymentMethodType())) throw new BusinessException("Metodo de pago no válido.");

        ConsumeBenefits currentConsumedBenefits = null;
        if (userPlanService.isUserPlanAvailable(userId)) {
            UserPlanEntity userPlan = userPlanService.getUserPlan(userId);

            if (userPlan.getCredits() != null) throw new BusinessException("Actualmente tienes creditos sin consumir.");

            if (userPlan.getConsumedBenefits() != null) {
                var consumeOrders = userPlan.getConsumedBenefits().getOrders();
                if (consumeOrders != null) {
                    var total = consumeOrders.get("total");
                    var consumed = consumeOrders.get("consumed");
                    if ((total - consumed) > 5) throw new BusinessException("Usted cuenta con un plan vigente.");
                    currentConsumedBenefits = userPlan.getConsumedBenefits();
                }
            }
        }
        BenefitsEntity benefitsEntity = subscriptionPlanRepository.findBenefitsByPlanId(dto.planId());

        var extraBenefits = benefitsEntity.getExtraBenefits();
        var principalBenefits = benefitsEntity.getPrincipalBenefits();
        Set<String> additionalTypes = dto.additional().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        boolean exists = Stream.concat(
                    extraBenefits.stream(),
                    principalBenefits.stream()
                )
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(additionalTypes::contains);

        if (exists) throw new BusinessException("Uno o más ya estan considerados en tus beneficios.");

        log.info("[registerInvoice] findInvoiceAdressByUser {}",userId);

        InvoiceAddressEntity invoiceAddress;
        invoiceAddress = invoiceRepository.findInvoiceAdressByUser(userId);

        if (invoiceAddress == null){
            if (dto.invoiceAddress()!=null){
                invoiceAddress = registerInvoiceAddress(userId,dto.invoiceAddress());
                log.info("[registerInvoice] Insert invoice address {}",invoiceAddress.getId());
            }else{
                log.error("[registerInvoice] Error to insert address invoice from {} because there isnt address.",userId);
                throw new BusinessException("No se pudo realizar el registro de pago, revise su dirección de facturación.");
            }
        }

        InvoiceEntity invoiceEntity= makeInvoiceEntity(userId,invoiceAddress.getId(),dto.paymentMethodType());
        log.debug("[registerInvoice] makeInvoiceEntity {}",invoiceEntity);

        List<DetailEntity<Float>> details = getDetails(dto);
        log.info("[registerInvoice] Details {}", details);
        invoiceEntity.setAddressId(invoiceAddress.getId());
        invoiceEntity.setTotalPrice(calculateTotalFromDetails(details));
        invoiceEntity.setDetails(JsonUtil.convertToJsonString(details));
        invoiceEntity.setDetailsEntity(details);
        invoiceEntity.setInvoiceAddressEntity(invoiceAddress);
        log.debug("[registerInvoice] setDetails {}",invoiceEntity);

        var user = profileRepository.findPrivacyDataByUserId(userId);

        List<ProductDTO> productDTOS = new ArrayList<>();
        Float planPrice = 0F;
        String planDescription = "";

        for (DetailEntity<Float> detail : details) {
            if (detail.getId() != null && detail.getId().equals(dto.planId().toString())) {
                planPrice += detail.getValue();

                if (detail.getValue()>0) planDescription = detail.getName();
            }
        }

        for (DetailEntity<Float> detail : details) {
            if (!(detail.getId() != null && detail.getId().equals(dto.planId().toString()))) {
                productDTOS.add(
                    new ProductDTO(
                        detail.getId(),
                        detail.getName(),
                        detail.getName(),
                        1,
                        detail.getValue()
                    )
                );
            }
        }

        if (planPrice > 0F) {
            productDTOS.add(
                new ProductDTO(
                    dto.planId().toString(),
                    planDescription,
                    planDescription,
                    1,
                    planPrice
                )
            );
        }

        log.info("[registerInvoice] Products {}", productDTOS);
        var processor = "mercadopago";

        invoiceEntity.setProcessor(processor);
        var invoiceResult = invoiceRepository.insertInvoice(invoiceEntity);
        if (invoiceResult.getId()<0) throw new BusinessException("Error al generar la factura.");

        GenericPaymentDTO payment = new GenericPaymentDTO(
                dto.paymentMethodType(),
                processor,
                invoiceResult.getId(),
                ip,
                dto.paymentToken(),
                new PayerDTO(
                        user.getName(),
                        user.getLastname(),
                        user.getEmail(),
                        new PayerIdentificationDTO(
                                "DNI",
                                user.getDocumentNumber()
                        )
                ),
                "Subscripcion :"+planDescription,
                BigDecimal.valueOf(invoiceEntity.getTotalPrice()),
                1,
                productDTOS
        );

        log.debug("[registerInvoice] GenericPaymentDTO - {}",payment);
        PaymentResult<PaymentResponseDTO> resultPayment = paymentService.paymentPlanSubscription(payment);
        if (!resultPayment.isSuccess()) throw new BusinessException("Error al procesar la factura :"+ resultPayment.getMessage());

        invoiceEntity.setPaymentReference(resultPayment.getProcessorRef());
        invoiceEntity.setStatus(resultPayment.getTxStatus());
        invoiceEntity.setMetadata(JsonUtil.convertToJsonString(resultPayment.getData()));

        if (resultPayment.getTxStatus().equals(TxStatusEnum.COMPLETED.getCode())){
            subscriptionService.subscribeUserToPlan(userId, benefitsEntity,dto.complementsId(), currentConsumedBenefits, dto.additional());
            EmailMessage email = new EmailMessage();
            email.setTo(List.of(new EmailAddress(user.getEmail(), user.getName())));
            email.setSubject("✅ Registro Satisfactorio");
            email.setTemplateName("welcome");

            notificationAsyncExecutor.sendNotification(email);
            notificationAsyncExecutor.sendNotification(
                    new WhatsappRequest(
                            MessageTemplateUtils.getDefaultTemplate(
                                    new ArrayList<>(),
                                    new ArrayList<>()
                            ),
                            this.templateSubscription,user.getPhoneNumber()
                    )
            );
        }

        return mapDTOFromEntity(invoiceResult);
    }

    @Transactional
    public void changeStatusInvoice(String status,String invoiceId){
        invoiceRepository.updateStatusInvoice(invoiceId,status);
    }

    public List<InvoiceDTO> getInvoiceList(String userId){
        log.info("[getInvoiceList] Starting with userId: {}", userId);
        List<InvoiceEntity> invoiceEntities = invoiceRepository.findByUserId(userId);

        List<InvoiceDTO> invoiceDTOS = new ArrayList<>();
        for (InvoiceEntity invoice:invoiceEntities){
            invoiceDTOS.add(mapDTOFromEntity(invoice));
        }
        return invoiceDTOS;
    }

    public InvoiceAddressEntity getInvoiceAddress(String userId){
        InvoiceAddressEntity address = invoiceRepository.findInvoiceAdressByUser(userId);
        if (address == null) throw new BusinessException("Dirección no encontrada");
        return address;
    }

    @Transactional
    public InvoiceAddressEntity changeInvoiceAddress(String userId,DeliveryDTO dto){
        invoiceRepository.updateActiveAddress(userId,false);
        return registerInvoiceAddress(userId,dto);
    }

    public List<DetailEntity<Float>> getInvoiceDetails(String userId, String invoiceId){
        return invoiceRepository.findDetailsByUserIdAndId(invoiceId,userId);
    }

    private Float calculateTotalFromDetails(List<DetailEntity<Float>> details){
        BigDecimal total = new BigDecimal("0");
        BigDecimal subtotal;
        for (DetailEntity<Float> detail :details){
            subtotal = BigDecimal.valueOf(detail.getValue());
            total = total.add(subtotal);
        }
        return total.floatValue();
    }

    private InvoiceAddressEntity registerInvoiceAddress(String userId,DeliveryDTO dto){
        log.info("[registerInvoiceAddress] Starting {}",userId);
        InvoiceAddressEntity invoiceAddress = new InvoiceAddressEntity();
        invoiceAddress.setUserId(userId);
        invoiceAddress.setAddress(dto.address());
        invoiceAddress.setDescription(dto.description());
        invoiceAddress.setActive(true);
        Integer addressId = invoiceRepository.insertInvoiceAddress(invoiceAddress);
        invoiceAddress.setId(addressId);
        log.info("[registerInvoiceAddress] Ending successful {}",userId);
        return invoiceAddress;
    }

    private List<DetailEntity<Float>> getDetails(UserSubscriptionDTO subscriptionDTO) throws JsonProcessingException {
        log.info("[getDetails] Starting");
        List<DetailEntity<Float>> details = new ArrayList<>();


        SubscriptionPlanEntity subscriptionPlanEntity = planService.getPlanPrices(subscriptionDTO.planId());
        log.debug("[getDetails] findPriceByPlanId {}",subscriptionPlanEntity);

        BigDecimal realPrice = BigDecimal.valueOf(subscriptionPlanEntity.getRealPrice());
        BigDecimal discount  = BigDecimal.valueOf(subscriptionPlanEntity.getDiscountAmount());
        BigDecimal price = realPrice.add(discount);

        details.add(new DetailEntity<>(String.valueOf(subscriptionDTO.planId()), "Plan - ".concat(subscriptionPlanEntity.getDescription()), price.floatValue()));
        details.add(new DetailEntity<>(String.valueOf(subscriptionDTO.planId()), "Descuento de plan - ".concat(subscriptionPlanEntity.getDescription()), -discount.floatValue()));

        //ADDITIONAL
        if (subscriptionDTO.additional() != null && !subscriptionDTO.additional().isEmpty()){
            String values = settingRepository.findByName("additional");
            List<Map<String, Object>> additionalSettings = objectMapper.readValue( values, new TypeReference<List<Map<String, Object>>>() {} );

            Map<String, String> typePriceMap = additionalSettings.stream()
                    .collect(Collectors.toMap(
                            item -> String.valueOf(item.get("type")),
                            item -> String.valueOf(item.get("monthlyPrice"))
                    ));
            for (var item : subscriptionDTO.additional()) {
                if (item == null) continue;
                if (typePriceMap.containsKey(item)){
                    details.add(new DetailEntity<>(
                            item,
                           item,
                            Float.parseFloat(typePriceMap.get(item))
                    ));
                }
            }
        }

        //DEPRECATED
        if (subscriptionDTO.complementsId() != null && !subscriptionDTO.complementsId().isEmpty()){
            List<MenuEntity> menus = menuService.getByIds(subscriptionDTO.complementsId());
            log.debug("[getDetails] findComplements {}",menus);

            if (menus == null || menus.size() != subscriptionDTO.complementsId().size()) throw new BusinessException("Complementos no encontrados.");

            for (MenuEntity menu : menus){
                if(subscriptionDTO.complementsId().contains(menu.getId())) details.add(new DetailEntity<>(menu.getId().toString(),menu.getName(), menu.getPrice()));
            }
        }

        List<DetailEntity<Float>> defaultDetails = settingRepository.findSettingDetailValueByName("invoice_details_default");
        log.debug("[getDetails] defaultDetails {}",defaultDetails);
        details.addAll(defaultDetails);
        return details;
    }

    private InvoiceDTO mapDTOFromEntity(InvoiceEntity entity){
        return new InvoiceDTO(
                entity.getId(),
                StatusDeliveryEnum.fromId(entity.getStatus()),
                entity.getEmissionDate(),
                entity.getTotalPrice(),
                entity.getDetailsEntity(),
                entity.getInvoiceAddressEntity()
        );
    }

    private InvoiceEntity makeInvoiceEntity(String userId,Integer addressId,String paymentMethod){
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.setDescription(INVOICE_DESCRIPTION);
        invoiceEntity.setEmissionDate(TimeUtil.getPeruDateTime().toLocalDate());
        invoiceEntity.setStatus(PENDING_STATUS);
        invoiceEntity.setUserId(userId);
        invoiceEntity.setPaymentMethod(paymentMethod);
        invoiceEntity.setAddressId(addressId);
        return invoiceEntity;
    }
}
