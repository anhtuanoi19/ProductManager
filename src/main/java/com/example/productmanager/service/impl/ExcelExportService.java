package com.example.productmanager.service.impl;

import com.example.productmanager.entity.Category;
import com.example.productmanager.entity.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ExcelExportService {

    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss"; // Định dạng ngày tháng giờ
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createBorderedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat(DATE_FORMAT));
        return style;
    }

    public ByteArrayInputStream exportCategoriesToExcel(List<Category> categories) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Categories");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle borderedStyle = createBorderedStyle(workbook);

            // Tạo tiêu đề cho bảng Excel
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "ID", headerStyle);
            createCell(headerRow, 1, "Name", headerStyle);
            createCell(headerRow, 2, "Created Date", headerStyle);
            createCell(headerRow, 3, "Modified Date", headerStyle);
            createCell(headerRow, 4, "Created By", headerStyle);
            createCell(headerRow, 5, "Modified By", headerStyle);
            createCell(headerRow, 6, "Description", headerStyle);
            createCell(headerRow, 7, "Category Code", headerStyle);
            createCell(headerRow, 8, "Status", headerStyle);

            // Điền dữ liệu vào bảng
            int rowIdx = 1;
            for (Category category : categories) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, category.getId().toString(), borderedStyle);
                createCell(row, 1, category.getName(), borderedStyle);
                createCell(row, 2, category.getCreatedDate() != null ? sdf.format(category.getCreatedDate()) : "", dateStyle);
                createCell(row, 3, category.getModifiedDate() != null ? sdf.format(category.getModifiedDate()) : "", dateStyle);
                createCell(row, 4, category.getCreatedBy() != null ? category.getCreatedBy() : "", borderedStyle);
                createCell(row, 5, category.getModifiedBy() != null ? category.getModifiedBy() : "", borderedStyle);
                createCell(row, 6, category.getDescription() != null ? category.getDescription() : "", borderedStyle);
                createCell(row, 7, category.getCategoryCode() != null ? category.getCategoryCode() : "", borderedStyle);
                createCell(row, 8, category.getStatus() != null ? category.getStatus() : "", borderedStyle);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportProductsToExcel(List<Product> products) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle borderedStyle = createBorderedStyle(workbook);

            // Tạo tiêu đề cho bảng Excel
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "ID", headerStyle);
            createCell(headerRow, 1, "Name", headerStyle);
            createCell(headerRow, 2, "Description", headerStyle);
            createCell(headerRow, 3, "Price", headerStyle);
            createCell(headerRow, 4, "Product Code", headerStyle);
            createCell(headerRow, 5, "Quantity", headerStyle);
            createCell(headerRow, 6, "Status", headerStyle);
            createCell(headerRow, 7, "Created Date", headerStyle);
            createCell(headerRow, 8, "Modified Date", headerStyle);
            createCell(headerRow, 9, "Created By", headerStyle);
            createCell(headerRow, 10, "Modified By", headerStyle);
            createCell(headerRow, 11, "Categories", headerStyle);

            // Điền dữ liệu vào bảng
            int rowIdx = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowIdx++);
                StringBuilder nameCategories = new StringBuilder();
                product.getProductCategories().stream().forEach(productCategory -> nameCategories.append(productCategory.getCategory().getName()).append(", "));

                createCell(row, 0, product.getId().toString(), borderedStyle);
                createCell(row, 1, product.getName(), borderedStyle);
                createCell(row, 2, product.getDescription() != null ? product.getDescription() : "", borderedStyle);
                createCell(row, 3, product.getPrice() != null ? product.getPrice() : 0, borderedStyle);
                createCell(row, 4, product.getProductCode() != null ? product.getProductCode() : "", borderedStyle);
                createCell(row, 5, product.getQuantity() != null ? product.getQuantity() : 0, borderedStyle);
                createCell(row, 6, product.getStatus() != null ? product.getStatus() : "", borderedStyle);
                createCell(row, 7, product.getCreatedDate() != null ? sdf.format(product.getCreatedDate()) : "", dateStyle);
                createCell(row, 8, product.getModifiedDate() != null ? sdf.format(product.getModifiedDate()) : "", dateStyle);
                createCell(row, 9, product.getCreatedBy() != null ? product.getCreatedBy() : "", borderedStyle);
                createCell(row, 10, product.getModifiedBy() != null ? product.getModifiedBy() : "", borderedStyle);
                createCell(row, 11, nameCategories.toString(), borderedStyle);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}