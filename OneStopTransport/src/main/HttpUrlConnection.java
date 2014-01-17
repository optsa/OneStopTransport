package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

import models.Agency;
import models.Calendar;
import models.Route;
import models.Stop;
import models.Stop_Time;
import models.Trip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 
public class HttpUrlConnection {
 
	private final String APIkey = "BajkERhfrGvqbxTSOMIbuNSGzZGgHvFhehZySmhC";
	private final int SMTUCid = 8;
 
	public void GetAgencies(Map<Integer, Agency> agencies) throws Exception {
 
		String url = "https://api.ost.pt/agencies/" + SMTUCid + "/?key=" + APIkey;
		PrintWriter writer = new PrintWriter("agency.txt", "UTF-8");
		writer.println("agency_id,agency_name,agency_url,agency_timezone,agency_language,agency_phone,agency_fare_url");
 
		JSONObject o = request(url);
		Agency agency = new Agency();
		agency.setAgency_id(o.getInt("id"));
		agency.setAgency_name(o.getString("agency_name"));
		agency.setAgency_timezone(o.getString("agency_timezone"));
		agency.setAgency_url(o.getString("agency_url"));
		agency.setAgency_language(o.getString("agency_lang"));
		agency.setAgency_phone(o.getString("agency_phone"));
		//agency.setAgency_fare_url(o.getString("agency_fare_url"));
		
		agencies.put(agency.getAgency_id(), agency);
		writer.println(agency.toString());
		writer.close();
		
		calendarWriter();
	}

	private void calendarWriter() throws FileNotFoundException,
			UnsupportedEncodingException {
		Calendar cal = new Calendar();
		cal.setService_id(1);
		cal.setMonday(1);
		cal.setTuesday(1);
		cal.setWednesday(1);
		cal.setThursday(1);
		cal.setFriday(1);
		cal.setSaturday(1);
		cal.setSunday(1);
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		java.util.Calendar today = java.util.Calendar.getInstance(); 
		cal.setStart_date(dateFormat.format(today.getTime()));
		java.util.Calendar today_plus_year = java.util.Calendar.getInstance();
		today_plus_year.add( java.util.Calendar.YEAR, 1 );
		cal.setEnd_date(dateFormat.format(today_plus_year.getTime()));
		PrintWriter calendarWriter = new PrintWriter("calendar.txt", "UTF-8");
		calendarWriter.println("service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date");
		calendarWriter.println(cal.toString());
		calendarWriter.close();
	}
	
	public void GetRoutes(Map<Integer, Route> routes) throws Exception {
		 
		String url = "https://api.ost.pt/routes/?agency=" + SMTUCid + "&key=" + APIkey;
		PrintWriter writer = new PrintWriter("routes.txt", "UTF-8");
		writer.println("route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color");
 
		JSONObject o = request(url);
		JSONArray objects = o.getJSONArray("Objects");
		JSONObject meta = o.getJSONObject("Meta");
		int offset = 0;
		do{
			url = "https://api.ost.pt/routes/?agency=" + SMTUCid + "&key=" + APIkey+ "&offset=" + offset;
			o = request(url);
			objects = o.getJSONArray("Objects");
			meta = o.getJSONObject("Meta");
			offset+=25;
			
			for (int i=0; i<objects.length();i++){
				JSONObject temp = objects.getJSONObject(i);
				JSONObject agency = temp.getJSONObject("agency");
				Route route = new Route();
				route.setRoute_id(temp.getInt("id"));
				route.setRoute_color(temp.getString("route_color"));
				route.setRoute_desc(temp.getString("route_desc"));
				route.setRoute_long_name(temp.getString("route_long_name"));
				route.setRoute_short_name(temp.getString("route_short_name"));
				route.setRoute_text_color(temp.getString("route_text_color"));
				route.setRoute_type(temp.getInt("route_type"));
				route.setRoute_url(temp.getString("route_url"));
				route.setAgency_id(agency.getInt("id"));
	
				routes.put(route.getRoute_id(), route);
				writer.println(route.toString());
				
			}	
		}while(!meta.getString("next_page").equals("null"));
		writer.close();
	}
	
