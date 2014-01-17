package models;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Stop {

	private int stop_id=-1;
	private String stop_code;
	private String stop_name;
	private String stop_desc;
	private String stop_lat;
	private String stop_lon;
	private String zone_id;
	private String stop_url;
	private int location_type=-1;
	private int parent_station=-1;
	private String stop_timezone;
	private int wheelchair_boarding=-1;
	
	public String toString(){
		String temp = stop_id + "," + stop_code + "," + stop_name + "," + stop_desc + "," + stop_lat + "," + stop_lon + "," + 
				zone_id + "," + stop_url + "," + location_type + "," + parent_station + "," + stop_timezone + "," + 
				wheelchair_boarding;
		return temp.replace("null", "").replace("-1", "");
	}
	
	public int getStop_id() {
		return stop_id;
	}
	public void setStop_id(int stop_id) {
		this.stop_id = stop_id;
	}
	public String getStop_code() {
		return stop_code;
	}
	public void setStop_code(String stop_code) {
		this.stop_code = stop_code;
	}
	public String getStop_name() {
		return stop_name;
	}
	public void setStop_name(String stop_name) {
		this.stop_name = stop_name;
	}
	public String getStop_desc() {
		return stop_desc;
	}
	public void setStop_desc(String stop_desc) {
		this.stop_desc = stop_desc;
	}
	public String getStop_lat() {
		return stop_lat;
	}
	public void setStop_lat(String stop_lat) {
		this.stop_lat = stop_lat;
	}
	public String getStop_lon() {
		return stop_lon;
	}
	public void setStop_lon(String stop_lon) {
		this.stop_lon = stop_lon;
	}
	public String getZone_id() {
		return zone_id;
	}
	public void setZone_id(String zone_id) {
		this.zone_id = zone_id;
	}
	public String getStop_url() {
		return stop_url;
	}
	public void setStop_url(String stop_url) {
		this.stop_url = stop_url;
	}
	public int getLocation_type() {
		return location_type;
	}
	public void setLocation_type(int location_type) {
		this.location_type = location_type;
	}
	public int getParent_station() {
		return parent_station;
	}
	public void setParent_station(int parent_station) {
		this.parent_station = parent_station;
	}
	public String getStop_timezone() {
		return stop_timezone;
	}
	public void setStop_timezone(String stop_timezone) {
		this.stop_timezone = stop_timezone;
	}
	public int getWheelchair_boarding() {
		return wheelchair_boarding;
	}
	public void setWheelchair_boarding(int wheelchair_boarding) {
		this.wheelchair_boarding = wheelchair_boarding;
	}
}
