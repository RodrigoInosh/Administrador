package cl.techk.models;

import org.apache.poi.ss.usermodel.Cell;
import org.json.JSONArray;

public class ValidateSearch2 extends ValidateDictionary {

    public static final int COLUMN_COUNT = 5;
    public static final int DATE_COLUMN_INDEX = 4;
    public static final int RUBRO_COLUMN_INDEX = 3;
    public static final int KEYWORD_COLUMN_INDEX = 1;

    public static boolean validateCellData(int index_column, int index_row, String market, Cell xlsx_cell,
            JSONArray array_row_data, JSONArray errors_on_data) {

        boolean valid_cell = false;
        valid_cell = validateCell(index_column, index_row, market, xlsx_cell, array_row_data, errors_on_data);
        
        return valid_cell;
    }

    public static boolean validateCell(int index_column, int index_row, String market, Cell xlsx_cell,
            JSONArray array_row_data, JSONArray errors_on_data) {

        boolean validate = false;

        if (index_column == DATE_COLUMN_INDEX) {
            validate = validateDateFormat(xlsx_cell, array_row_data, errors_on_data);
        } else if (index_column == KEYWORD_COLUMN_INDEX) {
            validate = validateKeywordColumn(xlsx_cell, array_row_data);
        } else if (index_column == RUBRO_COLUMN_INDEX) {
            validate = validateRubroExist(index_row, market, xlsx_cell, array_row_data, errors_on_data);
        } else if (index_column != DATE_COLUMN_INDEX) {
            validate = validateEmptyCell(index_column, index_row, xlsx_cell, array_row_data, errors_on_data);
        }
        
        return validate;
    }
}
