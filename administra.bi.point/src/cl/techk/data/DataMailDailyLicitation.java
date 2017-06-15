package cl.techk.data;

public class DataMailDailyLicitation {

    String client_name;
    String current_date;
    String message;
    
    public DataMailDailyLicitation(String client_name, String current_date) {
        this.client_name = client_name;
        this.current_date = current_date;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getCurrent_date() {
        return current_date;
    }

    public void setCurrent_date(String current_date) {
        this.current_date = current_date;
    }
    
    public String getMessage() {
    	return message;
    }
    
    public void setMessage(String message) {
    	this.message = message;
    }
}
