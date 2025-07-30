package com.studymate.domain.user.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.studymate.domain.user.domain.dto.request.EnglishNameRequest;
import com.studymate.domain.user.domain.dto.request.LocationRequest;
import com.studymate.domain.user.domain.dto.request.ProfileImageRequest;
import com.studymate.domain.user.domain.dto.request.SelfBioRequest;
import com.studymate.domain.user.domain.dto.response.LocationResponse;
import com.studymate.domain.user.domain.dto.response.ProfileImageUrlResponse;
import com.studymate.domain.user.domain.dto.response.UserNameResponse;
import com.studymate.domain.user.domain.repository.LocationRepository;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.Location;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final AmazonS3Client amazonS3;
    private final String bucketName = "languagemate-profile-img";

    @Override
    public void saveEnglishName(UUID userId,EnglishNameRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        user.setEnglishName(req.englishName());
        userRepository.save(user);
    }

    @Override
    public void saveProfileImage(UUID userId, MultipartFile file){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));

        String key = "profile-image/" + userId + "_" + file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    key,
                    file.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);//공개 설정
            amazonS3.putObject(request);

            String url = amazonS3.getUrl(bucketName, key).toString();
            user.setProfileImage(url);
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드 실패",e);
        }


    }

    @Override
    public void saveSelfBio(UUID userId,SelfBioRequest req){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        user.setSelfBio(req.selfBio());
        userRepository.save(user);
    }

    @Override
    public void saveLocation(UUID userId,LocationRequest req) {
        int locationId = req.locationId();
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
       Location location = locationRepository.findById(locationId)
                       .orElseThrow(()-> new NotFoundException("NOT FOUND LOCATION"));
        user.setLocation(location);
        userRepository.save(user);

    }

    @Override
    public List<LocationResponse> getAllLocation() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream()
                .map(loc -> new LocationResponse(
                        loc.getLocationId(),
                        loc.getCountry(),
                        loc.getCity(),
                        loc.getTimeZone()
                ))
                .toList();

    }

    @Override
    public UserNameResponse getUserName(UUID userId) {
        String name = userRepository.findNameByUserId(userId);
        return new UserNameResponse(name);
    }

    @Override
    public ProfileImageUrlResponse getProfileImageUrl(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        return new ProfileImageUrlResponse(user.getProfileImage());

    }


}
