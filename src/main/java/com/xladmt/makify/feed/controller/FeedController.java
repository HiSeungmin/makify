package com.xladmt.makify.feed.controller;

import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.feed.dto.FeedCommentResponse;
import com.xladmt.makify.feed.dto.FeedResponse;
import com.xladmt.makify.feed.dto.ToggleLikeResponse;
import com.xladmt.makify.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/feed")
    public String feed(Model model,
                       @RequestParam(defaultValue = "latest") String sort,
                       @AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = memberDetails != null ? memberDetails.getMember().getId() : null;
        String nickname = memberDetails != null ? memberDetails.getMember().getNickname() : null;
        List<FeedResponse> feeds = feedService.getFeed(memberId, sort);
        model.addAttribute("feeds", feeds);
        model.addAttribute("currentNickname", nickname);
        model.addAttribute("currentSort", sort);
        return "feed/feed";
    }

    @PostMapping("/api/feed/{recordId}/like")
    @ResponseBody
    public ResponseEntity<ToggleLikeResponse> toggleLike(
            @PathVariable Long recordId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = memberDetails.getMember().getId();
        return ResponseEntity.ok(feedService.toggleLike(recordId, memberId));
    }

    @GetMapping("/api/feed/{recordId}/comments")
    @ResponseBody
    public ResponseEntity<List<FeedCommentResponse>> getComments(@PathVariable Long recordId) {
        return ResponseEntity.ok(feedService.getComments(recordId));
    }

    @PostMapping("/api/feed/{recordId}/comments")
    @ResponseBody
    public ResponseEntity<FeedCommentResponse> addComment(
            @PathVariable Long recordId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = memberDetails.getMember().getId();
        String content = body.get("content");
        return ResponseEntity.ok(feedService.addComment(recordId, memberId, content));
    }

    @DeleteMapping("/api/feed/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = memberDetails.getMember().getId();
        feedService.deleteComment(commentId, memberId);
        return ResponseEntity.ok().build();
    }
}
