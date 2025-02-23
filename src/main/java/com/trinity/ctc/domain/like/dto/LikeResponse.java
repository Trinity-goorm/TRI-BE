package com.trinity.ctc.domain.like.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "찜하기 성공 응답 객체")
public class LikeResponse {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지", example = "찜하기 완료")
    private String message;
}
