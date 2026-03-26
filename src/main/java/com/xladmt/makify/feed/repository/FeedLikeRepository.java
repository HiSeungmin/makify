package com.xladmt.makify.feed.repository;

import com.xladmt.makify.common.entity.FeedLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    // 특정 인증에 특정 유저가 좋아요 눌렀는지 확인
    Optional<FeedLike> findByRecordIdAndMemberId(Long recordId, Long memberId);

    // 특정 인증의 좋아요 수
    @Query("SELECT COUNT(fl) FROM FeedLike fl WHERE fl.record.id = :recordId")
    int countByRecordId(@Param("recordId") Long recordId);
}
