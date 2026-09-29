package edu.umich.ropierce.zoomreporter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public record ZoomMeetingSummary(
    long meetingId,
    String topic,
    ZonedDateTime startTime,
    ZonedDateTime endTime,
    String userEmail,
    int duration,
    int participants
){}
