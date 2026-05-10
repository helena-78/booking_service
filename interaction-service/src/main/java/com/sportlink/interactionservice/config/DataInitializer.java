package com.sportlink.interactionservice.config;

import com.sportlink.interactionservice.model.Comment;
import com.sportlink.interactionservice.model.Follow;
import com.sportlink.interactionservice.repository.CommentRepository;
import com.sportlink.interactionservice.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

// @Profile("!test") ensures this is never loaded by @WebMvcTest or any test that sets profile="test"
@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(FollowRepository followRepository, CommentRepository commentRepository) {
        return args -> {
            if (followRepository.count() == 0) {
                UUID user1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
                UUID user2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
                UUID user3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
                UUID activity1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

                followRepository.save(Follow.builder().followerId(user1).followeeId(user2).build());
                followRepository.save(Follow.builder().followerId(user1).followeeId(user3).build());
                followRepository.save(Follow.builder().followerId(user2).followeeId(user1).build());

                commentRepository.save(Comment.builder().activityId(activity1).authorId(user1)
                        .body("Great activity, looking forward to it!").build());
                commentRepository.save(Comment.builder().activityId(activity1).authorId(user2)
                        .body("Count me in!").build());
            }
        };
    }
}
