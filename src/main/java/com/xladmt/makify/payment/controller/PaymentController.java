package com.xladmt.makify.payment.controller;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.xladmt.makify.payment.dto.PaymentCallbackRequest;
import com.xladmt.makify.payment.dto.RequestPayDto;
import com.xladmt.makify.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

//    @GetMapping("/payment/{id}")
//    public String paymentPage(@PathVariable(name = "id", required = false) String id,
//                              Model model) {
//
//        RequestPayDto requestDto = paymentService.findRequestDto(id);
//        model.addAttribute("requestDto", requestDto);
//        return "payment";
//    }

    @ResponseBody
    @PostMapping("/payment")
    public ResponseEntity<IamportResponse<Payment>> validationPayment(@RequestBody PaymentCallbackRequest request) {
        IamportResponse<Payment> iamportResponse = paymentService.paymentByCallback(request);

        log.info("결제 응답={}", iamportResponse.getResponse().toString());

        return new ResponseEntity<>(iamportResponse, HttpStatus.OK);
    }

    @GetMapping("/success-payment")
    public String successPaymentPage() {
        return "/payment/success-payment";
    }

    @GetMapping("/fail-payment")
    public String failPaymentPage() {
        return "/payment/fail-payment";
    }
}
