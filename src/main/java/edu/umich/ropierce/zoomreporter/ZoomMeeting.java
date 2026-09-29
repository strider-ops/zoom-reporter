package edu.umich.ropierce.zoomreporter;

import java.util.List;

public record ZoomMeeting(
    ZoomMeetingSummary zoomMeetingSummary,
    List<ZoomMeetingParticipant> zoomMeetingParticipantList
){}
