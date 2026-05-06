package com.xladmt.makify.feed.repository;

import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.feed.domain.FeedComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {

    // 특정 인증의 댓글 목록 (isVisible = Y)
    @Query("SELECT fc FROM FeedComment fc JOIN FETCH fc.member " +
            "WHERE fc.record.id = :recordId AND fc.isVisible = :isVisible " +
            "ORDER BY fc.createdAt ASC")
    List<FeedComment> findByRecordId(@Param("recordId") Long recordId,
                                     @Param("isVisible") YN isVisible);

    // 특정 인증의 댓글 수
    @Query("SELECT COUNT(fc) FROM FeedComment fc WHERE fc.record.id = :recordId AND fc.isVisible = 'Y'")
    int countByRecordId(@Param("recordId") Long recordId);
}
