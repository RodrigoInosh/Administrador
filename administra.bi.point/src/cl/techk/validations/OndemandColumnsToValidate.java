package cl.techk.validations;

import java.util.HashMap;

public class OndemandColumnsToValidate {

    HashMap<String, String> list_column_to_validate;
    HashMap<Integer, String> list_positions_column_to_validate;
    String relevant_column;
    int relevant_column_position; 

    public OndemandColumnsToValidate() {
        list_column_to_validate = new HashMap<String, String>();
        list_positions_column_to_validate = new HashMap<Integer, String>();
    }
    
    public OndemandColumnsToValidate(HashMap<String, String> list_column_to_validate) {
        this.list_column_to_validate = list_column_to_validate;
    }

    public void addValueToList(String column_name, String column_type) {
        list_column_to_validate.put(column_name, column_type);
    }
    
    public void putColumnPositionOnHash(int column_position, String column_name) {
        list_positions_column_to_validate.put(column_position, column_name);
    }

    public HashMap<String, String> getList_column_to_validate() {
        return list_column_to_validate;
    }

    public void setList_column_to_validate(HashMap<String, String> list_column_to_validate) {
        this.list_column_to_validate = list_column_to_validate;
    }

    public String getRelevant_column() {
        return relevant_column;
    }

    public void setRelevant_column(String relevant_column) {
        this.relevant_column = relevant_column;
    }

    public HashMap<Integer, String> getList_positions_column_to_validate() {
        return list_positions_column_to_validate;
    }

    public void setList_positions_column_to_validate(HashMap<Integer, String> list_positions_column_to_validate) {
        this.list_positions_column_to_validate = list_positions_column_to_validate;
    }

    public int getRelevant_column_position() {
        return relevant_column_position;
    }

    public void setRelevant_column_position(int relevant_column_position) {
        this.relevant_column_position = relevant_column_position;
    }
}
