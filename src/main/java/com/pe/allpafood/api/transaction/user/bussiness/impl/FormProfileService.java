package com.pe.allpafood.api.transaction.user.bussiness.impl;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.utils.dto.GeoPoint;
import com.pe.allpafood.api.transaction.user.entities.InformationEntity;
import com.pe.allpafood.api.transaction.user.dto.DeliveryDTO;
import com.pe.allpafood.api.transaction.user.dto.FormPersonalData;
import com.pe.allpafood.api.transaction.user.dto.FormPrivacyData;
import com.pe.allpafood.api.transaction.order.entity.DeliveryPointEntity;
import com.pe.allpafood.api.transaction.order.repository.impl.DeliveryRepository;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.user.repository.ProfileRepository;
import com.pe.allpafood.api.transaction.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormProfileService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePersonalData(String userId,FormPersonalData formPersonalData){
        profileRepository.updateBasicProfile(formPersonalData.name(), formPersonalData.lastname(), formPersonalData.gender(), userId, formPersonalData.image());
    }

    @Transactional
    public void changePrivacyData(String userId, FormPrivacyData formPrivacyData){
        userRepository.updateDocAndPhoneNumber(userId,formPrivacyData.documentNumber(), formPrivacyData.phoneNumber());
        profileRepository.updateAddressAndBornDate(userId,formPrivacyData.address(),formPrivacyData.bornDate());
    }

    @Transactional
    public void changePassword(String userId,String password, String newPassword){
        String currentPassword = userRepository.findPasswordById(userId);
        if (!passwordEncoder.matches(password,currentPassword)) throw  new BusinessException("La contraseña es incorrecta.");
        userRepository.updatePasswordById(userId,passwordEncoder.encode(newPassword));
    }

    public FormPrivacyData getUserPrivacyData(String userId) {
        ProfileEntity profileEntity = profileRepository.findPrivacyDataByUserId(userId);
        return new FormPrivacyData(
                profileEntity.getDocumentNumber(),
                profileEntity.getPhoneNumber(),
                profileEntity.getBornDate(),
                profileEntity.getAddress()
        );
    }

    public FormPersonalData getUserPersonalData(String userId) {
        ProfileEntity profileEntity = profileRepository.findPersonalDataByUserId(userId);
        InformationEntity information = JsonUtil.convertToObject(profileEntity.getInformation(),InformationEntity.class);
        assert information != null;
        return new FormPersonalData(
                profileEntity.getName(),
                profileEntity.getLastname(),
                information.getGender(),
                profileEntity.getRegisterDate(),
                profileEntity.getImageUrl()

        );
    }

    public DeliveryDTO getFormDelivery(String userId){
        log.info("[getFormDelivery] Starting form delivery - {}", userId);
        DeliveryPointEntity entity = deliveryRepository.findByUserIdAndAssigned(userId);
        log.debug("[getFormDelivery] DeliveryPointEntity founded - {}", entity);
        return new DeliveryDTO(
                entity.getId(),
                true,
                entity.getAddress(),
                entity.getDescription(),
                new GeoPoint(entity.getLocation()),
                entity.getDistrict()
        );
    }

}
