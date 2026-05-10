package com.sportlink.interactionservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowedEvent implements Serializable {
    private UUID followerId;
    private UUID followeeId;
    private LocalDateTime timestamp;
}
