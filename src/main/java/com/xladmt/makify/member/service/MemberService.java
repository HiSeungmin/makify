package com.xladmt.makify.member.service;

import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.member.dto.MyChallengeResponse;
import com.xladmt.makify.member.dto.MypageResponse;
import com.xladmt.makify.member.dto.SignupRequest;

public interface MemberService {
    Member signup(SignupRequest request);

    MypageResponse mypage(Long memberId);

    MyChallengeResponse myChallenge(Long memberId);
}
