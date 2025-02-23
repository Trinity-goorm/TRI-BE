package com.trinity.ctc.domain.notification.dto;

import com.google.firebase.messaging.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GroupFcmInformationDto {
    List<FcmMessageDto> messageDtoList;
    List<Message> messageList;
}
