package com.xladmt.makify.verification.service;

import com.xladmt.makify.verification.dto.HistoryResponse;
import com.xladmt.makify.verification.dto.VerifyResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VerificationService {
    VerifyResponse getVerifyPage(Long challengeId, Long memberId);
    void validateVerifyTime(Long challengeId);
    VerifyResponse getHistoryPage(Long challengeId, Long memberId);
    List<HistoryResponse> getRecords(Long challengeId, Long memberId);
    List<HistoryResponse> getOtherRecords(Long challengeId, Long memberId);
    int getTotalCount(Long challengeId, Long memberId);
    void verify(Long challengeId, Long memberId, MultipartFile image, String memo) throws IOException;
    void deleteVerify(long recordId, long memberId);
}
