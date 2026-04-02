package com.xladmt.makify.feed.repository;

import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.ChallengeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<ChallengeRecord, Long> {

    // 최신순
    @Query("SELECT cr FROM ChallengeRecord cr " +
            "JOIN FETCH cr.member " +
            "JOIN FETCH cr.challenge " +
            "WHERE cr.isVisible = :isVisible " +
            "AND cr.isApproved = :isApproved " +
            "AND cr.isPublic = :isPublic " +
            "ORDER BY cr.verificatedDate DESC")
    List<ChallengeRecord> findFeedOrderByLatest(@Param("isVisible") YN isVisible,
                                                @Param("isApproved") YN isApproved,
                                                @Param("isPublic") YN isPublic);

    // 좋아요순
    @Query("SELECT cr FROM ChallengeRecord cr " +
            "JOIN FETCH cr.member " +
            "JOIN FETCH cr.challenge " +
            "LEFT JOIN FeedLike fl ON fl.record.id = cr.id " +
            "WHERE cr.isVisible = :isVisible " +
            "AND cr.isApproved = :isApproved " +
            "AND cr.isPublic = :isPublic " +
            "GROUP BY cr.id " +
            "ORDER BY COUNT(fl.id) DESC, cr.verificatedDate DESC")
    List<ChallengeRecord> findFeedOrderByLikes(@Param("isVisible") YN isVisible,
                                               @Param("isApproved") YN isApproved,
                                               @Param("isPublic") YN isPublic);
}
