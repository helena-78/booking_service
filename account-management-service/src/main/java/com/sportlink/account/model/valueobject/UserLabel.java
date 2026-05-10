package com.sportlink.account.model.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLabel {

    @Column(name = "behavior_label")
    private String behaviorLabel;

    @Column(name = "label_value")
    private String labelValue;
}
