package cl.techk.data;

import java.util.ArrayList;
import java.util.Map;

public class DataMailLicitationBulletin {

    int total_licitation_to_expire;
    int total_favorites_licitation_to_expire;
    String current_date;
    String client_name;
    String licitation_type;
    ArrayList<Map<String, String>> licitations_to_expire = new ArrayList<Map<String, String>>();
    ArrayList<Map<String, String>> favorite_licitation_to_expire = new ArrayList<Map<String, String>>();

    public DataMailLicitationBulletin(ArrayList<Map<String, String>> licitation_to_expire, ArrayList<Map<String, String>> favorite_licitation_to_expire, 
            int total_licitation_to_expire, int total_favorites_licitation_to_expire, String client_name, String licitation_type, String current_date) {
        
        this.licitations_to_expire = licitation_to_expire;
        this.favorite_licitation_to_expire = favorite_licitation_to_expire;
        this.client_name = client_name;
        this.licitation_type = licitation_type;
        this.total_favorites_licitation_to_expire = total_favorites_licitation_to_expire;
        this.total_licitation_to_expire = total_licitation_to_expire;
        this.current_date = current_date;
    }

    public ArrayList<Map<String, String>> getLicitations_to_expire() {
        return licitations_to_expire;
    }

    public void setLicitations_to_expire(ArrayList<Map<String, String>> licitation_to_expire) {
        this.licitations_to_expire = licitation_to_expire;
    }

    public ArrayList<Map<String, String>> getFavorite_licitation_to_expire() {
        return favorite_licitation_to_expire;
    }

    public void setFavorite_licitation_to_expire(ArrayList<Map<String, String>> favorite_licitation_to_expire) {
        this.favorite_licitation_to_expire = favorite_licitation_to_expire;
    }
    
    public int getTotal_licitation_to_expire() {
        return total_licitation_to_expire;
    }

    public void setTotal_licitation_to_expire(int total_licitation_to_expire) {
        this.total_licitation_to_expire = total_licitation_to_expire;
    }

    public int getTotal_favorites_licitation_to_expire() {
        return total_favorites_licitation_to_expire;
    }

    public void setTotal_favorites_licitation_to_expire(int total_favorites_licitation_to_expire) {
        this.total_favorites_licitation_to_expire = total_favorites_licitation_to_expire;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getLicitation_type() {
        return licitation_type;
    }

    public void setLicitation_type(String licitation_type) {
        this.licitation_type = licitation_type;
    }

    public String getCurrent_date() {
        return current_date;
    }

    public void setCurrent_date(String current_date) {
        this.current_date = current_date;
    }
}
