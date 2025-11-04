package com.api.app.entity.codebase;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeBaseDto {
    private String groupCode;
    private String groupCodeName;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
