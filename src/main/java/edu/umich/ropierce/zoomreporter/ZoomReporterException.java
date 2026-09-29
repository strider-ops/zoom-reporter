package edu.umich.ropierce.zoomreporter;

public class ZoomReporterException extends RuntimeException {
    public ZoomReporterException(String noRosterFound) {
        super(noRosterFound);
    }
    public ZoomReporterException(Exception exception) {
        super(exception);
    }
}
