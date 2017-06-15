package cl.techk.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.json.JSONArray;
import cl.techk.ext.utils.Utils;

public class ValidateDictionary {

    private static final String DATETIME_REGEX = "\\d{4}\\b(-|/)\\d{2}\\b(-|/)\\d{2}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}";
    private static final String DATE_REGEX = "\\d{2}\\b(-|/)\\d{2}\\b(-|/)\\d{4}";

    public static boolean isEmpty(Cell xlsx_cell) {

        boolean is_empty = false;

        try {
            if (xlsx_cell.getStringCellValue() == null || "".equals(xlsx_cell.getStringCellValue())) {
                is_empty = true;
            }
        } catch (Exception error) {
            is_empty = true;
        }

        return is_empty;
    }

    public static boolean validateDateFormat(Cell xlsx_cell, JSONArray array_row_data, JSONArray errors_on_data) {

        boolean correct_format = false;
        SimpleDateFormat english_date = new SimpleDateFormat("yyyy-MM-dd");
        
        try {
            String cell_data = xlsx_cell.getStringCellValue();
            String formatted_date = english_date.format(new SimpleDateFormat("dd/MM/yyyy").parse(cell_data));

            if ("".equals(cell_data)) {
                Utils.putDataOnArray("0000-00-00", array_row_data);
                correct_format = true;
            } else if (cell_data.matches(DATETIME_REGEX) || cell_data.matches(DATE_REGEX)) {
                Utils.putDataOnArray(formatted_date, array_row_data);
                correct_format = true;
            } else {
                Utils.putDataOnArray(
                        "Error formato de Fecha en fila: " + (xlsx_cell.getRowIndex() + 1) + ". Formato correcto > yyyy-MM-dd",
                        errors_on_data);
            }
        } catch (NullPointerException error) {
            correct_format = true;
            Utils.putDataOnArray("0000-00-00", array_row_data);
            Utils.print("Celda de Fecha Nula");
        } catch (ParseException e) {
            correct_format = true;
            Utils.putDataOnArray("0000-00-00", array_row_data);
        }

        return correct_format;
    }

    public static boolean validateKeywordColumn(Cell xlsx_cell, JSONArray array_row_data) {

        boolean correct_format = true;

        try {
            String cell_data = xlsx_cell.getStringCellValue();
            Utils.putDataOnArray(cell_data, array_row_data);
        } catch (NullPointerException error) {
            Utils.putDataOnArray("", array_row_data);
            Utils.print("Celda de Keyword Nula");
        }

        return correct_format;
    }

    public static boolean validateEmptyCell(int index_column, int index_row, Cell xlsx_cell, JSONArray array_row_data,
            JSONArray errors_on_data) {

        boolean validate = false;

        if (!isEmpty(xlsx_cell)) {
            Utils.putDataOnArray(xlsx_cell.getStringCellValue(), array_row_data);
            validate = true;
        } else {
            Utils.putDataOnArray("celda vacía en fila: " + (index_row + 1) + " - columna:" + (index_column + 1),
                    errors_on_data);
        }

        return validate;
    }

    public static boolean validateNullCell(int index_column, int index_row, Cell xlsx_cell, JSONArray array_row_data,
            JSONArray errors_on_data) {

        boolean validate = false;

        try {
            xlsx_cell.getStringCellValue();
            validate = true;
        } catch (NullPointerException null_error) {
            Utils.putDataOnArray("celda vacía en fila: " + (index_row + 1) + " - columna:" + (index_column + 1),
                    errors_on_data);
        }

        return validate;
    }

    public static boolean validateRubroExist(int index_row, String market, Cell cell_rubro, JSONArray array_row_data,
            JSONArray errors_on_data) {

        boolean exists = false;

        try {
            String rubro = cell_rubro.getStringCellValue();

            if ("".equals(rubro)) {
                Utils.putDataOnArray("Rubro en fila:" + (index_row + 1) + " está vacío", errors_on_data);
            } else if (Dictionary.rubroExists(market, rubro) > 0) {
                Utils.putDataOnArray(rubro, array_row_data);
                exists = true;
            } else {
                Utils.putDataOnArray("Rubro en fila:" + (index_row + 1) + " no existe", errors_on_data);
            }
        } catch (NullPointerException error) {
            Utils.putDataOnArray("Rubro en fila:" + (index_row + 1) + " está vacío", errors_on_data);
            Utils.print("Celda de Rubro Nula");
        }

        return exists;
    }
}
