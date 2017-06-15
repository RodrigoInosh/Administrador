package cl.techk.models;

import org.apache.poi.ss.usermodel.Cell;
import org.json.JSONArray;

public class ValidateAppendant extends ValidateDictionary {

    public static final int COLUMN_COUNT = 3;
    public static final int DATE_INDEX_COLUMN = 2; 

    public static boolean validateCellData(int index_column, int index_row, Cell xlsx_cell, JSONArray array_row_data,
            JSONArray errors_on_data) {

        boolean valid_cell = false;

        if (index_column == DATE_INDEX_COLUMN) {
            valid_cell = validateDateFormat(xlsx_cell, array_row_data, errors_on_data);
        } else if (index_column != DATE_INDEX_COLUMN) {
            valid_cell = validateEmptyCell(index_column, index_row, xlsx_cell, array_row_data, errors_on_data);
        }
        
        return valid_cell;
    }
}