package cl.techk.models;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONObject;
import com.monitorjbl.xlsx.StreamingReader;
import cl.techk.ext.utils.Utils;
import cl.techk.lib.ExcelUtils;

public class ChargeSearch2 {

    public static JSONObject charge(String upload_file_directory, int market_id) {

        JSONObject response = new JSONObject();
        JSONArray errors = new JSONArray();
        
        try {
            Utils.print("charging search2");
            StreamingReader excel_reader = ExcelUtils.getFileStreamingReader(upload_file_directory, "B2");
            JSONObject market_data = Dictionary.getMarketPrefix(market_id);
            String market_name = market_data.getString("mercado");
            response = getDataToCharge(market_name, excel_reader);
        } catch (Exception error) {
            errors.put(new JSONArray("[No viene la hoja correspondiente]"));
            response.put("errors", errors);
            response.put("row_data", new JSONArray());
        }
        
        return response;
    }

    public static JSONObject getDataToCharge(String market_name, StreamingReader excel_reader) {

        int row_index = 0;
        JSONArray rows_data = new JSONArray();
        JSONArray errors_data = new JSONArray();
        JSONObject response = new JSONObject();

        for (Row xlsx_row : excel_reader) {
            
            if (row_index == 0) {
                row_index++;
                continue;
            }

            JSONObject readed_data = getReadedExcelRow(market_name, xlsx_row);
            errors_data.put(readed_data.get("errors"));
            
            if (readed_data.getJSONArray("row_data").length() > 0) {
                rows_data.put(readed_data.get("row_data"));
            } else {
                rows_data = new JSONArray();
                break;
            }
            
            row_index++;
        }
        
        response.put("row_data", rows_data);
        response.put("errors", errors_data);
        
        return response;
    }

    public static JSONObject getReadedExcelRow(String market_name, Row xlsx_row) {

        JSONObject response = new JSONObject();
        JSONArray array_row_data = new JSONArray();
        JSONArray errors_on_data = new JSONArray();
        int row_index = xlsx_row.getRowNum();
        
        for (int index_column = 0; index_column < ValidateSearch2.COLUMN_COUNT; index_column++) {
            
            Cell xlsx_cell = xlsx_row.getCell(index_column);
            
            if (!ValidateSearch2.validateCellData(index_column, row_index, market_name, xlsx_cell, array_row_data,
                    errors_on_data)) {
                array_row_data = new JSONArray();
                break;
            }
        }
        
        response.put("errors", errors_on_data);
        response.put("row_data", array_row_data);
        
        return response;
    }
}
