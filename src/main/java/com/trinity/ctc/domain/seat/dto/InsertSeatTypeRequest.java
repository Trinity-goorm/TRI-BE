package com.trinity.ctc.domain.seat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "초기 예약좌석 데이터 삽입", example = """
    [
      { "minCapacity": 1, "maxCapacity": 2 },
      { "minCapacity": 3, "maxCapacity": 4 },
      { "minCapacity": 5, "maxCapacity": 6 }
    ]
""")
public class InsertSeatTypeRequest {

    @Schema(description = "최소 인원 수", example = "1")
    private int minCapacity;

    @Schema(description = "최대 인원 수", example = "2")
    private int maxCapacity;
}
