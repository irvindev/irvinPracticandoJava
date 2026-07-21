package com.pe.allpafood.api.transaction.order.bussiness;

import com.pe.allpafood.api.transaction.auth.dto.FormUserDTO;
import com.pe.allpafood.api.transaction.auth.dto.UpdateMotorizedDTO;
import com.pe.allpafood.api.core.enums.StatusDeliveryEnum;
import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.converter.TimeUtil;
import com.pe.allpafood.api.gateway.order_admin.dto.AssignDeliveryPoint;
import com.pe.allpafood.api.transaction.order.entity.OrderUserEntity;
import com.pe.allpafood.api.transaction.order.repository.impl.DeliveryRepository;
import com.pe.allpafood.api.transaction.order.repository.impl.OrderRepository;
import com.pe.allpafood.api.transaction.user.dto.DeliveryDTO;
import com.pe.allpafood.api.transaction.order.entity.DeliveryPointEntity;
import com.pe.allpafood.api.transaction.order.entity.OrderEntity;
import com.pe.allpafood.api.transaction.notification.bussiness.impl.WhatsappNotification;
import com.pe.allpafood.api.transaction.notification.entity.WhatsappRequest;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import com.pe.allpafood.api.transaction.user.repository.ProfileRepository;
import com.pe.allpafood.api.transaction.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final WhatsappNotification whatsappNotification;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;

    @Value("${notification.service.whatsapp.template-order}")
    private String notifyOrder;

    public List<DeliveryDTO> getMyDeliveryPoints(String userId){
        List<DeliveryPointEntity> entities = deliveryRepository.findByUserId(userId);
        List<DeliveryDTO> response = new ArrayList<>();
        for (DeliveryPointEntity entity:entities){
            response.add(
                new DeliveryDTO(
                    entity.getId(),
                    entity.isAssigned(),
                    entity.getAddress(),
                    entity.getDescription(),
                    entity.getGeoLocation(),
                        entity.getDistrict()
                )
            );
        }

        return response;
    }

    public List<UserEntity> getDeliveredUsers(){
        return userRepository.findByRole(2);
    }

    @Transactional
    public void createNewUserDelivery(FormUserDTO user){
        log.info("Create new user delivery {}",user);
        try{
            UserEntity userEntity = new UserEntity();
            userEntity.setId(user.email().split("@")[0]);
            userEntity.setEmail(user.email());
            userEntity.setPhoneNumber(user.phoneNumber());
            userEntity.setPassword(passwordEncoder.encode(user.password()));
            userEntity.setProvider("user-pass");
            userEntity.setDocumentNumber(user.documentNumber());
            userEntity.setVerified(true);


            userRepository.insertByCsv(userEntity);
            userRepository.insertUserRole(userEntity.getId(),2);

            ProfileEntity profileEntity = new ProfileEntity();
            profileEntity.setUserId(userEntity.getId());
            profileEntity.setName(user.name());
            profileEntity.setLastname(user.lastname());
            profileEntity.setDistrict(String.join(",", user.districts()));
            profileRepository.saveProfile(profileEntity);
        }catch (DuplicateKeyException e){
            throw new BusinessException("El usuario ya existe");
        }
    }

    @Transactional
    public void updateUserDelivery(String userId, UpdateMotorizedDTO form){
        log.info("Update user delivery {} - {}", userId, form);
        try{
            String encodedPassword = form.password() != null ? passwordEncoder.encode(form.password()) : null;
            userRepository.updateMotorizedUser(userId, form.email(), form.phoneNumber(), form.documentNumber(), encodedPassword);

            String district = (form.districts() != null && !form.districts().isEmpty())
                    ? String.join(",", form.districts())
                    : null;
            profileRepository.updateMotorizedProfile(userId, form.name(), form.lastname(), district);
        }catch (DuplicateKeyException e){
            throw new BusinessException("El email, teléfono o documento ya existe");
        }
    }

    @Transactional
    public void deleteMotorized(String userId){
        userRepository.deleteRoleByUserIdAndRoleId(userId,2);
    }

    @Transactional
    public void completeOrders(String userId, List<Long> orderIds){
        List<OrderEntity> orderEntities = orderIds.stream().map(id->{
                    OrderEntity order = new OrderEntity();
                    order.setId(id);
                    order.setStatus(StatusDeliveryEnum.COMPLETED.getId());
                    order.setDeliveryUserId(userId);
                    return order;
                }
        ).toList();

        this.orderRepository.updateOrdersStatusAndMotorized(orderEntities,StatusDeliveryEnum.IN_PROGRESS.getId(), userId);
        this.sendClientNotifications(orderIds);
    }

    public List<OrderUserEntity> getAllMyOrdersToday(String userId, LocalDate date){
        if (date == null) date = TimeUtil.getPeruDate();
        return orderRepository.findByDeliveryDateAndDeliveryUserId(date,userId);
    }

    public List<OrderUserEntity> getAllOrders(LocalDate startDate, LocalDate endDate){
        return orderRepository.findByDeliveryDateAndStatus(startDate,endDate);
    }

    @Transactional
    public DeliveryDTO addDeliveryPoint(String userId, DeliveryDTO deliveryDTO){
        DeliveryPointEntity  deliveryPoint = new DeliveryPointEntity();
        deliveryPoint.setAddress(deliveryDTO.address());
        deliveryPoint.setDescription(deliveryDTO.description());
        deliveryPoint.setLocation(deliveryDTO.location().toString());
        deliveryPoint.setUserId(userId);
        deliveryPoint.setAssigned(false);
        deliveryPoint.setDistrict(deliveryDTO.district());
        Long id = deliveryRepository.insertDeliveryPointAddress(deliveryPoint);
        return new DeliveryDTO(
            id,
            deliveryDTO.assigned(),
            deliveryDTO.address(),
            deliveryDTO.description(),
            deliveryDTO.location(),
            deliveryDTO.district()
        );
    }

    @Transactional
    public void changeDeliveryPoint (String userId, Long deliveryPointId){
        deliveryRepository.updateAssignedByUserId(userId,false);
        deliveryRepository.updateAssignedByUserIdAndId(userId,deliveryPointId,true);
        orderRepository.updateByUserIdAndStatusAndMinDate(userId,LocalDate.now(),"P",deliveryPointId);
    }

    @Transactional
    public void removeDeliveryPoint(String userId, Long deliveryPointId){
        deliveryRepository.updateActiveByUserIdAndId(userId,deliveryPointId,false);
    }

    @Transactional
    public void initDelivery(AssignDeliveryPoint assignDeliveryPoint){
        List<OrderEntity> orderEntities = assignDeliveryPoint.orderIds().stream().map(id->{
                    OrderEntity order = new OrderEntity();
                    order.setId(id);
                    order.setStatus(StatusDeliveryEnum.IN_PROGRESS.getId());
                    order.setDeliveryUserId(assignDeliveryPoint.userId());
                    return order;
                }
        ).toList();

        this.orderRepository.updateOrdersStatusAndMotorized(orderEntities,StatusDeliveryEnum.PENDING.getId(), null);
        this.sendClientNotifications(assignDeliveryPoint.orderIds());
    }

    @Transactional
    public void deassignDelivery(AssignDeliveryPoint assignDeliveryPoint){
        List<OrderEntity> orderEntities = assignDeliveryPoint.orderIds().stream().map(id->{
                    OrderEntity order = new OrderEntity();
                    order.setId(id);
                    order.setStatus(StatusDeliveryEnum.PENDING.getId());
                    order.setDeliveryUserId(null);
                    return order;
                }
        ).toList();

        this.orderRepository.updateOrdersStatusAndMotorized(orderEntities,StatusDeliveryEnum.IN_PROGRESS.getId(), assignDeliveryPoint.userId());
    }

    private void sendClientNotifications(List<Long> ordersId) {
        try {
            List<String> phoneNumbers = orderRepository.findPhoneNumbersByOrderIds(ordersId);
            for (String phone : phoneNumbers) {
                try{
                    whatsappNotification.sendNotification(new WhatsappRequest(null, notifyOrder,phone));
                }catch (RestClientException e){
                    log.info("[sendClientNotifications] Error to try send message {}",e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[sendClientNotifications] Error to send phone numbers.{}", e.getMessage());
        }
    }
}