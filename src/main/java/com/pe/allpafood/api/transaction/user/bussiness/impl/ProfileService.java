package com.pe.allpafood.api.transaction.user.bussiness.impl;


import com.pe.allpafood.api.core.utils.converter.JsonUtil;
import com.pe.allpafood.api.core.utils.dto.GeoPoint;
import com.pe.allpafood.api.transaction.user.entities.InformationEntity;
import com.pe.allpafood.api.transaction.user.dto.ProfileDTO;
import com.pe.allpafood.api.transaction.order.entity.DeliveryPointEntity;
import com.pe.allpafood.api.transaction.order.repository.impl.DeliveryRepository;
import com.pe.allpafood.api.transaction.setting.SettingRepository;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.user.repository.ProfileRepository;
import com.pe.allpafood.api.transaction.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final SettingRepository settingRepository;

    public ProfileDTO getProfileByUserId(String userId) {
        log.info("getProfileByUserId {}", userId);
        ProfileEntity profile = profileRepository.findByUserId(userId);
        log.info("profile got {}", profile);

        if (profile==null) return null;

        log.info("User Profile es diferente de null {}", userId);

        if(profile.getInformation()!=null) profile.setInformationDeserializer(JsonUtil.convertToObject(profile.getInformation(), InformationEntity.class));

        DeliveryPointEntity deliveryPointEntity = deliveryRepository.findByUserIdAndAssigned(userId);

        if (deliveryPointEntity != null) {
            if (deliveryPointEntity.getLocation() != null)
                deliveryPointEntity.setGeoLocation(new GeoPoint(deliveryPointEntity.getLocation()));
        }else {
            deliveryPointEntity = new DeliveryPointEntity();
        }

        return this.mapDTOFromProfile(profile,deliveryPointEntity);
    }

    public List<ProfileEntity> getUserAlimentsRestriction(List<String> userIds){
        log.info("Starting getUserAlimentsRestriction");
        return profileRepository.getAilmentsRestrictionsByUserIds(userIds);
    }

    @Transactional
    public ProfileDTO completeProfile(String userId, ProfileDTO dto) {
        ProfileEntity profileEntity = mapProfileFromDTO(dto);
        profileEntity.setUserId(userId);
        log.info("Profile completed {} - {}", userId, dto);
        Integer recommendedPlanId = settingRepository.findRecommendedPlanId("objective_plan_recommended",dto.information().getNutritionalObjective());
        profileEntity.setRecommendedPlanId(recommendedPlanId);
        profileRepository.saveProfile(profileEntity);

        if (!userRepository.isVerifiedUser(userId)) throw new RuntimeException("El usuario no se encuentra verificado.");

        if (dto.location()!=null && dto.location().getLatitude()!=null && dto.location().getLongitude()!=null) {

            DeliveryPointEntity deliveryPointFounded = deliveryRepository.findByUserIdAndAssigned(userId);
            DeliveryPointEntity deliveryPointEntity;

            if (deliveryPointFounded==null) {
                deliveryPointEntity = mapDeliveryFromDTO(userId,dto,true);
            }else {
                deliveryPointEntity = mapDeliveryFromDTO(userId,dto,false);
                deliveryPointEntity.setId(deliveryPointFounded.getId());
            }
            log.info("Profile completed {} - {}", userId, deliveryPointEntity);

            deliveryRepository.insertDeliveryPointAddress(deliveryPointEntity);
        }

        userRepository.updateProfileCompleted(userId,true);

        return dto;
    }

    private DeliveryPointEntity mapDeliveryFromDTO(String userId, ProfileDTO dto, boolean assigned){
        DeliveryPointEntity  deliveryPoint = new DeliveryPointEntity();
        deliveryPoint.setUserId(userId);
        deliveryPoint.setAddress(dto.address());
        deliveryPoint.setDescription(dto.descriptionAddress());
        deliveryPoint.setGeoLocation(dto.location());
        deliveryPoint.setLocation(dto.location().toString());
        deliveryPoint.setAssigned(assigned);
        deliveryPoint.setDistrict(dto.districtLocation());
        return deliveryPoint;
    }

    private ProfileEntity mapProfileFromDTO(ProfileDTO dto) {
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setAddress(dto.address());
        profileEntity.setDistrict(dto.district());
        profileEntity.setBornDate(dto.bornDate());
        profileEntity.setInformation(JsonUtil.convertToJsonString(dto.information()));
        return profileEntity;
    }

    private ProfileDTO mapDTOFromProfile(ProfileEntity profileEntity, DeliveryPointEntity deliveryPoint) {
        return new ProfileDTO(
                profileEntity.getBornDate(),
                profileEntity.getDistrict(),
                deliveryPoint.getAddress(),
                deliveryPoint.getDescription(),
                deliveryPoint.getGeoLocation(),
                deliveryPoint.getDistrict(),
                profileEntity.getInformationDeserializer(),
                profileEntity.getName(),
                profileEntity.getImageUrl()
        );
    }

}
