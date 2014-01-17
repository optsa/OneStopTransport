package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import models.Agency;

@Path("/Agencies")
public class AgencyResource {

	// vamos utilizar um Map estático para
	// "simular" uma base de dados
	static private Map<Integer, Agency> bandasMap;

	static {
		bandasMap = new HashMap<Integer, Agency>();

		Agency b1 = new Agency();
		b1.setAgency_id(1);
		b1.setAgency_name("Led Zeppelin");

		bandasMap.put(b1.getAgency_id(), b1);

		Agency b2 = new Agency();
		b2.setAgency_id(2);
		b2.setAgency_name("Zeppelin Led");

		bandasMap.put(b2.getAgency_id(), b2);
	}

	@GET
	@Produces("text/xml")
	public List<Agency> getBandas() {
		return new ArrayList<Agency>(bandasMap.values());
	}

	@Path("{id}")
	@GET
	@Produces("text/xml")
	public Agency getBanda(@PathParam("id") int id) {
		return bandasMap.get(id);
	}
}
