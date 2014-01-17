package main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import models.Agency;
import models.Route;
import models.Stop;
import models.Stop_Time;
import models.Trip;

import org.opentripplanner.graph_builder.GraphBuilderMain;

@Path("/Update")
public class Update {
	@javax.ws.rs.core.Context ServletContext context;
	static private Map<Integer, Agency> agencies = new HashMap<Integer, Agency>();
	static private Map<Integer, Route> routes = new HashMap<Integer, Route>();
	static private Map<Integer, Stop> stops = new HashMap<Integer, Stop>();
	static private Map<Integer, Stop_Time> stoptimes = new HashMap<Integer, Stop_Time>();
	static private Map<Integer, Trip> trips = new HashMap<Integer, Trip>();
	private double dataFetchTime = 0;
	private double graphBuildTime = 0;

	@Path("{id}")
	@GET
	@Produces("text/html")
	public String getUpdate(@PathParam("id") String id) throws IOException, NoSuchAlgorithmException, NamingException {
		
		Context env = (Context)new InitialContext().lookup("java:comp/env");
		String keyword = (String)env.lookup("keyword");
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(keyword.getBytes("UTF-8"));
		byte[] digest = md.digest();
		StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<digest.length;i++) {
    		String hex=Integer.toHexString(0xff & digest[i]);
   	     	if(hex.length()==1) hexString.append('0');
   	     	hexString.append(hex);
    	}
		if(id.equals(hexString.toString())){

			agencies.clear();
			routes.clear();
			stops.clear();
			stoptimes.clear();
			trips.clear();

			HttpUrlConnection http = new HttpUrlConnection();

			try {
				long startTime = System.currentTimeMillis();
				http.GetAgencies(agencies);
				http.GetRoutes(routes);
				http.GetStops(stops);
				http.GetTrips(routes.keySet(),trips);
				http.GetStopTimes(routes.keySet(),trips.keySet(),stoptimes);

				long totalTime = System.currentTimeMillis() - startTime;
				dataFetchTime = ((double) totalTime) / 1000.0;

			} catch (Exception e) {
				e.printStackTrace();
			}

			zipFiles();
			long startTime = System.currentTimeMillis();
			GraphBuilderMain.main(new String[]{context.getRealPath("/WEB-INF/graph-builder.xml")});
			long totalTime = System.currentTimeMillis() - startTime;
			graphBuildTime = ((double) totalTime) / 1000.0;
			
			String temp = readConfig("routersURL");
			
			URL url = new URL(temp);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			int responseCode = httpCon.getResponseCode();
			
			System.out.println("\nSending 'PUT' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
			
			return agencies.size() + " agency(ies) imported.<br>" + 
			routes.size() + " route(s) imported.<br>" + 
			stops.size() + " stop(s) imported.<br>" + 
			trips.size() + " trip(s) imported.<br>" + 
			stoptimes.size() + " stop_time(s) imported.<br>" +
			"Data fetched in " + dataFetchTime + " seconds.<br>" +
			"Graph built in " + graphBuildTime + " seconds.";
		}
		return "Wrong id!";
	}

	private void zipFiles() {
		try {
			final int BUFFER = 2048;
			BufferedInputStream origin = null;
			
			FileOutputStream dest = new FileOutputStream(readConfig("gtfsPath"));
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];

			String files[] = new String[6];
			files[0] = "agency.txt";
			files[1] = "calendar.txt";
			files[2] = "routes.txt";
			files[3] = "stop_times.txt";
			files[4] = "stops.txt";
			files[5] = "trips.txt";

			for (int i=0; i<files.length; i++) {
				FileInputStream fi = new FileInputStream(files[i]);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(files[i]);
				out.putNextEntry(entry);
				int count;
				while((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();

				File f = new File(files[i]);
				f.delete();
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
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
