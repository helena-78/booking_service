package com.sportlink.moderation.repository;

import com.sportlink.moderation.model.AccountSanction;
import com.sportlink.moderation.model.enums.AccountSyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountSanctionRepository extends JpaRepository<AccountSanction, UUID> {

    List<AccountSanction> findByTargetUserIdOrderByCreatedAtDesc(UUID targetUserId);

    List<AccountSanction> findByAccountSyncStatus(AccountSyncStatus accountSyncStatus);
}
