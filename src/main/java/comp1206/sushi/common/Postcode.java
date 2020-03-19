package comp1206.sushi.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import comp1206.sushi.common.Postcode;

public class Postcode extends Model implements Serializable{

	private String name;
	private Map<String,Double> latLong;
	private Number distance;

	public Postcode(String code) {
		this.name = code;
		calculateLatLong();
		this.distance = Integer.valueOf(0);
	}
	
	public Postcode(String code, Restaurant restaurant) {
		this.name = code;
		calculateLatLong();
		calculateDistance(restaurant);
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Number getDistance() {
		return this.distance;
	}

	public Map<String,Double> getLatLong() {
		return this.latLong;
	}
	
	protected void calculateDistance(Restaurant restaurant) {
		//This function needs implementing
		Postcode destination = restaurant.getLocation();
		double lat1 = destination.getLatLong().get("lat");
		double lon1 = destination.getLatLong().get("lon");
		double lat2 = this.getLatLong().get("lat");
		double lon2 = this.getLatLong().get("lon");
		String unit = "M";

		if ((lat1 == lat2) && (lon1 == lon2)) {
			this.distance = 0;
		} else {
			double theta = lon1 - lon2;
			double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
					+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			if (unit == "K") {
				dist = dist * 1.609344;
			} else if (unit == "N") {
				dist = dist * 0.8684;
			} else if (unit == "M") {
				dist = dist * 1609.34;
			}
			this.distance = (int) Math.round(dist);
		}
	}
	
	protected void calculateLatLong() {
		// This function needs implementing
				this.latLong = new HashMap<String, Double>();

				String[] split2 = name.split(" ");
				String firstPart = split2[0];
				String secondPart = split2[1];

				StringBuilder content = new StringBuilder();

				// many of these calls can throw exceptions, so i've just
				// wrapped them all in one try/catch statement.
				try {
					// create a url object
					URL url = new URL("https://api.postcodes.io/postcodes/" + firstPart + "%20" + secondPart);

					// create a urlconnection object
					URLConnection urlConnection = url.openConnection();

					// wrap the urlconnection in a bufferedreader
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

					String line;

					// read from the urlconnection via the bufferedreader
					while ((line = bufferedReader.readLine()) != null) {
						content.append(line + "\n");
					}
					bufferedReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				String output = content.toString();

				String[] split = output.split("\"");
				Double lat = null;
				Double lon = null;
				lat = Double.valueOf((split[26]).substring(1, (split[26]).length() - 1));
				lon = Double.valueOf((split[24]).substring(1, (split[24]).length() - 1));

				latLong.put("lat", lat);
				latLong.put("lon", lon);

				this.distance = new Integer(0);
	}
	
}
