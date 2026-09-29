package edu.umich.ropierce.zoomreporter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class ExcelPoiWrapper {
    private DateTimeFormatter tabDateFormat = DateTimeFormatter.ofPattern("LLL d, yyyy");
    private Workbook workbook = new XSSFWorkbook();
    private String workbookFile = "Attendance.xlsx";

    public void addSheet(ZoomMeeting zoomMeeting) throws IOException {
        String tabName = zoomMeeting.zoomMeetingSummary().startTime().format(tabDateFormat);
        Sheet sheet = workbook.createSheet(tabName);
        workbook.createFont();

        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle cellStyle = workbook.createCellStyle();
        short format = createHelper.createDataFormat().getFormat("m/d/yy h:mm");
        cellStyle.setDataFormat(format);

        addSummaryRows(zoomMeeting, sheet, cellStyle);
        addParticipantRows(zoomMeeting.zoomMeetingParticipantList(), sheet, cellStyle);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short)12);
        style.setFont(font);
        for (int i = 0;i < 7; i++) {
            sheet.autoSizeColumn(i);
            sheet.setDefaultColumnStyle(i,style);

        }

//        sheet.getRow(0).getCell(0).setCellStyle(style);
    }

    private void addParticipantRows(List<ZoomMeetingParticipant> zoomMeetingParticipants, Sheet sheet, CellStyle cellStyle) {
        sheet.createRow(sheet.getLastRowNum()+1);
        Row columnHeaderRow = sheet.createRow(sheet.getLastRowNum()+1);

        List<String> headerTextList = Arrays.asList("Name (Original Name)","User Email","Total Duration (Minutes)","Guest");
        for (String headerText : headerTextList){
            Cell cell = columnHeaderRow.createCell(columnHeaderRow.getLastCellNum()==-1?columnHeaderRow.getLastCellNum() + 1: columnHeaderRow.getLastCellNum());

            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short)12);
            style.setFont(font);
            cell.setCellStyle(style);
            cell.setCellValue(headerText);

        }
        for (ZoomMeetingParticipant zoomMeetingParticipant : zoomMeetingParticipants){
            Row headerValueRow = sheet.createRow(sheet.getLastRowNum()+1);
            Cell cell1 = headerValueRow.createCell(0);
            cell1.setCellValue(zoomMeetingParticipant.name());
            Cell cell2 = headerValueRow.createCell(1);
            cell2.setCellValue(zoomMeetingParticipant.email());
            Cell cell3 = headerValueRow.createCell(2);
            cell3.setCellValue(zoomMeetingParticipant.duration());
            Cell cell4 = headerValueRow.createCell(3);
            cell4.setCellValue(zoomMeetingParticipant.guest()?"Yes":"No");
        }
    }

    private void addSummaryRows(ZoomMeeting zoomMeeting, Sheet sheet, CellStyle cellStyle) {
        Row headerRow = sheet.createRow(0);

        List<String> headerTextList = Arrays.asList("Meeting ID", "Topic", "Start Time", "End Time", "User Email", "Duration (Minutes)", "Participants");
        for (String headerText : headerTextList){
            Cell cell = headerRow.createCell(headerRow.getLastCellNum()==-1?headerRow.getLastCellNum() + 1: headerRow.getLastCellNum());
            cell.setCellValue(headerText);
        }
        Row headerValueRow = sheet.createRow(1);
        Cell cell1 = headerValueRow.createCell(0);
        cell1.setCellValue(zoomMeeting.zoomMeetingSummary().meetingId());

        Cell cell2 = headerValueRow.createCell(headerValueRow.getLastCellNum());
        cell2.setCellValue(zoomMeeting.zoomMeetingSummary().topic());

        Cell cell3 = headerValueRow.createCell(headerValueRow.getLastCellNum());
        cell3.setCellStyle(cellStyle);
        cell3.setCellValue(zoomMeeting.zoomMeetingSummary().startTime().toLocalDateTime());

        Cell cell4 = headerValueRow.createCell(headerValueRow.getLastCellNum());
        cell4.setCellStyle(cellStyle);
        cell4.setCellValue(zoomMeeting.zoomMeetingSummary().endTime().toLocalDateTime());

        Cell cell5 = headerValueRow.createCell(headerValueRow.getLastCellNum());
        cell5.setCellValue(zoomMeeting.zoomMeetingSummary().userEmail());

        Cell cell6 = headerValueRow.createCell(headerValueRow.getLastCellNum());
        cell6.setCellValue(zoomMeeting.zoomMeetingSummary().duration());

        Cell cell7 = headerValueRow.createCell(headerValueRow.getLastCellNum());
        cell7.setCellValue(zoomMeeting.zoomMeetingSummary().participants());
    }

    public void close() throws IOException {
        FileOutputStream out = new FileOutputStream(workbookFile);
        workbook.write(out);
        out.close();

    }
}
