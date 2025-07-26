package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dto.request.EnglishNameRequest;
import com.studymate.domain.user.domain.dto.request.LocationRequest;
import com.studymate.domain.user.domain.dto.request.ProfileImageRequest;
import com.studymate.domain.user.domain.dto.request.SelfBioRequest;
import com.studymate.domain.user.domain.dto.response.LocationResponse;
import com.studymate.domain.user.domain.dto.response.UserNameResponse;
import com.studymate.domain.user.domain.repository.LocationRepository;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.Location;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    @Override
    public void saveEnglishName(EnglishNameRequest req) {
        UUID userId = req.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        user.setEnglishName(req.englishName());
        userRepository.save(user);
    }

    @Override
    public void saveProfileImage(ProfileImageRequest req){
        UUID userId = req.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        user.setProfileImage(req.profileImage());
        userRepository.save(user);
    }

    @Override
    public void saveSelfBio(SelfBioRequest req){
        UUID userId = req.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("NOT FOUND USER"));
        user.setSelfBio(req.selfBio());
        userRepository.save(user);
    }

    @Override
    public void saveLocation(LocationRequest req) {
        UUID userId = req.userId();
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


}
