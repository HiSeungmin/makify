package com.xladmt.makify.notification.service;

import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.entity.PushSubscription;
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
            // 같은 endpoint면 키만 갱신 (브라우저가 키를 재발급하는 경우)
            existing.get().updateKeys(
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
