package edu.umich.ropierce.zoomreporter;

public record ZoomMeetingParticipant(
        String name,
        String email,
        int duration,
        boolean guest
) {
}
