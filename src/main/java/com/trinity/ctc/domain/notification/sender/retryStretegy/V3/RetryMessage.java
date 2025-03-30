package com.trinity.ctc.domain.notification.sender.retryStretegy.V3;

import com.trinity.ctc.domain.notification.message.FcmMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


@Getter
@AllArgsConstructor
public class RetryMessage implements Delayed {

    private final FcmMessage fcmMessage;
    private final int retryCount;
    private final long startTimeMillis;

    @Override
    public long getDelay(TimeUnit unit) {
        long delay = startTimeMillis - System.currentTimeMillis();
        return unit.convert(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
    }
}
