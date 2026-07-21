package com.pe.allpafood.api.transaction.user.bussiness.impl;

import com.opencsv.CSVReader;
import com.pe.allpafood.api.core.utils.generator.CodesUtil;
import com.pe.allpafood.api.core.exception.BusinessException;
import com.pe.allpafood.api.transaction.notification.bussiness.impl.NotificationAsyncExecutor;
import com.pe.allpafood.api.transaction.notification.entity.TemplateRequest;
import com.pe.allpafood.api.transaction.notification.entity.WhatsappRequest;
import com.pe.allpafood.api.transaction.notification.bussiness.impl.WhatsappNotification;
import com.pe.allpafood.api.transaction.auth.dto.FormUserDTO;
import com.pe.allpafood.api.transaction.user.repository.ProfileRepository;
import com.pe.allpafood.api.transaction.user.repository.UserRepository;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import com.pe.allpafood.api.transaction.user.entities.ProfileEntity;
import com.pe.allpafood.api.transaction.plan.bussiness.impl.SubscriptionService;
import com.pe.allpafood.api.transaction.user.utils.UserErroEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegisterService {

    @Value("${notification.service.whatsapp.template-verification}")
    private String templateVerification;

    @Value("${notification.service.whatsapp.template-verification.button-url}")
    private String buttonUrl;


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WhatsappNotification whatsappNotification;
    private final ProfileRepository profileRepository;
    private final SubscriptionService subscriptionService;

    @Transactional(rollbackFor = {DuplicateKeyException.class, BusinessException.class, Exception.class})
    public UserEntity registerUser(String userId, FormUserDTO userDTO) {
        if (!userRepository.isVerifiedUser(userId)) throw new RuntimeException(UserErroEnum.USER_NOT_VERIFIED.getValue());

        String provider = userRepository.findProviderById(userId);
        if(provider==null) throw new RuntimeException(UserErroEnum.USER_NOT_FOUND.getValue());

        UserEntity user = new UserEntity();

        if (provider.equals("user-pass")){
            user.setEmail(userDTO.email());
            user.setPassword(passwordEncoder.encode(userDTO.password()));
        }
        user.setId(userId);
        user.setDocumentNumber(userDTO.documentNumber());
        user.setRegistrationCompleted(true);

        try{
            userRepository.updateByPhoneVerification(user);
        }catch (DuplicateKeyException e){
            throw new BusinessException(UserErroEnum.DATA_DUPLICATED.getValue());
        }

        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setUserId(user.getId());
        profileEntity.setName(userDTO.name());
        profileEntity.setLastname(userDTO.lastname());
        profileRepository.saveProfile(profileEntity);
        return user;
    }

    @Transactional(rollbackFor = {DuplicateKeyException.class, BusinessException.class})
    public void uploadMassive(MultipartFile file, Integer corporation, Integer planId, List<Integer> complements) throws BusinessException {

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] line;
            reader.readNext();

            while ((line = reader.readNext()) != null) {
                UserEntity user = new UserEntity();
                String phoneNumber = "51".concat(line[4]);
                String email = line[3];
                String userId=userRepository.findIdCorporateUser(phoneNumber,email,corporation);

                if(userId==null){
                    log.info("No existe el siguiente usuario corporativo {}-{}",phoneNumber,email);

                    userId = CodesUtil.randomId();
                    user.setId(userId);
                    user.setEmail(email);
                    user.setPhoneNumber(phoneNumber);
                    user.setCorporateUser(true);
                    user.setCorporationId(corporation);
                    user.setProfileCompleted(false);
                    user.setRegistrationCompleted(true);
                    user.setVerified(true);
                    user.setProvider("user-pass");
                    user.setPassword(passwordEncoder.encode(line[2]));

                    try{
                        userRepository.insertByCsv(user);
                    }catch (DuplicateKeyException e){
                        throw  new BusinessException("El siguente email: "+user.getEmail()+" ya se encuentra registrado.");
                    }

                    ProfileEntity profile = new ProfileEntity();
                    profile.setUserId(userId);
                    profile.setName(line[0]);
                    profile.setLastname(line[1]);
                    profile.setDocumentNumber(line[2]);
                    profileRepository.saveProfile(profile);
                }

                subscriptionService.subscribeUserToPlan(userId,null,complements, null,null);
            }
        }catch (Exception e){
            log.info("Error en la lectura del archivo : {}",e.getMessage());
            throw  new BusinessException(UserErroEnum.CSV_UPLOAD_ERR.getValue());
        }
    }

    @Transactional
    public String sendVerificationCode(String phoneNumber) throws BusinessException {

        UserEntity userEntity = this.userRepository.findByPhoneNumber(phoneNumber);
        log.info("[sendVerificationCode] user : {}", userEntity);

        if(userEntity!=null){
            if(userEntity.getVerified() && userEntity.isRegistrationCompleted()){
                log.info("[sendVerificationCode] {} - Número verificado.", phoneNumber);
                throw new BusinessException(UserErroEnum.NUM_PENDENT_VERIFICATION.getValue());
            }

            if (userEntity.getVerificationCodeExp() != null && userEntity.getVerificationCodeExp().isAfter(LocalDateTime.now())) {
                log.info("[sendVerificationCode] {} - Número pendiente de verificación.", phoneNumber);
                throw new BusinessException(UserErroEnum.NUM_ALREADY_VERIFIED.getValue());
            }

            log.info("[sendVerificationCode] {} - Eliminando usuario no verificado.", phoneNumber);
            this.userRepository.deleteByUserId(userEntity.getId());
        }

        userEntity = new UserEntity();
        userEntity.setId(CodesUtil.randomId());
        userEntity.setVerified(false);
        userEntity.setRegistrationCompleted(false);
        userEntity.setProfileCompleted(false);
        userEntity.setPhoneNumber(phoneNumber);
        userEntity.setProvider("user-pass");
        userEntity.setCorporateUser(false);

        String code = CodesUtil.randomNumber();
        userEntity.setVerificationCode(this.passwordEncoder.encode(code));
        userEntity.setVerificationCodeExp(LocalDateTime.now().plusMinutes(10));

        log.info("User inserted verification code: {}: ",userEntity);

        try{
            this.userRepository.insertUserByCodeVerification(userEntity);
            this.whatsappNotification.sendNotification(new WhatsappRequest(this.getComponentsTemplateVerification(code),this.templateVerification,phoneNumber));
            this.userRepository.insertUserRole(userEntity.getId(), 1);
        } catch (Exception e){
            log.info("Error to execute verify code : {}",e.getMessage());
            throw new BusinessException(UserErroEnum.SEND_VERIFICATION_ERR.getValue());
        }

        return userEntity.getId();
    }

    private List<TemplateRequest.Component> getComponentsTemplateVerification(String code){
        TemplateRequest.Component componentBody = new TemplateRequest.Component();
        componentBody.setType("body");
        this.setParameter(componentBody,code);

        TemplateRequest.Component componentButton = new TemplateRequest.Component();
        componentButton.setType("button");
        componentButton.setIndex(0);
        componentButton.setSubType("url");
        this.setParameter(componentButton,this.buttonUrl);

        var components = List.of(componentBody, componentButton);
        log.info("[getComponentsTemplateVerification] components: {}",components);
        return components;
    }

    private void setParameter(TemplateRequest.Component component, String text){
        TemplateRequest.Parameter parameter = new TemplateRequest.Parameter();
        parameter.setType("text");
        parameter.setText(text);
        component.setParameters(List.of(parameter));
    }

    @Transactional
    public void verifyCode(String userId, String code){
        UserEntity userEntity = userRepository.findById(userId);
        log.debug("[verifyCode] UserEntity: {}",userEntity);
        if(userEntity==null) throw new RuntimeException("El usuario no existe");

        if(userEntity.getVerified()) throw new RuntimeException("Ya se encuentra verificado.");

        if(passwordEncoder.matches(code, userEntity.getVerificationCode())){
            log.info("El código de verificación es correcto para user : {}",userEntity.getId());

            if(LocalDateTime.now().isBefore(userEntity.getVerificationCodeExp())){
                log.info("El código de verificación aun no expiró : {}",userEntity.getId());
                userEntity.setVerified(true);
                userEntity.setVerificationCode(null);
                userEntity.setVerificationCodeExp(null);
                userRepository.updateUserVerificationCode(userEntity);
            }else{
                log.info("El código ha expirado {} :",userEntity.getId());
                userRepository.deleteByUserId(userEntity.getId());
                throw new RuntimeException("El código ha expirado.");
            }
        }else{
            throw new RuntimeException("El código no coincide.");
        }
    }
}
