package com.pe.allpafood.api.transaction.user.bussiness.impl;

import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.core.exception.NotFoundException;
import com.pe.allpafood.api.core.utils.dto.PageResult;
import com.pe.allpafood.api.core.utils.generator.CodesUtil;
import com.pe.allpafood.api.gateway.admin.users.dto.DetailUserDTO;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import com.pe.allpafood.api.transaction.user.repository.ProfileRepository;
import com.pe.allpafood.api.transaction.user.repository.UserRepository;
import com.pe.allpafood.api.transaction.user.utils.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public PageResult<UserEntity> listUsers(String email, Integer roleId, int page, int size) {
        return userRepository.findUsers(email, roleId, page, size);
    }

    public void updateUserStatus(String userId, Integer status) {

        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        UserStatus userStatus = UserStatus.fromValue(status);

        int updated = userRepository.updateUserStatus(userId, userStatus.getValue());

        if (updated == 0) {
            throw new RuntimeException("User not found or already in that status");
        }
    }


    @Transactional
    public void registerUser(DetailUserDTO user){
        try{
            UserEntity userEntity = new UserEntity();
            userEntity.setId(CodesUtil.randomId());
            userEntity.setEmail(user.email());
            userEntity.setPhoneNumber(user.phoneNumber());
            userEntity.setPassword(passwordEncoder.encode(user.password()));
            userEntity.setProvider("user-pass");
            userEntity.setDocumentNumber(user.documentNumber());
            userEntity.setVerified(true);
            userEntity.setRegistrationCompleted(user.roleId().equals("USER"));
            userEntity.setStatus(user.status());
            userRepository.insert(userEntity);
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
    public void updateInformation(DetailUserDTO userDto, String id, String updatedBy){
        try {

            var user = userRepository.findById(id);
            if (user == null)
                throw new NotFoundException("Usuarioa no econtrado.");
            UserEntity userEntity = new UserEntity();
            userEntity.setId(id);
            userEntity.setEmail(userDto.email());
            userEntity.setPhoneNumber(userDto.phoneNumber());
            userEntity.setDocumentNumber(userDto.documentNumber());
            userEntity.setStatus(userDto.status());

            userRepository.update(userEntity);

            ProfileEntity profileEntity = new ProfileEntity();
            profileEntity.setUserId(userEntity.getId());
            profileEntity.setName(userDto.name());
            profileEntity.setLastname(userDto.lastname());

            if (!userDto.districts().isEmpty()){
                profileEntity.setDistrict(String.join(",", userDto.districts()));
            }

            profileEntity.setModifiedAt(LocalDateTime.now());
            profileEntity.setModifiedBy(updatedBy);
            profileRepository.saveProfile(profileEntity);
        }catch (DuplicateKeyException e){
            throw new BusinessException("El usuario ya existe");
        }
    }

}
