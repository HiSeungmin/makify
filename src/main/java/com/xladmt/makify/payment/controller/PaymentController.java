package com.xladmt.makify.payment.controller;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.application.PaymentFacade;
import com.xladmt.makify.payment.dto.PaymentCallbackRequest;
import com.xladmt.makify.payment.dto.PaymentInitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    /**
     * 결제 초기화 - "결제하기" 버튼 클릭 시 호출
     */
    @ResponseBody
    @PostMapping("/payment/init")
    public ResponseEntity<Map<String, String>> initPayment(@RequestBody PaymentInitRequest request) {

        String uuid = paymentFacade.initializePayment(request);

        if (uuid == null) { return new ResponseEntity<>(HttpStatus.BAD_REQUEST); }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "uuid", uuid,
                "message", "결제 초기화 성공"
        ));
    }

    /**
     * 결제 완료 콜백 - 외부 결제 완료 후 호출
     */
    @ResponseBody
    @PostMapping("/payment/callback")
    public ResponseEntity<Map<String, Object>> paymentCallback(@RequestBody PaymentCallbackRequest request) {

        IamportResponse<Payment> iamportResponse = paymentFacade.processPaymentCallback(request);

        if (iamportResponse == null) { return new ResponseEntity<>(HttpStatus.BAD_REQUEST); }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "결제 완료",
                "paymentInfo", iamportResponse.getResponse()
        ));
    }

    @GetMapping("/fail-payment")
    public String failPaymentPage() {
        return "/error/error";
    }
}
