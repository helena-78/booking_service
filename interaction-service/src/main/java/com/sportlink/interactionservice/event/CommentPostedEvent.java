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
public class CommentPostedEvent implements Serializable {
    private UUID commentId;
    private UUID activityId;
    private UUID authorId;
    private UUID activityOwnerId; // null when caller doesn't know the owner
    private String body;
    private LocalDateTime timestamp;
}
