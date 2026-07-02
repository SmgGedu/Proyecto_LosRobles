package com.losrobles.api.services;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.losrobles.api.models.RegistroAcceso;
import com.losrobles.api.repositories.RegistroAccesoRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera los reportes de Auditoría (registro de accesos) en Excel y PDF
 * para el Panel de Administrador.
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final RegistroAccesoRepository registroRepo;

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color VERDE_LOS_ROBLES = new Color(45, 90, 39);

    private static final int FILA_HEADER_EXCEL = 3;

    @Transactional(readOnly = true)
    public byte[] generarExcel(LocalDateTime desde, LocalDateTime hasta, String categoria) throws IOException {
        List<RegistroAcceso> registros = obtenerRegistros(desde, hasta, categoria);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Registro de Accesos");
            DefaultIndexedColorMap colorMap = new DefaultIndexedColorMap();

            String[] columnas = {
                    "Visitante", "DNI", "Departamento", "Anfitrión", "Conserje",
                    "Tipo Ingreso", "Tipo Visita", "Hora Entrada", "Hora Salida", "Estado", "Placa"
            };
            int ultimaColumna = columnas.length - 1;

            agregarLogo(workbook, sheet);

            XSSFFont tituloFont = workbook.createFont();
            tituloFont.setBold(true);
            tituloFont.setFontHeightInPoints((short) 14);
            tituloFont.setColor(new XSSFColor(VERDE_LOS_ROBLES, colorMap));
            CellStyle tituloStyle = workbook.createCellStyle();
            tituloStyle.setFont(tituloFont);
            tituloStyle.setAlignment(HorizontalAlignment.CENTER);
            tituloStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Row tituloRow = sheet.createRow(0);
            tituloRow.setHeightInPoints(36f);
            Cell tituloCell = tituloRow.createCell(1);
            tituloCell.setCellValue("Reporte de Registro de Accesos - Condominio Los Robles");
            tituloCell.setCellStyle(tituloStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, ultimaColumna));

            XSSFFont subtituloFont = workbook.createFont();
            subtituloFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            CellStyle subtituloStyle = workbook.createCellStyle();
            subtituloStyle.setFont(subtituloFont);
            subtituloStyle.setAlignment(HorizontalAlignment.CENTER);

            Row subtituloRow = sheet.createRow(1);
            Cell subtituloCell = subtituloRow.createCell(1);
            subtituloCell.setCellValue("Periodo: " + desde.format(FECHA_HORA) + "  -  " + hasta.format(FECHA_HORA)
                    + "    |    Categoría: " + categoriaLabel(categoria));
            subtituloCell.setCellStyle(subtituloStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, ultimaColumna));

            sheet.createRow(2);

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(VERDE_LOS_ROBLES, colorMap));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(FILA_HEADER_EXCEL);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = FILA_HEADER_EXCEL + 1;
            for (RegistroAcceso r : registros) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nombreVisitante(r));
                row.createCell(1).setCellValue(r.getVisitante().getDni());
                row.createCell(2).setCellValue(formatDepartamento(r));
                row.createCell(3).setCellValue(nombreAnfitrion(r));
                row.createCell(4).setCellValue(r.getConserjeEnTurno().getNombres());
                row.createCell(5).setCellValue(r.getTipoIngreso());
                row.createCell(6).setCellValue(r.getTipoVisita() != null ? r.getTipoVisita() : "-");
                row.createCell(7).setCellValue(r.getHoraIngreso() != null ? r.getHoraIngreso().format(FECHA_HORA) : "-");
                row.createCell(8).setCellValue(r.getHoraSalida() != null ? r.getHoraSalida().format(FECHA_HORA) : "-");
                row.createCell(9).setCellValue(r.getEstadoAcceso());
                row.createCell(10).setCellValue(r.getPlacaVehiculo() != null ? r.getPlacaVehiculo() : "");
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(LocalDateTime desde, LocalDateTime hasta, String categoria) throws DocumentException {
        List<RegistroAcceso> registros = obtenerRegistros(desde, hasta, categoria);

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Font tituloFont = new Font(Font.HELVETICA, 16, Font.BOLD, VERDE_LOS_ROBLES);
        Font subtituloFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

        Paragraph titulo = new Paragraph("Reporte de Registro de Accesos - Condominio Los Robles", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph periodo = new Paragraph(
                "Periodo: " + desde.format(FECHA_HORA) + "  -  " + hasta.format(FECHA_HORA)
                        + "    |    Categoría: " + categoriaLabel(categoria),
                subtituloFont);
        periodo.setAlignment(Element.ALIGN_CENTER);
        periodo.setSpacingAfter(15f);
        document.add(periodo);

        String[] columnas = { "Visitante", "DNI", "Depto", "Anfitrión", "Conserje", "Tipo Ingreso", "Tipo Visita",
                "Entrada", "Salida", "Estado" };
        PdfPTable table = new PdfPTable(columnas.length);
        table.setWidthPercentage(100);

        for (String col : columnas) {
            PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
            cell.setBackgroundColor(VERDE_LOS_ROBLES);
            cell.setPadding(5f);
            table.addCell(cell);
        }

        if (registros.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No se encontraron registros para el periodo seleccionado.", cellFont));
            empty.setColspan(columnas.length);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(10f);
            table.addCell(empty);
        } else {
            for (RegistroAcceso r : registros) {
                table.addCell(new Phrase(nombreVisitante(r), cellFont));
                table.addCell(new Phrase(r.getVisitante().getDni(), cellFont));
                table.addCell(new Phrase(formatDepartamento(r), cellFont));
                table.addCell(new Phrase(nombreAnfitrion(r), cellFont));
                table.addCell(new Phrase(r.getConserjeEnTurno().getNombres(), cellFont));
                table.addCell(new Phrase(r.getTipoIngreso(), cellFont));
                table.addCell(new Phrase(r.getTipoVisita() != null ? r.getTipoVisita() : "-", cellFont));
                table.addCell(new Phrase(r.getHoraIngreso() != null ? r.getHoraIngreso().format(FECHA_HORA) : "-", cellFont));
                table.addCell(new Phrase(r.getHoraSalida() != null ? r.getHoraSalida().format(FECHA_HORA) : "-", cellFont));
                table.addCell(new Phrase(r.getEstadoAcceso(), cellFont));
            }
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    private List<RegistroAcceso> obtenerRegistros(LocalDateTime desde, LocalDateTime hasta, String categoria) {
        String tipoVisita = (categoria == null || categoria.isBlank() || "TODOS".equalsIgnoreCase(categoria))
                ? null
                : categoria.toUpperCase();
        return registroRepo.findForReporte(desde, hasta, tipoVisita);
    }

    private String nombreVisitante(RegistroAcceso r) {
        return r.getVisitante().getNombre() + " " + r.getVisitante().getApellidos();
    }

    private String nombreAnfitrion(RegistroAcceso r) {
        return r.getResidenteQueAutoriza() != null
                ? r.getResidenteQueAutoriza().getNombres() + " " + r.getResidenteQueAutoriza().getApellidos()
                : "INGRESO MANUAL";
    }

    private String formatDepartamento(RegistroAcceso r) {
        return r.getDepartamentoDestino() != null
                ? r.getDepartamentoDestino().getBloqueTorre() + "-" + r.getDepartamentoDestino().getNumeroDepa()
                : "N/A";
    }

    private String categoriaLabel(String categoria) {
        return (categoria == null || categoria.isBlank() || "TODOS".equalsIgnoreCase(categoria))
                ? "Todos los registros"
                : categoria;
    }

    private void agregarLogo(XSSFWorkbook workbook, Sheet sheet) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/reportes/logo.png")) {
            if (is == null) {
                return;
            }
            byte[] logoBytes = is.readAllBytes();
            int pictureIdx = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);

            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);

            Drawing<?> drawing = sheet.createDrawingPatriarch();
            Picture picture = drawing.createPicture(anchor, pictureIdx);
            picture.resize();
        }
    }
}
