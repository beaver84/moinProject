package com.example.moinproject.repository;

import com.example.moinproject.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 주어진 userId로 사용자가 존재하는지 확인합니다.
     * @param userId 확인할 사용자 ID (이메일)
     * @return 사용자가 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByUserId(String userId);

    /**
     * 주어진 userId로 사용자를 찾습니다.
     * @param userId 찾을 사용자 ID (이메일)
     * @return 찾은 사용자 Optional 객체
     */
    Optional<User> findByUserId(String userId);

    /**
     * 주어진 phoneNumber로 사용자를 찾습니다.
     * @param phoneNumber 찾을 사용자의 전화번호
     * @return 찾은 사용자 Optional 객체
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
}