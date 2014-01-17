package models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Agency {

	private int agency_id=-1;
	private String agency_name;
	private String agency_url;
	private String agency_timezone;
	private String agency_language;
	private String agency_phone;
	private String agency_fare_url;

	public String toString(){
		String temp = agency_id + "," + agency_name + "," + agency_url + "," + agency_timezone + "," + agency_language + "," +
				agency_phone + "," + agency_fare_url;
		return temp.replace("null", "").replace("-1", "");
	}

	public int getAgency_id() {
		return agency_id;
	}
	public void setAgency_id(int agency_id) {
		this.agency_id = agency_id;
	}
	public String getAgency_name() {
		return agency_name;
	}
	public void setAgency_name(String agency_name) {
		this.agency_name = agency_name;
	}
	public String getAgency_url() {
		return agency_url;
	}
	public void setAgency_url(String agency_url) {
		this.agency_url = agency_url;
	}
	public String getAgency_timezone() {
		return agency_timezone;
	}
	public void setAgency_timezone(String agency_timezone) {
		this.agency_timezone = agency_timezone;
	}
	public String getAgency_language() {
		return agency_language;
	}
	public void setAgency_language(String agency_language) {
		this.agency_language = agency_language;
	}
	public String getAgency_phone() {
		return agency_phone;
	}
	public void setAgency_phone(String agency_phone) {
		this.agency_phone = agency_phone;
	}
	public String getAgency_fare_url() {
		return agency_fare_url;
	}
	public void setAgency_fare_url(String agency_fare_url) {
		this.agency_fare_url = agency_fare_url;
	}
}
