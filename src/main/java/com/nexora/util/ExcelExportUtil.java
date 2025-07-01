package com.nexora.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;

public class ExcelExportUtil {

    public static <T> byte[] exportToExcel(List<T> data, String sheetName) throws Exception {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("No data to export");
        }
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            Field[] fields = data.get(0).getClass().getDeclaredFields();

            // Header row
            Row header = sheet.createRow(0);
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                header.createCell(i).setCellValue(fields[i].getName());
            }

            // Data rows
            int rowIdx = 1;
            for (T obj : data) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < fields.length; i++) {
                    Object value = fields[i].get(obj);
                    row.createCell(i).setCellValue(value != null ? value.toString() : "");
                }
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }
}