package com.fealrias.purchasehistory.web.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ErrorResponse {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String errorCode;
    private String instance;
}
