package edu.umich.ropierce.zoomreporter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Arrays.stream;

// Define a class that reads the CSV file and creates a list of ZoomMeetingSummary objects

public class ZoomCsvReader {

    private static final Logger log = LoggerFactory.getLogger(ZoomCsvReader.class);

    // Define a constant for the CSV file name
    private static final String FILE_NAME = "zoom.csv";


    // Define a method that returns a list of ZoomMeetingSummary objects from the CSV file
    public ZoomMeeting readCSV(File file) {

        // Create a try-with-resources block to handle the file input stream
        String line = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {


            int count = 1;
            Optional<ZoomMeetingSummary> zoomMeetingSummaryOptional = Optional.empty();
            List<ZoomMeetingParticipant> zoomMeetingParticipantList = new ArrayList<>();

            boolean showUniqueUsers = true;
            while ((line = br.readLine()) != null) {
                switch (count++) {
                    case 1 -> showUniqueUsers = true;
                    case 2 -> zoomMeetingSummaryOptional = Optional.of(getZoomMeetingSummary(line));
                    case 3 -> {}
                    case 4 -> {
                        showUniqueUsers = !line.contains("Join Time,Leave Time,");
                    }
                    default -> zoomMeetingParticipantList.add(buildZoomParticipant(line, showUniqueUsers));
                };
            }

            ZoomMeeting zoomMeeting = new ZoomMeeting(zoomMeetingSummaryOptional.get(),zoomMeetingParticipantList);
            log.info("ZoomMeeting: {}", zoomMeeting);
            return zoomMeeting;
        } catch (IOException|NumberFormatException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            log.error("File: " + file.getAbsolutePath() + ",\nline: " + line);
            throw new ZoomReporterException(e);
        }
    }

    private ZoomMeetingParticipant buildZoomParticipant(String line, boolean showUniqueUsers) {
        String output = line.replaceAll("(?<=\")([^\"]*)(,)(.*?)(?=\")", "$1" + '\u0001' + "$3");
        List<String> participantFieldList = Arrays.asList(output.split(","));
        participantFieldList = participantFieldList.stream()
                .map(a->a.replace('\u0001',','))
                .toList();
        String name = participantFieldList.get(0);
        String email = participantFieldList.get(1);
        int idx = (showUniqueUsers ? 0: 2);
        int duration = Integer.parseInt(participantFieldList.get(2 + idx));
        boolean guest = Boolean.valueOf(participantFieldList.get(3 + idx));

        return new ZoomMeetingParticipant(name,email,duration,guest);
    }

    private ZoomMeetingSummary getZoomMeetingSummary(String line) throws IOException {
        // Create a new ZoomMeetingSummary object from the array and add it to the list
        String[] data = line.split(",");
        DateTimeFormatter csvDateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");

        ZoomMeetingSummary zoomMeetingSummary = new ZoomMeetingSummary(
                parseLong(data[1]),
                data[0],
                ZonedDateTime.of(LocalDateTime.parse(data[4].replace("\"",""),csvDateFormat), ZoneId.systemDefault()),
                ZonedDateTime.of(LocalDateTime.parse(data[5].replace("\"",""),csvDateFormat), ZoneId.systemDefault()),
                data[2],
                parseInt(data[3]),
                parseInt(data[6]));

        return zoomMeetingSummary;
    }

    public void doConversion() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("zoom_files");
        assert url != null;
        String path = url.getPath();
        File[] zoomFileList = new File(path).listFiles();
        log.info("path:" + Arrays.toString(zoomFileList));
        assert zoomFileList != null;
        ExcelPoiWrapper excelPoiWrapper = new ExcelPoiWrapper();

        List<ZoomMeeting> zoomMeetingList = new ArrayList<>();

        stream(zoomFileList)
            .sequential()
            .forEach(file->{
                zoomMeetingList.add(readCSV(file));
            });

        zoomMeetingList.sort(Comparator.comparing((ZoomMeeting a) -> a.zoomMeetingSummary().startTime()));

        zoomMeetingList
            .forEach(zoomMeeting->{
                try {
                    excelPoiWrapper.addSheet(zoomMeeting);
                } catch (IOException e) {
                    throw new ZoomReporterException(e);
                }
            });

        excelPoiWrapper.close();
    }

    // Define a main method to test the code
    public static void main(String[] args) throws IOException {
        new ZoomCsvReader().doConversion();
        // output file is Attendance.xlsx located in this project root dir
    }
}
