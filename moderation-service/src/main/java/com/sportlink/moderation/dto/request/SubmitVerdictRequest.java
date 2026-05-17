package com.sportlink.moderation.dto.request;

import com.sportlink.moderation.model.enums.Verdict;
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
public class SubmitVerdictRequest {

    @NotNull
    private Verdict verdict;

    @NotNull
    private UUID moderatorId;
}
