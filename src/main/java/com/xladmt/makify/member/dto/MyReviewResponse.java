package com.xladmt.makify.member.dto;

import com.xladmt.makify.member.domain.Review;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class MyReviewResponse {

    private final List<ReviewItem> reviews;

    public MyReviewResponse(List<Review> reviews) {
        this.reviews = reviews.stream().map(ReviewItem::new).toList();
    }

    @Getter
    public static class ReviewItem {
        private final Long reviewId;
        private final Long challengeId;
        private final String challengeTitle;
        private final String content;
        private final int star;
        private final String createdAt;

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        public ReviewItem(Review review) {
            this.reviewId       = review.getId();
            this.challengeId    = review.getChallenge().getId();
            this.challengeTitle = review.getChallenge().getTitle();
            this.content        = review.getContent();
            this.star           = review.getStar();
            this.createdAt      = review.getCreateDate() != null
                    ? review.getCreateDate().format(FORMATTER) : "";
        }
    }
}
