package com.kshrd.kroya_api.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kshrd.kroya_api.enums.IResponseMessage;
import com.kshrd.kroya_api.enums.ResponseMessage;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class    BaseResponse {
    private Object payload;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String inqCnt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object inqRec;
    private String message;
    private String code;
    @JsonIgnore
    @Builder.Default
    private IResponseMessage responseMessage = ResponseMessage.OK;
    @Builder.Default
    private boolean isError = false;
    @JsonIgnore
    private org.springframework.data.domain.Page<?> rawData;

    public String getMessage() {
        if(this.message != null)
            return this.message;
        if (this.responseMessage != null)
            return this.responseMessage.getMessage();
        return null;
    }

    public String getCode() {
        if(this.code != null)
            return this.code;
        if (this.responseMessage != null)
            return this.responseMessage.getCode();
        return null;
    }

    public Object getPayload() {
        return payload;
    }
}
