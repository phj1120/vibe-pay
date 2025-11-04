package com.api.app.entity.codedetail;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeDetailDto {
    private String groupCode;
    private String code;
    private String codeName;
    private String referenceValue1;
    private String referenceValue2;
    private Long displaySequence;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
