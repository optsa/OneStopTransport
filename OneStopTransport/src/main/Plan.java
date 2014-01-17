package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

@Path("/Plan")
public class Plan {
	@Context ServletContext context;
	
	@GET
	@Produces("application/xml")
	//@Produces({ MediaType.APPLICATION_JSON})
	public String getPlan(
			@DefaultValue("false")@QueryParam("arriveBy")boolean arriveBy,
			@DefaultValue("false")@QueryParam("wheelchair")boolean wheelchair,
			@DefaultValue("false")@QueryParam("showIntermediateStops")boolean showIntermediateStops,
			@DefaultValue("TRANSIT,WALK") @QueryParam("mode")String mode, //options: TRANSIT, WALK, BUSISH, TRAINISH
			@DefaultValue("QUICK") @QueryParam("optimize")String optimize, //options: QUICK,TRANSFER
			@DefaultValue("-1")@QueryParam("maxWalkDistance")int maxWalkDistance,
			@DefaultValue("3")@QueryParam("walkSpeed")double walkSpeed,
			@DefaultValue("-1")@QueryParam("minTransferTime")int minTransferTime,
			@DefaultValue("-1")@QueryParam("maxTransfers")int maxTransfers,
			@DefaultValue("-1")@QueryParam("date")String date, 
			@DefaultValue("-1")@QueryParam("time")String time, 
			@QueryParam("toPlace")String toPlace, 
			@QueryParam("fromPlace")String fromPlace,
			@DefaultValue("")@QueryParam("preferredRoutes")String preferredRoutes,
			@DefaultValue("")@QueryParam("preferredAgencies")String preferredAgencies,
			@DefaultValue("")@QueryParam("unpreferredRoutes")String unpreferredRoutes,
			@DefaultValue("")@QueryParam("unpreferredAgencies")String unpreferredAgencies,
			@DefaultValue("")@QueryParam("bannedRoutes")String bannedRoutes,
			@DefaultValue("")@QueryParam("bannedAgencies")String bannedAgencies,
			@DefaultValue("")@QueryParam("bannedTrips")String bannedTrips,
			@DefaultValue("")@QueryParam("intermediatePlaces")String intermediatePlaces
			) throws IOException, NoSuchAlgorithmException, NamingException {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat timeFormat = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		
		if(date.equals("-1")){
			date = dateFormat.format(cal.getTime());
		}
		if(time.equals("-1")){
			time = timeFormat.format(cal.getTime());
		}
		
		String temp = readConfig("opentripplannerURL");
		
		if(!temp.equals("")){	
			String url = temp
					+ "arriveBy=" + arriveBy
					+ "&wheelchair=" + wheelchair
					+ "&showIntermediateStops=" + showIntermediateStops
					+ "&time=" + time
					+ "&mode=" + mode
					+ "&optimize=" + optimize
					+ "&maxWalkDistance=" + maxWalkDistance
					+ "&walkSpeed=" + walkSpeed/3.6
					+ "&minTransferTime=" + minTransferTime
					+ "&maxTransfers=" + maxTransfers
					+ "&date=" + date
					+ "&toPlace=" + toPlace
					+ "&fromPlace=" + fromPlace;
			
			if(!preferredRoutes.equals(""))
				url+="&preferredRoutes=" + preferredRoutes;			
			if(!preferredAgencies.equals(""))
				url+="&preferredAgencies=" + preferredAgencies;			
			if(!unpreferredRoutes.equals(""))
				url+="&unpreferredRoutes=" + unpreferredRoutes;	
			if(!unpreferredAgencies.equals(""))
				url+="&unpreferredAgencies=" + unpreferredAgencies;			
			if(!bannedRoutes.equals(""))
				url+="&bannedRoutes=" + bannedRoutes;
			if(!bannedAgencies.equals(""))
				url+="&bannedAgencies=" + bannedAgencies;
			if(!bannedTrips.equals(""))
				url+="&bannedTrips=" + bannedTrips;
			if(!intermediatePlaces.equals("")){
				String[] places = intermediatePlaces.split(";",-1);
				intermediatePlaces = "";
				String waitingTimes = "";
				for(String s: places){
					if(s.contains("::"))
						waitingTimes+= s.split("::")[1];
					else
						waitingTimes+="0";
					waitingTimes+=",";
					intermediatePlaces+=s.split("::")[0] + ";";
				}
				waitingTimes = waitingTimes.substring(0, waitingTimes.length()-1);
				intermediatePlaces = intermediatePlaces.substring(0, intermediatePlaces.length()-1);
				url+="&intermediatePlaces=" + intermediatePlaces + "&intermediatePlacesWaitingTimes=" + waitingTimes + "&intermediatePlacesOrdered=true";
			}
			
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
				response.append(inputLine+ "\n");
			}
			in.close();
			String result = response.toString();
			return result;
		}
		return temp;
	}
	
	
	public String readConfig(String key) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(context.getRealPath("/WEB-INF/config")));
		String line;
		while ((line = br.readLine()) != null) {
		   if(line.contains(key)){
			   br.close();
			   return line.substring(line.indexOf("=")+1);
		   }
		}
		br.close();
		return "";
	}
}