	public void GetStops(Map<Integer, Stop> stops) throws Exception {
		String url = "https://api.ost.pt/stops/?agency=" + SMTUCid + "&withroutes=false&key=" + APIkey;
		PrintWriter writer = new PrintWriter("stops.txt", "UTF-8");
		writer.println("stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station,stop_timezone,wheelchair_boarding");
		
		JSONObject o = request(url);
		JSONArray objects = o.getJSONArray("Objects");
		JSONObject meta = o.getJSONObject("Meta");
		int offset = 0;
		do{
			url = "https://api.ost.pt/stops/?agency=" + SMTUCid + "&withroutes=false&key=" + APIkey+ "&offset=" + offset;
			o = request(url);
			objects = o.getJSONArray("Objects");
			meta = o.getJSONObject("Meta");
			offset+=25;
			
			for (int i=0; i<objects.length();i++){
				JSONObject temp = objects.getJSONObject(i);
				Stop stop = new Stop();
				stop.setStop_id(temp.getInt("id"));
				stop.setStop_code(temp.getString("stop_code").replace(",", " "));
				stop.setStop_name(temp.getString("stop_name").replace(",", " "));
				if(!temp.getString("stop_desc").trim().equals(temp.getString("stop_name").trim()))
					stop.setStop_desc(temp.getString("stop_desc").replace(",", " "));
				stop.setStop_url(temp.getString("stop_url"));
				if(!temp.getString("parent_station_id").equals("null"))
					stop.setParent_station(temp.getInt("parent_station_id"));
				stop.setLocation_type(temp.getString("location_type").equals("false")?0:1);
				JSONObject point = temp.getJSONObject("point");
				JSONArray coordinates = point.getJSONArray("coordinates");
				stop.setStop_lon(coordinates.getString(0));
				stop.setStop_lat(coordinates.getString(1));		
				//stop.setStop_timezone(temp.getString("stop_timezone"));
				//stop.setZone_id(temp.getString("zone_id"));
				//stop.setWheelchair_boarding(temp.getInt("wheelchair_boarding"));
				
				stops.put(stop.getStop_id(), stop);
				writer.println(stop.toString());
			}	
		}while(!meta.getString("next_page").equals("null"));
		writer.close();
	}
	
