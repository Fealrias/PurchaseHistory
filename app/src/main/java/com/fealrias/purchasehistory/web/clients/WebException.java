package com.fealrias.purchasehistory.web.clients;

import com.fealrias.purchasehistory.data.Constants;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
public class WebException extends RuntimeException {
    Integer errorResource;
    String errorCode;


    public WebException(ErrorResponse errorResponse) {
        super(errorResponse.getDetail());
        this.errorCode = errorResponse.getErrorCode();
    }

    public WebException(Integer errorMessage) {
        super(errorMessage.toString());
        this.errorResource = errorMessage;
    }

    public Integer getErrorResource() {
        if (errorResource != null)
            return errorResource;
        return Constants.errorsMap.get(errorCode);
    }
}
