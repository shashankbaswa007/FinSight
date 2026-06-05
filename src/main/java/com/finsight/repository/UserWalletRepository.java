package com.finsight.repository;

import com.finsight.model.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
    
    List<UserWallet> findByUserId(Long userId);
    
    Optional<UserWallet> findByUserIdAndCurrencyId(Long userId, Long currencyId);
    
    Optional<UserWallet> findByUserIdAndPrimaryWallet(Long userId, Boolean primaryWallet);
}
