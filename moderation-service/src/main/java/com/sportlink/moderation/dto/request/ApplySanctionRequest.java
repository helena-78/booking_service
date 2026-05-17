package com.sportlink.moderation.dto.request;

import com.sportlink.moderation.model.enums.SanctionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplySanctionRequest {

    @NotNull
    private UUID caseId;

    @NotNull
    private UUID targetUserId;

    @NotNull
    private SanctionType sanctionType;
}
