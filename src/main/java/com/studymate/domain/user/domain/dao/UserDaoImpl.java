package com.studymate.domain.user.domain.dao;

import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUserIdentity(String identity){
        return userRepository.findByUserIdentity(identity);
    }
    @Override
    public User save(User user){
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUserId(UUID userId){
        return userRepository.findById(userId);
    }
}