	public void GetStopTimes(Set<Integer> set, Set<Integer> tripSet, Map<Integer, Stop_Time> stoptimes) throws Exception{
		PrintWriter writer = new PrintWriter("stop_times.txt", "UTF-8");
		writer.println("trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled");
		for(int routeId : set)
		{
			String url = "https://api.ost.pt/stoptimes/?route=" + routeId + "&key=" + APIkey;
			 
			JSONObject o = request(url);
			JSONArray objects = o.getJSONArray("Objects");
			JSONObject meta = o.getJSONObject("Meta");
			int offset = 0;
			int stopSequence = 0;
			String arrival_time = "";
			String departure_time = "";
			do{
				url = "https://api.ost.pt/stoptimes/?route=" + routeId + "&key=" + APIkey + "&offset=" + offset;
				o = request(url);
				objects = o.getJSONArray("Objects");
				meta = o.getJSONObject("Meta");
				offset+=25;
				
				
				for (int i=0; i<objects.length();i++){
					JSONObject temp = objects.getJSONObject(i);
					int tripId = temp.getInt("trip_id");
					boolean changeArrivalTime = false;
					boolean changeDepartureTime = false;
					if(tripSet.contains(tripId))
					{
						Stop_Time stop_time = new Stop_Time();
						
						if(temp.getInt("stop_sequence")>stopSequence){
							if(!temp.getString("arrival_times").equals("null")){
								if(arrival_time.equals("") || arrival_time.compareTo(temp.getString("arrival_times"))<0 ){
									arrival_time = temp.getString("arrival_times");
								}
								else{
									String tempTime = temp.getString("arrival_times");
									String[] tempTimeParcels = tempTime.split(":");
									int hours = 24 + Integer.parseInt(tempTimeParcels[0]);
									arrival_time = hours + ":" + tempTimeParcels[1] + ":" + tempTimeParcels[2];
									changeArrivalTime = true;
								}
							}
							if(!temp.getString("departure_times").equals("null")){
								if(departure_time.equals("")|| departure_time.compareTo(temp.getString("departure_times"))<0){
									departure_time = temp.getString("departure_times");
								}
								else {
									String tempTime = temp.getString("departure_times");
									String[] tempTimeParcels = tempTime.split(":");
									int hours = 24 + Integer.parseInt(tempTimeParcels[0]);
									departure_time = hours + ":" + tempTimeParcels[1] + ":" + tempTimeParcels[2];
									changeDepartureTime = true;
								}
							}
						}
						else{
							arrival_time = temp.getString("arrival_times");
							departure_time = temp.getString("departure_times");
						}
						stopSequence = temp.getInt("stop_sequence");
						
						if(!changeArrivalTime)
							stop_time.setArrival_time(temp.getString("arrival_times"));
						else
							stop_time.setArrival_time(arrival_time);
						if(!changeDepartureTime)
							stop_time.setDeparture_time(temp.getString("departure_times"));
						else
							stop_time.setDeparture_time(departure_time);
						
						stop_time.setStop_id(temp.getInt("stop_id"));
						stop_time.setTrip_id(temp.getInt("trip_id"));
						stop_time.setStop_time_id(temp.getInt("id"));
						stop_time.setStop_sequence(temp.getInt("stop_sequence"));
						stop_time.setStop_headsign(temp.getString("stop_headsign"));
						if(!temp.getString("pickup_type").equals("null"))
							stop_time.setPickup_type(temp.getInt("pickup_type"));
						if(!temp.getString("drop_off_type").equals("null"))
							stop_time.setDrop_off_type(temp.getInt("drop_off_type"));
						stop_time.setShape_dist_traveled(temp.getString("shape_dist_traveled"));
						
						stoptimes.put(stop_time.getStop_time_id(), stop_time);
						writer.println(stop_time.toString());
					}
				}	
			}while(!meta.getString("next_page").equals("null"));
		}
		writer.close();
	}

	public void GetTrips(Set<Integer> set, Map<Integer, Trip> trips) throws Exception{
		PrintWriter writer = new PrintWriter("trips.txt", "UTF-8");
		writer.println("route_id,service_id,trip_id,trip_headsign,trip_short_name,direction_id,block_id,shape_id,wheelchair_accessible");
		for(int routeId : set){
			String url = "https://api.ost.pt/trips/?route=" + routeId + "&key=" + APIkey;
			 
			JSONObject o = request(url);
			JSONArray objects = o.getJSONArray("Objects");
			JSONObject meta = o.getJSONObject("Meta");
			int offset = 0;
			do{
				url = "https://api.ost.pt/trips/?route=" + routeId + "&key=" + APIkey + "&offset=" + offset;
				o = request(url);
				objects = o.getJSONArray("Objects");
				meta = o.getJSONObject("Meta");
				offset+=25;
				
				for (int i=0; i<objects.length();i++){
					JSONObject temp = objects.getJSONObject(i);
					Trip trip = new Trip();
					trip.setTrip_id(temp.getInt("id"));
					trip.setBlock_id(temp.getInt("block_id"));
					trip.setRoute_id(temp.getInt("route_id"));
					trip.setTrip_short_name(temp.getString("trip_short_name").replace(",", " "));
					trip.setTrip_headsign(temp.getString("trip_headsign"));
					//Não estão implementadas shapes
//					if(!temp.getString("shape_id").equals("null"))
//						trip.setShape_id(temp.getInt("shape_id"));
					trip.setService_id(1); //Harcoded porque API não contempla serviços
					//trip.setDirection_id(temp.getInt("direction_id"));
					//trip.setWheelchair_accessible(temp.getInt("wheelchair_accessible"));
					
					trips.put(trip.getTrip_id(), trip);
					writer.println(trip.toString());
				}	
			}while(!meta.getString("next_page").equals("null"));
		}
		writer.close();
	}

	private JSONObject request(String url) throws MalformedURLException,
			IOException, ProtocolException, UnsupportedEncodingException,
			JSONException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
		 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		JSONObject o = new JSONObject(response.toString());
		return o;
	}
}