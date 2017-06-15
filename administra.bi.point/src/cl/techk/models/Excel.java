package cl.techk.models;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import com.monitorjbl.xlsx.StreamingReader;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;
import cl.techk.lib.ExcelUtils;
import cl.techk.lib.ExcelUtils.FormatType;
import cl.techk.mail.rest.Mail;
import cl.techk.validations.OndemandColumnsToValidate;

public class Excel {
    
    private static final String DATETIME_REGEX = "\\d{4}-\\d{2}-\\d{2}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}";
    private static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}";
    private static String type_clasification;

    public static boolean getValidationInsertDataOndemand(StreamingReader reader, String table_to_insert, String type_clasification_file, 
            StringBuffer error_mail_message) {

        StringBuffer query_insert_data = new StringBuffer("INSERT INTO " + table_to_insert + "(");
        OndemandColumnsToValidate columns_to_validate;
        JSONObject readed_data_validations = new JSONObject();
        type_clasification = type_clasification_file.toUpperCase();
        int row_index = 0;
        int count_empty_rows = 0;
        int file_count_columns = 0;
        int position_in_header_column_to_validate = -1;
        boolean correct_inserted_data = false;
        Connection connection = null;
        PreparedStatement statement_insert_data = null;

        try {
            connection = DBCnx.conexion();
            columns_to_validate = getColumnsToValidate(type_clasification_file);

            for (Row xlsx_row : reader) {
                readed_data_validations = readRowData(row_index, xlsx_row, statement_insert_data, connection,
                        query_insert_data, columns_to_validate, error_mail_message, file_count_columns,
                        position_in_header_column_to_validate);

                if (row_index == 0) {

                    file_count_columns = readed_data_validations.getInt("file_count_columns");
                    position_in_header_column_to_validate = readed_data_validations.getInt("column_to_validate_header_position");
                    statement_insert_data = connection.prepareStatement(query_insert_data.toString());
                } else if(!readed_data_validations.getBoolean("data_row_ok")){
                    correct_inserted_data = false;
                    count_empty_rows++;
                    break;
                } else {
                    correct_inserted_data = true;
                    executeStatementBatch(count_empty_rows, statement_insert_data);
                    count_empty_rows = 0;
                }
                row_index++;
            }
            statement_insert_data.executeBatch();
            isDataCorrectInserted(correct_inserted_data, count_empty_rows, error_mail_message, table_to_insert);
        } catch (Exception e) {
            correct_inserted_data = false;
            e.printStackTrace();
            prepareErrorProcessMail("En fila "+(row_index+1)+" Error --> "+e.getMessage() + ". "+ Mail.getClasificationSQLErrorMessage(e.getMessage()), 
                    table_to_insert);
        } finally {
            DBCnx.close(statement_insert_data);
            DBCnx.close(connection);
        }
        return correct_inserted_data;
    }
    
    public static void executeStatementBatch(int count_batch_records, PreparedStatement statement_insert_data){
        try {
            if(count_batch_records > 200) {
                statement_insert_data.executeBatch();
            }
        } catch (Exception error){
            error.printStackTrace();
        }
    }
    
    public static void isDataCorrectInserted(boolean correct_inserted_data, int count_empty_rows, StringBuffer error_mail_message, String table_to_insert){
        if (!correct_inserted_data) {
            String mail_body_message = "<b>Filas Vacías :</b>" + count_empty_rows + "<br>" + error_mail_message.toString();
            prepareErrorProcessMail(mail_body_message, table_to_insert);
        }
    }
    
    public static void prepareErrorProcessMail(String mail_body_message, String table_to_insert) {
        String subject = "[ONDEMAND]["+type_clasification+"] Error carga archivo OnDemand";
        Mail.sendMail(mail_body_message.toString(), subject, null, null, 5, "Tech-K", 20);
        DataManager.deleteData("TRUNCATE " + table_to_insert);
    }

    public static JSONObject readRowData(int row_index, Row xlsx_row, PreparedStatement statement_insert_data,
            Connection connection, StringBuffer query_insert_data, OndemandColumnsToValidate columns_to_validate,
            StringBuffer error_mail_message, int file_count_columns, int index_relevant_column) {

        JSONObject readed_data_validations = new JSONObject();
        try {
            if (row_index == 0) {
                readed_data_validations = processHeaderData(xlsx_row, query_insert_data, columns_to_validate);
            } else {
                JSONObject row_data = processRowsData(file_count_columns, statement_insert_data, xlsx_row,
                        error_mail_message, row_index, columns_to_validate);
                readed_data_validations.put("data_row_ok", row_data.get("validate_relevant_column"));
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return readed_data_validations;
    }

    public static OndemandColumnsToValidate getColumnsToValidate(String type_clasification_file) {
        OndemandColumnsToValidate columns_object = new OndemandColumnsToValidate();

        switch (type_clasification_file) {
        case "ord":
            columns_object.addValueToList("nroOrden", "String");
            columns_object.addValueToList("fechaEnvioDate", "Date");
            columns_object.setRelevant_column("nroOrden");
            break;
        case "lic":
            columns_object.addValueToList("nroLicitacion", "String");
            columns_object.addValueToList("fechaPublicacion", "Date");
            columns_object.addValueToList("fechaCierre", "Date");
            columns_object.addValueToList("fechaAdjudicacion", "Date");
            columns_object.setRelevant_column("nroLicitacion");
            break;
        case "adj":
            columns_object.addValueToList("nroLicitacion", "String");
            columns_object.addValueToList("fechaPublicacion", "Date");
            columns_object.addValueToList("fechaCierre", "Date");
            columns_object.addValueToList("fechaInformada", "Date");
            columns_object.setRelevant_column("nroLicitacion");
            break;
        }

        return columns_object;
    }

    public static JSONObject processHeaderData(Row xlsx_row, StringBuffer query_insert_data,
            OndemandColumnsToValidate columns_to_validate) {

        String insert_query_values = ") VALUES (";
        String insert_columns_names = "";
        JSONObject header_columns_data = new JSONObject();
        int file_count_columns = 0;

        for (Cell xlsx_cell : xlsx_row) {
            String cell_value = xlsx_cell.getStringCellValue();

            setPositionColumnsToValidate(columns_to_validate, cell_value, file_count_columns);
            insert_columns_names += xlsx_cell.getStringCellValue() + ",";
            insert_query_values += "?,";
            file_count_columns++;
        }
        insert_columns_names = Utils.removeLastCharacter(insert_columns_names);
        insert_query_values = Utils.removeLastCharacter(insert_query_values);
        header_columns_data.put("file_count_columns", file_count_columns);
        header_columns_data.put("column_to_validate_header_position", columns_to_validate.getRelevant_column_position());
        query_insert_data.append(insert_columns_names + insert_query_values + ")");

        return header_columns_data;
    }

    public static void setPositionColumnsToValidate(OndemandColumnsToValidate columns_to_validate, String cell_value,
            int file_count_columns) {

        String relevant_column = columns_to_validate.getRelevant_column();
        HashMap<String, String> columns = columns_to_validate.getList_column_to_validate();
        if (columns.containsKey(cell_value)) {
            if (relevant_column.equals(cell_value)) {
                columns_to_validate.setRelevant_column_position(file_count_columns);
            } else {
                columns_to_validate.putColumnPositionOnHash(file_count_columns, cell_value);
            }
        }
    }

    public static JSONObject processRowsData(int file_count_columns, PreparedStatement statement_insert_data,
            Row xlsx_row, StringBuffer error_mail_message, int ciclo, OndemandColumnsToValidate columns_to_validate) {

        JSONObject validation_data = new JSONObject();
        boolean validate_relevant_column = false;
        try {
            for (int index_column = 0; index_column < file_count_columns; index_column++) {

                validate_relevant_column = validateCellData(xlsx_row, index_column, error_mail_message, ciclo, statement_insert_data, columns_to_validate);
                if (!validate_relevant_column) {
                    break;
                }
            }
            if (validate_relevant_column) {
                statement_insert_data.addBatch();
            }
        } catch (Exception error) {
            validate_relevant_column = false;
            Utils.print("Fila:"+ciclo+" Error: "+error.getMessage());
        }
        validation_data.put("validate_relevant_column", validate_relevant_column);
        return validation_data;
    }

    public static boolean validateCellData(Row xlsx_row, int index_column, StringBuffer error_mail_message,
            int row_index, PreparedStatement statement_insert_data, OndemandColumnsToValidate columns_to_validate) {

        Cell xlsx_cell = xlsx_row.getCell(index_column);
        boolean validate_column = true;
        int index_relevant_column = columns_to_validate.getRelevant_column_position();
        try {
            if (isRelevantColumn(index_column, index_relevant_column)) {
                validate_column = addRelevantColumnCellValueToStatement(xlsx_cell, row_index, index_column, error_mail_message, statement_insert_data, 
                        columns_to_validate.getRelevant_column());
            } else if (isColumnToValidateFormat(index_column, columns_to_validate)) {
                String column_name = columns_to_validate.getList_positions_column_to_validate().get(index_column);
                String column_type = columns_to_validate.getList_column_to_validate().get(column_name);
                validate_column = validateFormatColumn(xlsx_cell, row_index, index_column, statement_insert_data, column_name, column_type, error_mail_message);
            } else if (xlsx_cell != null && !"".equals(xlsx_cell.getStringCellValue())) {
                addValueToStatement(statement_insert_data, xlsx_cell.getStringCellValue(), index_column);
            } else {
                addValueToStatement(statement_insert_data, "", index_column);
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
        return validate_column;
    }

    public static boolean isRelevantColumn(int index_column, int index_relevant_column) {

        boolean is_relevant_column = false;
        if (index_column == index_relevant_column) {
            is_relevant_column = true;
        }
        return is_relevant_column;
    }

    public static boolean isColumnToValidateFormat(int index_column, OndemandColumnsToValidate columns_to_validate) {

        boolean is_column_to_validate = false;
        HashMap<Integer, String> columns_to_validate_format = columns_to_validate.getList_positions_column_to_validate();
        if (columns_to_validate_format.containsKey(index_column)) {
            is_column_to_validate = true;
        }
        return is_column_to_validate;
    }

    public static boolean addRelevantColumnCellValueToStatement(Cell xlsx_cell, int row_index, int index_column,
            StringBuffer error_mail_message, PreparedStatement statement_insert_data, String relevant_column_name) {

        boolean insert_correct = false;
        try {
            if (xlsx_cell != null && !"".equals(xlsx_cell.getStringCellValue())) {
                insert_correct = true;
                statement_insert_data.setString(index_column + 1, xlsx_cell.getStringCellValue());
            } else {
                error_mail_message.append("En la fila " + (row_index + 1) + " la columna '"+relevant_column_name+"' está vacía.\n<br>");
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
        return insert_correct;
    }

    public static boolean validateFormatColumn(Cell xlsx_cell, int row_index, int index_column, PreparedStatement statement_insert_data, String column_name,
            String column_type, StringBuffer error_mail_message) {
        boolean correct_format = true;
        try {
            if (xlsx_cell != null) {
                String cell_data = xlsx_cell.getStringCellValue();

                switch (column_type) {
                case "Date":
                    if (validateDateFormat(cell_data, error_mail_message, column_name, row_index)) {
                        addValueToStatement(statement_insert_data, cell_data, index_column);
                    } else {
                        addValueToStatement(statement_insert_data, null, index_column);
                    }
                    break;
                }
            } else {
                addValueToStatement(statement_insert_data, null, index_column);
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
        return correct_format;
    }

    public static boolean validateDateFormat(String cell_data, StringBuffer error_mail_message, String column_name,
            int row_index) {

        boolean valid_format = false;
        if (cell_data.matches(DATETIME_REGEX) || cell_data.matches(DATE_REGEX)) {
            valid_format = true;
        } else {
            error_mail_message.append("En la fila " + (row_index + 1) + " la columna '" + column_name
                    + "' no tiene el formato correcto (yyyy-MM-dd).\n<br>");
        }
        return valid_format;
    }
    
    public static void addValueToStatement(PreparedStatement statement_insert_data, String data, int index_column) {
        try {
            statement_insert_data.setString(index_column + 1, data);
        } catch (Exception error){
            error.printStackTrace();
        }
    }
    
    public static boolean insertMaintainerExcelData(StreamingReader reader, String tabla, String column,
            int id_client) {
        boolean insert_correctos = true;

        String query_insert_data = "INSERT INTO " + tabla + "(";
        String insert_query_values = ") VALUES (";

        Connection connection = null;
        PreparedStatement statement_insert_data = null;

        int ciclo = 0, file_count_columns = 0;
        int count_empty_rows = 0;
        boolean validate_item_number = true;
        try {
            connection = DBCnx.conexion();

            for (Row xlsx_row : reader) {
                if (ciclo == 0) {
                    String insert_columns_names = "";
                    for (Cell xlsx_cell : xlsx_row) {
                        insert_columns_names += xlsx_cell.getStringCellValue() + ",";
                        insert_query_values += "?,";
                        file_count_columns++;
                    }
                    if (insert_columns_names.length() > 0) {
                        insert_columns_names = insert_columns_names.substring(0, insert_columns_names.length() - 1);
                        insert_query_values = insert_query_values.substring(0, insert_query_values.length() - 1);
                    }
                    query_insert_data += insert_columns_names + insert_query_values + ")";

                    statement_insert_data = connection.prepareStatement(query_insert_data);

                    ciclo++;
                } else {
                    for (int index_column = 0; index_column < file_count_columns; index_column++) {
                        String dato = "";
                        if (xlsx_row.getCell(index_column) != null) {
                            dato = xlsx_row.getCell(index_column).getStringCellValue();
                            statement_insert_data.setString(index_column + 1, dato);
                        } else {
                            statement_insert_data.setNull(index_column + 1, java.sql.Types.NULL);
                        }
                    }

                    if (validate_item_number && count_empty_rows == 0) {
                        statement_insert_data.addBatch();
                    } else if (validate_item_number && count_empty_rows > 0) {
                        validate_item_number = false;
                        break;
                    } else if (count_empty_rows < 100) {
                        validate_item_number = true;
                    }

                    if (ciclo % 20 == 0) {
                        Utils.printOrdDeb(ciclo + " ejecutando batch....");
                        statement_insert_data.executeBatch();
                    }
                    ciclo++;
                }
            }

            if (!validate_item_number) {
                insert_correctos = false;
                DataManager.deleteData("DELETE FROM " + tabla + " WHERE " + column + "=" + id_client);
            } else {
                statement_insert_data.executeBatch();
            }
        } catch (SQLException e) {
            insert_correctos = false;
            e.printStackTrace();
            DataManager.deleteData("DELETE FROM " + tabla + " WHERE " + column + "=" + id_client);

        } finally {
            DBCnx.close(statement_insert_data);
            DBCnx.close(connection);
        }

        return insert_correctos;
    }

    public static void saveFileInDestinationFolder(String file_name, String destination_folder) {
        // Abre el archivo que se acaba de subir
        File afile = new File("/Desarrollo/" + file_name);
        // mueve el archivo a la carpeta de destino.
        if (afile.renameTo(new File(destination_folder + file_name))) {
            Utils.printOrdErr("Archivo guardado correctamente");
        } else {
            Utils.printOrdErr("Error al guardar archivo en carpeta destino");
        }
    }

    public static JSONObject readNotificationSettingFile(JSONObject response, Workbook notifications_workbook,
            int client_id, String option_file_import) {

        int number_of_sheets = notifications_workbook.getNumberOfSheets();
        boolean process_with_errors = false;
        StringBuilder error_message = new StringBuilder("");

        for (int sheet_number = 0; sheet_number < number_of_sheets; sheet_number++) {
            LinkedHashMap<Integer, String> headers_xlsx_data = new LinkedHashMap<Integer, String>();
            LinkedHashMap<String, LinkedHashMap<Integer, Integer>> user_notification_setting = new LinkedHashMap<String, LinkedHashMap<Integer, Integer>>();
            Sheet sheet = notifications_workbook.getSheetAt(sheet_number);
            Iterator<Row> sheet_row_iterator = sheet.iterator();

            process_with_errors = readRowsData(sheet_row_iterator, headers_xlsx_data, user_notification_setting,
                    sheet.getSheetName(), option_file_import, error_message);

            if (!process_with_errors) {
                Notification.preparedFileNotificationSettingToInsert(user_notification_setting, client_id);
            }
            headers_xlsx_data.clear();
            user_notification_setting.clear();
        }

        if ("".equals(error_message.toString())) {
            response.put("resp", "Configuración Guardada Correctamente");
        } else {
            response.put("resp", "Carga con Observaciones, se envió correo con detalle.");
            String subject = "Observaciones carga configuración de notificaciones";
            Mail.sendMail(error_message.toString(), subject, null, null, 5, "Tech-K", 21);
        }
        return response;
    }

    public static boolean readRowsData(Iterator<Row> sheet_row_iterator,
            LinkedHashMap<Integer, String> headers_xlsx_data,
            LinkedHashMap<String, LinkedHashMap<Integer, Integer>> user_notification_setting, String sheet_name,
            String option_file_import, StringBuilder error_message) {

        int index_row = 0;
        boolean headers_with_errors = false;
        boolean rows_with_errors = false;

        while (sheet_row_iterator.hasNext()) {
            if (!headers_with_errors) {
                LinkedHashMap<Integer, Integer> notification_setting = new LinkedHashMap<Integer, Integer>();
                Row row = sheet_row_iterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                int cell_index = 0, mail_receiving_user = 0, user_generates_event = 0;

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (index_row == 0) {
                        headers_with_errors = validateHeaders(cell_index, sheet_name, cell, headers_xlsx_data,
                                option_file_import, error_message);
                        if (headers_with_errors) {
                            break;
                        }
                    } else {
                        if (headers_xlsx_data.get(cell_index).equals("0")) {
                            String receiving_user = cell.getStringCellValue();
                            mail_receiving_user = validateUserExist(cell, index_row, error_message, sheet_name,
                                    receiving_user);
                        } else if (headers_xlsx_data.get(cell_index).equals("00")) {
                            String user_generates = cell.getStringCellValue();
                            user_generates_event = validateUserExist(cell, index_row, error_message, sheet_name,
                                    user_generates);
                        } else {
                            try {
                                int cell_value = (int) cell.getNumericCellValue();

                                if (cell_value == 0 || cell_value == 1) {

                                    notification_setting.put(Integer.parseInt(headers_xlsx_data.get(cell_index)),
                                            cell_value);
                                } else {
                                    error_message.append("En hoja '" + sheet_name + "' - fila: " + (index_row + 1)
                                            + ", La configuración de la notificación: '"
                                            + headers_xlsx_data.get(cell_index) + "' debe ser 0 ó 1.<br>");
                                    rows_with_errors = true;
                                    break;
                                }
                            } catch (IllegalStateException error) {
                                error_message.append("En hoja '" + sheet_name + "' - fila: " + (index_row + 1)
                                        + ", La configuración de la notificación: " + "'"
                                        + headers_xlsx_data.get(cell_index)
                                        + "' debe ser un campo numérico, hoja no procesada.<br>");
                                rows_with_errors = true;
                                break;
                            }
                        }
                    }
                    cell_index++;
                }
                index_row++;

                if (headers_with_errors) {
                    continue;
                }
                if (!rows_with_errors) {
                    if (!user_notification_setting.containsKey(mail_receiving_user + "-" + user_generates_event)) {
                        user_notification_setting.put(mail_receiving_user + "-" + user_generates_event,
                                notification_setting);
                    } else {
                        Utils.print("Ya existe " + mail_receiving_user + "-" + user_generates_event);
                    }
                }
            } else {
                break;
            }
        }
        return headers_with_errors;
    }

    public static int validateUserExist(Cell cell, int index_row, StringBuilder error_message, String sheet_name,
            String user_name) {
        int user_id = 0;
        user_id = DataManager.userExists(user_name);

        if (user_id < 1) {
            error_message.append("En hoja '" + sheet_name + "' - fila: " + (index_row + 1) + ", Usuario '" + user_name
                    + "' " + "no existe, configuración no agregada.<br>");
        }
        return user_id;
    }

    public static boolean validateHeaders(int cell_index, String sheet_name, Cell cell,
            LinkedHashMap<Integer, String> headers_xlsx_data, String option_file_import, StringBuilder error_message) {
        boolean errors = false;

        if (cell.getStringCellValue().isEmpty()) {
            error_message.append("La cabecera de la columna " + cell_index + ", de la hoja '" + sheet_name
                    + "' viene vacía. Hoja no procesada.\n");
            errors = true;
        } else {
            try {
                String header = cell.getStringCellValue().split("-")[1];
                if (!option_file_import.contains("usuario")) {
                    validateNotification(headers_xlsx_data, header, cell_index);
                } else {
                    headers_xlsx_data.put(cell_index, header);
                }
            } catch (ArrayIndexOutOfBoundsException error) {
                error_message.append("Error nombre de la cabecera en columna: " + cell_index + " hoja '" + sheet_name
                        + "'. Falta '-', hoja no procesada.");
                errors = true;
            }
        }
        return errors;
    }

    public static void validateNotification(LinkedHashMap<Integer, String> headers_xlsx_data, String header,
            int cell_index) {
        headers_xlsx_data.put(cell_index, header);
    }

    public static XSSFSheet generateSheet(ResultSet resultSet, FormatType[] formatTypes, XSSFSheet sheet,
            XSSFCellStyle style, int currentRow, XSSFWorkbook workbook) throws Exception {

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        if (formatTypes != null && formatTypes.length != resultSetMetaData.getColumnCount()) {
            throw new IllegalStateException(
                    "Number of types is not identical to number of resultset columns. " + "Number of types: "
                            + formatTypes.length + ". Number of columns: " + resultSetMetaData.getColumnCount());
        }

        XSSFRow row = sheet.createRow(currentRow);
        int numCols = resultSetMetaData.getColumnCount();
        boolean isAutoDecideFormatTypes;
        if (isAutoDecideFormatTypes = (formatTypes == null)) {
            formatTypes = new FormatType[numCols];
        }

        // defino estilo titulo

        XSSFCellStyle estilo = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        estilo.setFont(font);
        estilo.setFillBackgroundColor(HSSFColor.GREEN.index);
        estilo.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        for (int i = 0; i < numCols; i++) {
            String title = resultSetMetaData.getColumnLabel(i + 1);
            ExcelUtils.writeCell(row, i, title, FormatType.TEXT, null, estilo, workbook);
            if (isAutoDecideFormatTypes) {
                Class _class = Class.forName(resultSetMetaData.getColumnClassName(i + 1));
                formatTypes[i] = ExcelUtils.getFormatType(_class);
            }
        }
        currentRow++;
        // Write report rows
        while (resultSet.next()) {
            row = sheet.createRow(currentRow++);
            row.setHeightInPoints(18);
            for (int i = 0; i < numCols; i++) {
                Object value = resultSet.getObject(i + 1);
                ExcelUtils.writeCell(row, i, value, formatTypes[i], null, style, workbook);
            }
        }
        // Autosize columns
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn((short) i);
        }
        return sheet;
    }

    public static void generateExcelFile(HttpServletResponse response, String report_option, String client_id) {
        switch (report_option) {
        case "logs":
            generateLogsReportFile(response);
            break;
        case "user_notification":
            generateNotificationReportFile(response, client_id, 1);
            break;
        case "client_notification":
            generateNotificationReportFile(response, client_id, 2);
            break;
        }

    }

    public static void generateLogsReportFile(HttpServletResponse response) {

        XSSFWorkbook wb = new XSSFWorkbook();
        try {
            FormatType[] format_type = null;
            String nombreHoja1 = "Logins";
            String nombreHoja2 = "Modulo";
            String nombreHoja3 = "Mantenedor";

            XSSFSheet sheet1 = null;
            XSSFSheet sheet2 = null;
            XSSFSheet sheet3 = null;

            sheet1 = (XSSFSheet) wb.createSheet(nombreHoja1);
            sheet2 = (XSSFSheet) wb.createSheet(nombreHoja2);
            sheet3 = (XSSFSheet) wb.createSheet(nombreHoja3);

            String query_logs_data = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',u.idusuario 'Id Usuario',"
                    + "u.nombre 'Nombre Usuario',p.nombre 'Perfil',l.fecha_acceso 'Fecha' FROM "
                    + "log_acceso_modulos l LEFT JOIN usuario u ON l.id_usuario = u.idusuario "
                    + "LEFT JOIN cliente c ON c.idcliente = l.id_cliente left join perfil p "
                    + "ON p.idperfil = u.perfil_idperfil WHERE modulo = 'login' ORDER BY fecha_acceso ASC;";

            format_type = new FormatType[] { FormatType.TEXT, FormatType.TEXT, FormatType.TEXT,
                    FormatType.TEXT, FormatType.TEXT, FormatType.TEXT };
            getDataQueryReport(sheet1, wb, query_logs_data, format_type);

            String query_modules_access = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                    + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',l.fecha_acceso 'Fecha',"
                    + "l.modulo 'Módulo', '' Específico FROM log_acceso_modulos l LEFT JOIN "
                    + "usuario u ON l.id_usuario = u.idusuario LEFT JOIN cliente c ON c.idcliente = l.id_cliente "
                    + "left join perfil p ON p.idperfil = u.perfil_idperfil WHERE modulo not in ('login', 'Reportes')";

            // Obtengo la información de los reportes descargados
            String query_report_zone_access = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                    + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario', r.fecha_descarga 'Fecha',"
                    + "'Reportes' 'Módulo', r.nombre_reporte 'Específico' "
                    + "FROM log_descarga_reportes r LEFT JOIN usuario u ON u.idusuario = r.id_usuario "
                    + "LEFT JOIN cliente c ON c.idcliente = r.id_cliente";

            // Obtengo la información total de los módulos (Accesos y descarga
            // de reportes)
            String query_users_sections_access = "(" + query_modules_access + ") UNION (" + query_report_zone_access
                    + ")";
            format_type = new FormatType[] { FormatType.TEXT, FormatType.TEXT, FormatType.TEXT,
                    FormatType.TEXT, FormatType.TEXT, FormatType.TEXT, FormatType.TEXT };
            getDataQueryReport(sheet2, wb, query_users_sections_access, format_type);

            // Obtengo la información del log de Mercado Relevante
            String query_relevant_market = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                    + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',mr.fecha 'Fecha',"
                    + "'Mercado Relevante' Sección, case when mr.accion = 'Create' then "
                    + "'Creación' when mr.accion = 'Update' then 'Modificacion' when mr.accion = "
                    + "'Delete' then 'Eliminación' END Acción FROM log_mantenedor_clienteDiccionario mr "
                    + "LEFT JOIN usuario u ON mr.id_usuario = u.idusuario LEFT JOIN cliente c ON mr.id_cliente = c.idcliente";

            // Obtengo la información de los logs de cambios en el mantenedor
            // Glosa Región
            String query_region_glosa = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                    + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',mr.fecha 'Fecha','Glosas de Despacho' Sección, case when mr.accion = 'Create' then 'Creación' "
                    + "when mr.accion = 'Update' then 'Modificacion' when mr.accion = 'Delete' then "
                    + "'Eliminación' end Acción FROM log_mantenedor_glosaRegion  mr LEFT JOIN usuario u ON "
                    + "mr.id_usuario = u.idusuario LEFT JOIN cliente c ON mr.id_cliente = c.idcliente";

            // Obtengo la información de los logs de cambios en el mantenedor
            // catalogo de productos
            String query_products_catalog = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                    + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',mr.fecha 'Fecha',"
                    + "'Catálogo de Productos' Sección, case when mr.accion = 'Create' then 'Creación' "
                    + "when mr.accion = 'Update' then 'Modificacion' when mr.accion = 'Delete' then 'Eliminación' "
                    + "end Acción FROM log_mantenedor_productos  mr LEFT JOIN usuario u ON mr.id_usuario = u.idusuario "
                    + "LEFT JOIN cliente c ON mr.id_cliente = c.idcliente";

            // Obtengo la información de los logs de cambios en el mantenedor
            // responsable licitacion
            String query_licitation_responsible = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                    + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',mr.fecha 'Fecha',"
                    + "'Responsable Licitación' Sección,case when mr.accion = 'Create' then 'Creación' "
                    + "when mr.accion = 'Update' then 'Modificacion' when mr.accion = 'Delete' then 'Eliminación' "
                    + "end Acción FROM `log_mantenedor_usuarioParametroAsignaciion` mr LEFT JOIN usuario u ON "
                    + "mr.usuario_idusuario = u.idusuario LEFT JOIN cliente c ON mr.cliente_idcliente = c.idcliente";

            // Obtengo la información total de los módulos (Accesos y descarga
            // de reportes)
            String query_users_maintainer_actions = "(" + query_relevant_market + ") UNION (" + query_region_glosa
                    + ") " + "UNION (" + query_products_catalog + ") UNION (" + query_licitation_responsible
                    + ") ORDER BY fecha;";
            format_type = new FormatType[] { FormatType.TEXT, FormatType.TEXT, FormatType.TEXT,
                    FormatType.TEXT, FormatType.TEXT, FormatType.TEXT, FormatType.TEXT };
            getDataQueryReport(sheet3, wb, query_users_maintainer_actions, format_type);

            OutputStream out = response.getOutputStream();
            wb.write(out);
            out.close();
        } catch (Exception e) {
            try {
                wb.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public static void getDataQueryReport(XSSFSheet report_sheet, XSSFWorkbook report_workbook,
            String query_report_data, FormatType[] format_type) {
        Statement consulta = null;
        ResultSet resultados = null;
        Connection conexion = null;

        try {

            conexion = DBCnx.conexion();
            consulta = conexion.createStatement();

            resultados = consulta.executeQuery(query_report_data);
            int fila = 0;

            XSSFCellStyle style = null;
            style = (XSSFCellStyle) report_workbook.createCellStyle();
            Font font = report_workbook.createFont();
            font.setFontName("Calibri");
            font.setFontHeight((short) (11 * 20));
            style.setFont(font);

            while (resultados.next()) {
                Excel.generateSheet(resultados, format_type, report_sheet, style, fila, report_workbook);
            }

        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.closeAll(resultados, consulta, conexion);
        }
    }

    public static void generateNotificationReportFile(HttpServletResponse response, String client_id,
            int notification_type) {
        try {
            LinkedHashMap<String, String> list_notifications = getLinkedHashMap(String.valueOf(notification_type),
                    "SELECT id, alerta, tipo FROM mail_tipoAlerta WHERE tipo = ?");

            LinkedHashMap<String, String> list_user_notification = getLinkedHashMap(client_id,
                    "SELECT u.idusuario, u.usuario FROM usuario u WHERE u.cliente_idcliente = ? AND u.habilitado = 1 ORDER BY u.usuario ASC");

            XSSFWorkbook user_notifications_workbook = new XSSFWorkbook();

            prepareSheetNotifications(user_notifications_workbook, list_notifications, list_user_notification,
                    notification_type);

            OutputStream out = response.getOutputStream();
            user_notifications_workbook.write(out);
            out.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static void prepareSheetNotifications(XSSFWorkbook user_notifications_workbook,
            LinkedHashMap<String, String> list_notifications, LinkedHashMap<String, String> list_user_notification,
            int notification_type) {

        int index_row = 1;
        XSSFSheet user_sheet = null;
        if (notification_type == 2) {
            user_sheet = (XSSFSheet) user_notifications_workbook.createSheet("Por Cliente");
        }

        Iterator<String> user_notification = list_user_notification.keySet().iterator();
        while (user_notification.hasNext()) {
            String current_user_id = user_notification.next();
            String current_user_name = list_user_notification.get(current_user_id);

            switch (notification_type) {
            case 1:
                createUserNotificationsSheet(user_notifications_workbook, current_user_id, current_user_name,
                        list_notifications, notification_type, list_user_notification);
                break;
            case 2:
                index_row = createSheetsByClient(user_notifications_workbook, list_notifications,
                        list_user_notification, notification_type, current_user_id, current_user_name, index_row,
                        user_sheet);
                break;
            }
        }
    }

    public static int createSheetsByClient(XSSFWorkbook user_notifications_workbook,
            LinkedHashMap<String, String> list_notifications, LinkedHashMap<String, String> list_user_notification,
            int notification_type, String current_user_id, String current_user_name, int index_row,
            XSSFSheet user_sheet) {

        setNotificationHeader(user_notifications_workbook, list_notifications, user_sheet, notification_type);
        index_row = setClientNotificationRows(list_notifications, list_user_notification, current_user_id,
                current_user_name, user_notifications_workbook, user_sheet, index_row);

        return index_row;
    }

    public static void createUserNotificationsSheet(XSSFWorkbook user_notifications_workbook, String user_id,
            String user_name, LinkedHashMap<String, String> list_notifications, int notification_type,
            LinkedHashMap<String, String> list_user_notification) {

        XSSFSheet user_sheet = null;
        user_sheet = (XSSFSheet) user_notifications_workbook.createSheet(user_name);

        setNotificationHeader(user_notifications_workbook, list_notifications, user_sheet, notification_type);
        setUserNotificationRows(user_name, user_id, list_notifications, user_sheet, user_notifications_workbook,
                list_user_notification);
    }

    public static LinkedHashMap<String, String> getLinkedHashMap(String column_id, String query) {
        LinkedHashMap<String, String> values_hash_map = new LinkedHashMap<String, String>();

        Connection connection = DBCnx.conexion();
        PreparedStatement statement_list_users = null;
        ResultSet users_list = null;
        try {
            statement_list_users = connection.prepareStatement(query);
            statement_list_users.setString(1, column_id);
            users_list = statement_list_users.executeQuery();

            while (users_list.next()) {
                String value_id = users_list.getString(1);
                String value_name = users_list.getString(2);

                values_hash_map.put(value_id, value_name);
            }
        } catch (SQLException error) {
            error.printStackTrace();
        } finally {
            DBCnx.closeAll(users_list, statement_list_users, connection);
        }
        return values_hash_map;
    }

    public static void setNotificationHeader(XSSFWorkbook user_notifications_workbook,
            LinkedHashMap<String, String> list_notifications, XSSFSheet user_sheet, int notification_type) {

        try {
            XSSFRow row = user_sheet.createRow(0);
            XSSFCellStyle estilo = user_notifications_workbook.createCellStyle();
            Font font = user_notifications_workbook.createFont();
            font.setBold(true);
            font.setColor(HSSFColor.WHITE.index);
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            estilo.setFont(font);
            estilo.setFillBackgroundColor(HSSFColor.GREEN.index);
            estilo.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

            Iterator<String> header_name = list_notifications.keySet().iterator();
            int column_index = 0;

            ExcelUtils.writeCell(row, 0, "Usuario que recibe notificación-0", FormatType.TEXT, null, estilo,
                    user_notifications_workbook);
            if (notification_type == 1) {
                ExcelUtils.writeCell(row, 1, "Usuario que genera evento-00", FormatType.TEXT, null, estilo,
                        user_notifications_workbook);
                column_index = 2;
            } else if (notification_type == 2) {
                column_index = 1;
            }

            while (header_name.hasNext()) {
                String current_notification = header_name.next();
                String current_notification_name = list_notifications.get(current_notification);
                String header = current_notification_name + "-" + current_notification;
                ExcelUtils.writeCell(row, column_index, header, FormatType.TEXT, null, estilo, user_notifications_workbook);
                column_index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setUserNotificationRows(String current_user_name, String current_user_id,
            LinkedHashMap<String, String> list_notifications, XSSFSheet user_sheet,
            XSSFWorkbook user_notifications_workbook, LinkedHashMap<String, String> list_user_notification) {

        try {
            LinkedHashMap<String, String> user_notifications_setting = getLinkedHashMap(current_user_id,
                    "SELECT mcu.id, CONCAT(mua.id_tipoAlerta,'-',u.usuario) FROM `mail_usuarioAlerta` mua left join mail_tipoAlerta mta ON mta.id = mua.`id_tipoAlerta` left join "
                            + "mail_configuracion_usuario mcu on mcu.id_mail_usuario_alerta = mua.id left join usuario u ON u.idusuario = mcu.id_usuario WHERE "
                            + "`id_mailAdmin` = ? and mta.tipo = 1 order by mcu.id_usuario");

            LinkedHashMap<String, String> list_revised_users = new LinkedHashMap<String, String>();

            Iterator<String> user_generated_notification = user_notifications_setting.keySet().iterator();
            int index_row = 1;

            while (user_generated_notification.hasNext()) {
                String user_notification = user_generated_notification.next();
                String user_notification_value[] = user_notifications_setting.get(user_notification).split("-");
                String user_notification_name = user_notification_value[1];

                if (!list_revised_users.containsKey(user_notification_name)) {
                    list_revised_users.put(user_notification_name, user_notification_name);
                    XSSFRow row = user_sheet.createRow(index_row);

                    ExcelUtils.writeCell(row, 0, current_user_name, FormatType.TEXT, null, null, user_notifications_workbook);
                    ExcelUtils.writeCell(row, 1, user_notification_name, FormatType.TEXT, null, null, user_notifications_workbook);
                    setConfigurationValues(row, list_notifications, user_notifications_setting,
                            user_notifications_workbook, index_row, user_sheet, user_notification_name, 2);
                    index_row++;
                }
            }
            generateRowValuesFromUnconfiguredUsers(list_revised_users, index_row, list_user_notification,
                    current_user_name, user_sheet, user_notifications_workbook, list_notifications);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static void generateRowValuesFromUnconfiguredUsers(LinkedHashMap<String, String> list_revised_users,
            int index_row, LinkedHashMap<String, String> list_user_notification, String current_user_name,
            XSSFSheet user_sheet, XSSFWorkbook user_notifications_workbook,
            LinkedHashMap<String, String> list_notifications) {

        LinkedHashMap<String, String> user_notifications_setting = new LinkedHashMap<String, String>();
        Iterator<String> user_generated_notification = list_user_notification.keySet().iterator();

        while (user_generated_notification.hasNext()) {
            try {
                String id_user_notification = user_generated_notification.next();
                String name_user_notification = list_user_notification.get(id_user_notification);

                if (!list_revised_users.containsKey(name_user_notification)) {
                    list_revised_users.put(name_user_notification, name_user_notification);
                    XSSFRow row = user_sheet.createRow(index_row);

                    ExcelUtils.writeCell(row, 0, current_user_name, FormatType.TEXT, null, null, user_notifications_workbook);
                    ExcelUtils.writeCell(row, 1, name_user_notification, FormatType.TEXT, null, null, user_notifications_workbook);
                    setConfigurationValues(row, list_notifications, user_notifications_setting,
                            user_notifications_workbook, index_row, user_sheet, name_user_notification, 2);
                    index_row++;
                }

            } catch (Exception error) {
                error.printStackTrace();
            }
        }
    }

    public static void setConfigurationValues(XSSFRow row, LinkedHashMap<String, String> list_notifications,
            LinkedHashMap<String, String> user_notifications_setting, XSSFWorkbook user_notifications_workbook,
            int index_row, XSSFSheet user_sheet, String user_notification_name, int column_index) {

        try {
            Iterator<String> notifications_iterator = list_notifications.keySet().iterator();

            while (notifications_iterator.hasNext()) {

                String current_notification_type = notifications_iterator.next();

                if (user_notifications_setting
                        .containsValue(current_notification_type + "-" + user_notification_name)) {
                    ExcelUtils.writeCell(row, column_index, 1, FormatType.INTEGER, null, null, user_notifications_workbook);
                } else {
                    ExcelUtils.writeCell(row, column_index, 0, FormatType.INTEGER, null, null, user_notifications_workbook);
                }
                column_index++;
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static int setClientNotificationRows(LinkedHashMap<String, String> list_notifications,
            LinkedHashMap<String, String> list_user_notification, String current_user_id, String current_user_name,
            XSSFWorkbook user_notifications_workbook, XSSFSheet user_sheet, int index_row) {
        try {
            LinkedHashMap<String, String> user_notifications_setting = getLinkedHashMap(current_user_id,
                    "SELECT mua.id, CONCAT(mua.id_tipoAlerta,'-',u.usuario) FROM `mail_usuarioAlerta` mua "
                            + "left join mail_tipoAlerta mta ON mta.id = mua.`id_tipoAlerta` left join usuario u ON "
                            + "u.idusuario = mua.id_mailAdmin WHERE `id_mailAdmin` = ? and mta.tipo = 2 order by mua.id_mailAdmin");

            LinkedHashMap<String, String> list_revised_users = new LinkedHashMap<String, String>();
            XSSFRow row = user_sheet.createRow(index_row);

            Iterator<String> user_generated_notification = user_notifications_setting.keySet().iterator();
            if (user_generated_notification.hasNext()) {
                while (user_generated_notification.hasNext()) {
                    String user_notification = user_generated_notification.next();
                    String user_notification_value[] = user_notifications_setting.get(user_notification).split("-");
                    String user_notification_name = user_notification_value[1];

                    index_row = writeCellNotificationsSettingsByClient(list_revised_users, row, index_row,
                            user_notifications_workbook, user_sheet, user_notification_name, current_user_name,
                            list_notifications, user_notifications_setting);
                }
            } else {
                index_row = writeCellNotificationsSettingsByClient(list_revised_users, row, index_row,
                        user_notifications_workbook, user_sheet, current_user_name, current_user_name,
                        list_notifications, user_notifications_setting);
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
        return index_row;
    }

    public static int writeCellNotificationsSettingsByClient(LinkedHashMap<String, String> list_revised_users,
            XSSFRow row, int index_row, XSSFWorkbook user_notifications_workbook, XSSFSheet user_sheet,
            String user_notification_name, String current_user_name, LinkedHashMap<String, String> list_notifications,
            LinkedHashMap<String, String> user_notifications_setting) {

        try {
            if (!list_revised_users.containsKey(user_notification_name)) {
                list_revised_users.put(user_notification_name, user_notification_name);
                ExcelUtils.writeCell(row, 0, current_user_name, FormatType.TEXT, null, null, user_notifications_workbook);
                setConfigurationValues(row, list_notifications, user_notifications_setting, user_notifications_workbook,
                        index_row, user_sheet, user_notification_name, 1);
                index_row++;
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return index_row;
    }
    
    public static void setRowsValues(SXSSFSheet report_sheet, int row_index, int cant_columnas, String info_postulaciones[]) {
        
        Row row = report_sheet.createRow(row_index);
        for (int ix = 0; ix < cant_columnas; ix++) {
            row.createCell(ix).setCellValue(info_postulaciones[ix]);
        }
    }
}
