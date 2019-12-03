package com.starwars;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Stateless
@Path("/jdtest")
public class StarWarsAPI extends Application implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger log = Logger.getLogger(StarWarsAPI.class);

	private String SWAPI_API = "https://swapi.co/api/";

	@GET
	@Produces({ "application/json" })
	public List<String> getFilmPeopleNamesBySpecies(@QueryParam("film_id") String filmId,
			@QueryParam("character_id") String characterId) throws Exception {
		List<String> response = new ArrayList<String>();
		try {
			JsonObject films = getBuilder("films", filmId);

			JsonObject species = getBuilder("species", characterId);
			String nameSpecie = species.get("name").getAsString();

			JsonArray peoples = films.getAsJsonArray("characters");
			if (peoples != null) {
				for (JsonElement especie : peoples) {
					
					String peopleURI = especie.getAsString();
					HttpGet httpGet = new HttpGet(peopleURI);
					JsonObject peopleJson = getRequest(httpGet);

					JsonElement speciePeople = peopleJson.get("species");

					String especieURI = speciePeople.getAsString();
					HttpGet httpGet2 = new HttpGet(especieURI);
					JsonObject specieJson = getRequest(httpGet2);
					String namePeopleSpecie = specieJson.get("name").getAsString();

					if (nameSpecie.equals(namePeopleSpecie)) {
						response.add(peopleJson.get("name").getAsString());
					}
				}
			}
			log.debug(response.toString());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		return response;
	}

	public JsonObject getBuilder(String path, String searchquery) throws Exception {
		HttpGet httpGet = new HttpGet(SWAPI_API + path + "/" + searchquery);
		return getRequest(httpGet);
	}

	public JsonObject getRequest(HttpGet request) throws Exception {

		HttpClient httpClient = HttpClientBuilder.create().build();
		request.addHeader("accept", "application/json");
		HttpResponse response = httpClient.execute(request);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		}

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

		String line;
		StringBuilder stringBuilder = new StringBuilder();
		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
		}

		JsonObject jsonObject = deserialize(stringBuilder.toString());
		bufferedReader.close();

		return jsonObject;
	}

	public JsonObject deserialize(String json) {
		Gson gson = new Gson();
		JsonObject jsonClass = gson.fromJson(json, JsonObject.class);
		return jsonClass;
	}
}
