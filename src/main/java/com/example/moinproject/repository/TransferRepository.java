package com.example.moinproject.repository;

import com.example.moinproject.domain.entity.Transfer;
import com.example.moinproject.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByUserOrderByRequestedDateDesc(User user);
}