package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.application.mapper.UserMapper;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.out.UserRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.UserEntity;
import com.agileforge.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository, UserMapper userMapper) {
        this.jpaUserRepository = jpaUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = jpaUserRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findByIdAndDeletedFalse(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmailAndDeletedFalse(email)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmailAndDeletedFalse(email);
    }
}
