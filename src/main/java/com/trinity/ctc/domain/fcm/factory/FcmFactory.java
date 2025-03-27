package com.trinity.ctc.domain.fcm.factory;

import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.trinity.ctc.global.util.formatter.DateTimeUtil.convertToLocalDateTime;

@Component
@RequiredArgsConstructor
public class FcmFactory {

    private final UserRepository userRepository;

    public List<Fcm> createFcmListByCsv(List<Map<String, String>> fcmData) {
        List<Fcm> fcmList = new ArrayList<>();
        for (Map<String, String> row : fcmData) {
            Fcm fcm = Fcm.builder()
                    .token(row.get("token"))
                    .registeredAt(convertToLocalDateTime(row.get("registered_at")))
                    .expiresAt(convertToLocalDateTime(row.get("expires_at")))
                    .user(userRepository.findById(Long.parseLong(row.get("receiver_id"))).orElse(null))
                    .build();

            fcmList.add(fcm);
        }
        return fcmList;
    }
}
