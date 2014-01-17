package models;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Calendar {
	
	private int service_id;
	private int monday;
	private int tuesday;
	private int wednesday;
	private int thursday;
	private int friday;
	private int saturday;
	private int sunday;
	private String start_date;
	private String end_date;
	
	public String toString(){
		String temp = service_id + "," + monday + "," + tuesday + "," + wednesday + "," + thursday + "," + friday + "," + 
				saturday + "," + sunday + "," + start_date + "," + end_date;
		return temp.replace("null", "");
	}
	
	public int getService_id() {
		return service_id;
	}
	public void setService_id(int service_id) {
		this.service_id = service_id;
	}
	public int getMonday() {
		return monday;
	}
	public void setMonday(int monday) {
		this.monday = monday;
	}
	public int getTuesday() {
		return tuesday;
	}
	public void setTuesday(int tuesday) {
		this.tuesday = tuesday;
	}
	public int getWednesday() {
		return wednesday;
	}
	public void setWednesday(int wednesday) {
		this.wednesday = wednesday;
	}
	public int getThursday() {
		return thursday;
	}
	public void setThursday(int thursday) {
		this.thursday = thursday;
	}
	public int getFriday() {
		return friday;
	}
	public void setFriday(int friday) {
		this.friday = friday;
	}
	public int getSaturday() {
		return saturday;
	}
	public void setSaturday(int saturday) {
		this.saturday = saturday;
	}
	public int getSunday() {
		return sunday;
	}
	public void setSunday(int sunday) {
		this.sunday = sunday;
	}
	public String getStart_date() {
		return start_date;
	}
	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
}
