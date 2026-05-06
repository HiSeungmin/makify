package com.xladmt.makify.notification.service;

import com.xladmt.makify.member.domain.Member;
import com.xladmt.makify.verification.domain.PushSubscription;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.notification.dto.PushSubscriptionRequest;
import com.xladmt.makify.notification.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PushSubscriptionServiceImpl implements PushSubscriptionService {
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void subscribe(Long memberId, PushSubscriptionRequest request) {
        Optional<PushSubscription> existing =
                pushSubscriptionRepository.findByEndpoint(request.getEndpoint());

        if (existing.isPresent()) {
            PushSubscription sub = existing.get();
            // 다른 회원이 같은 브라우저로 로그인한 경우 회원도 갱신
            if (!sub.getMember().getId().equals(memberId)) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
                sub.updateMember(member);
            }
            sub.updateKeys(
                    request.getKeys().getP256dh(),
                    request.getKeys().getAuth());
        } else {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            PushSubscription sub = PushSubscription.create(
                    member,
                    request.getEndpoint(),
                    request.getKeys().getP256dh(),
                    request.getKeys().getAuth());
            pushSubscriptionRepository.save(sub);
        }
    }

    @Override
    @Transactional
    public void unsubscribe(String endpoint) {
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
    }

}
