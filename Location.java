
public class Location {
	private String name;
	private String address;
	private String type;
	private double longitude;
	private double latitude;
	private double distance;
	private boolean favorite = false;
	private float rating = -1;
	private double priority;
	
	/**
	 * Constructs a location
	 * 
	 * @param n
	 * 			String name
	 * @param a
	 * 			String address
	 * @param t
	 * 			String type
	 * @param lon
	 * 			double longitude
	 * @param lat
	 * 			double latitude
	 * @param dist
	 * 			double distance
	 */
	public Location(String n, String a, String t, double lon, double lat, double dist) {
		name = n;
		address = a;
		type = t;
		longitude = lon;
		latitude = lat;
		distance = dist;
		updatePriority();
	}
	
	/**
	 * Gets the value of the property name
	 * @return
	 * 			String name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the value of the property address
	 * @return
	 * 			String address
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Gets the value of the property type
	 * @return
	 * 			String type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Gets the value of the property longitude
	 * @return
	 * 			double longitude
	 */
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * Gets the value of the property latitude
	 * @return
	 * 			double latitude
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * Gets the value of the property distance
	 * @return
	 * 			double distance
	 */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * Gets the value of the property favorite
	 * @return
	 * 			boolean favorite
	 */
	public boolean getFavorite() {
		return favorite;
	}
	
	/**
	 * Gets the value of the property rating
	 * @return
	 * 			float rating
	 */
	public float getRating() {
		return rating;
	}
	
	/**
	 * Gets the value of the property priority
	 * @return
	 * 			double priority
	 */
	public double getPriority() {
		return priority;
	}
	
	/**
	 * Sets the value of the property distance
	 * @param d
	 * 			double distance
	 */
	public void setDistance(double d) {
		distance = d;
		updatePriority();
	}
	
	/**
	 * Sets the value of the property favorite
	 * @param f
	 * 			boolean favorite
	 */
	public void setFavorite(boolean f) {
		favorite = f;
		//updatePriority();
	}
	
	/**
	 * Sets the value of the property rating
	 * @param r
	 * 			float rating
	 */
	public void setRating(float r) {
		rating = r;
		updatePriority();
	}
	
	/**
	 * Updates the value of the property priority
	 */
	private void updatePriority() {
		double ratingCon = 20*(rating - 2);
		int fav = 0;
		if (favorite == true) {
			fav = 30;
		}
		double distCon = 300*(1/distance);
		
		if (distance == -1) {
			if (rating == -1) {
				priority = fav;
			}
			else {
				priority = fav + ratingCon;
			}
		}
		else {
			if (rating == -1) {
				priority = fav + distCon;
			}
			else {
				priority = fav + ratingCon + distCon;
			}
		}
	}
}
