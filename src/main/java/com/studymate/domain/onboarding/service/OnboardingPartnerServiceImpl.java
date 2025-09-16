package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.PartnerGenderRequest;
import com.studymate.domain.onboarding.domain.dto.request.PartnerRequest;
import com.studymate.domain.onboarding.domain.dto.response.LearningExpectationResponse;
import com.studymate.domain.onboarding.domain.dto.response.PartnerGenderResponse;
import com.studymate.domain.onboarding.domain.dto.response.PartnerPersonalityResponse;
import com.studymate.domain.onboarding.domain.repository.OnboardingPartnerRepository;
import com.studymate.domain.onboarding.domain.repository.PartnerPersonalityRepository;
import com.studymate.domain.onboarding.domain.type.LearningExpectionType;
import com.studymate.domain.onboarding.domain.type.PartnerGenderType;
import com.studymate.domain.onboarding.entity.OnboardingPartner;
import com.studymate.domain.onboarding.entity.OnboardingPartnerId;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OnboardingPartnerServiceImpl implements OnboardingPartnerService {

    private final OnboardingPartnerRepository onboardingPartnerRepository;
    private final UserRepository userRepository;
    private final PartnerPersonalityRepository partnerPersonalityRepository;

    @Override
    public void savePartnerPersonality(UUID userId,PartnerRequest req){
        List<Integer> partnerPersonalityIds = req.personalPartnerIds();
        List<OnboardingPartner> onboardPartners = partnerPersonalityIds.stream()
                .map(partnerPersonalityId-> OnboardingPartner.builder()
                        .id(new OnboardingPartnerId(userId,partnerPersonalityId))
                        .build())
                .collect(Collectors.toList());
        onboardingPartnerRepository.saveAll(onboardPartners);

    }

    @Override
    public void savePartnerGender(UUID userId,PartnerGenderRequest req) {
        PartnerGenderType partnerGenderType = req.partnerGenderType();
        User user = userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException("USER NOT FOUND"));
        user.setPartnerGender(partnerGenderType);
        userRepository.save(user);

    }

    @Override
    public List<PartnerGenderResponse> getAllPartnerGenderType() {
        return Arrays.stream(PartnerGenderType.values())
                .map(e -> new PartnerGenderResponse(e.name(), e.getDescription()))
                .toList();
    }

    @Override
    public List<PartnerPersonalityResponse> getAllPartnerPersonality() {
        return partnerPersonalityRepository.findAll().stream()
                .map(p->new PartnerPersonalityResponse(
                        p.getPartnerPersonalityId(),
                        p.getPartnerPersonality()
                ))
                .toList();
    }

}
