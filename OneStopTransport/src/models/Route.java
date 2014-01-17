package models;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Route {
	
	private int route_id=-1;
	private int agency_id=-1;
	private String route_short_name;
	private String route_long_name;
	private String route_desc;
	private int route_type=-1;
	private String route_url;
	private String route_color;
	private String route_text_color;
	
	public String toString(){
		String temp = route_id + "," + agency_id + "," + route_short_name + "," + route_long_name + "," + route_desc + "," + 
				route_type + "," + route_url + "," + route_color + "," + route_text_color;
		return temp.replace("null", "").replace("-1", "");
	}
	
	public int getRoute_id() {
		return route_id;
	}
	public void setRoute_id(int route_id) {
		this.route_id = route_id;
	}
	public int getAgency_id() {
		return agency_id;
	}
	public void setAgency_id(int agency_id) {
		this.agency_id = agency_id;
	}
	public String getRoute_short_name() {
		return route_short_name;
	}
	public void setRoute_short_name(String route_short_name) {
		this.route_short_name = route_short_name;
	}
	public String getRoute_long_name() {
		return route_long_name;
	}
	public void setRoute_long_name(String route_long_name) {
		this.route_long_name = route_long_name;
	}
	public String getRoute_desc() {
		return route_desc;
	}
	public void setRoute_desc(String route_desc) {
		this.route_desc = route_desc;
	}
	public int getRoute_type() {
		return route_type;
	}
	public void setRoute_type(int route_type) {
		this.route_type = route_type;
	}
	public String getRoute_url() {
		return route_url;
	}
	public void setRoute_url(String route_url) {
		this.route_url = route_url;
	}
	public String getRoute_color() {
		return route_color;
	}
	public void setRoute_color(String route_color) {
		this.route_color = route_color;
	}
	public String getRoute_text_color() {
		return route_text_color;
	}
	public void setRoute_text_color(String route_text_color) {
		this.route_text_color = route_text_color;
	}
}
