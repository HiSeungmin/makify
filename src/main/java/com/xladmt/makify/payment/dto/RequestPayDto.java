package com.xladmt.makify.payment.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RequestPayDto {
        private String uuid;
        //private String challengeName;
        //private Long paymentPrice;
        private String buyerName;
        private String buyerEmail;

        @Builder
        public RequestPayDto(String uuid, String buyerName,String buyerEmail) {
            this.uuid = uuid;
            //this.challengeName = challengeName;
            this.buyerName = buyerName;
//            this.paymentPrice = paymentPrice;
            this.buyerEmail = buyerEmail;
        }
}