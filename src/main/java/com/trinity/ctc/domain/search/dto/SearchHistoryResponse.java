package com.trinity.ctc.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "검색 히스토리 조회 응답")
public class SearchHistoryResponse {

    @Schema(description = "검색 히스토리 ID", example = "1")
    private Long id;

    @Schema(description = "검색 키워드", example = "일식집")
    private String keyword;
}
