package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// This class is the model object of this project. It helps keep only useful information from TicketMaster API JSON response.
// It also helps to store event info into database as well as retrieving them from database.
public class Item {
	private String itemId;
	private String name;
	private double rating;
	private String address;
	private Set<String> categories;
	private String imageUrl;
	private String url;
	private double distance;


	/**
	 * private CONSTRUCTOR for Item class.
	 * This is a builder pattern in Java.
	 */
	private Item(ItemBuilder builder) {
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.rating = builder.rating;
		this.address = builder.address;
		this.categories = builder.categories;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
		this.distance = builder.distance;
	}


	/**
	 * Getters for outside world to access Item.
	 */
	public String getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public double getRating() {
		return rating;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	public double getDistance() {
		return distance;
	}


	/**
	 * Static nested class that serves to construct Item (builder pattern).
	 */
	// MUST BE static b/c: otherwise we need to have a Item instance to access ItemBuilder, but we need ItemBuilder to create Item. It's a dead circle.
	// MUST BE a nested class b/c: if not, in ItemBuilder class, the constructor of Item can't be called there.
	public static class ItemBuilder {  
		private String itemId;
		private String name;
		private double rating;
		private String address;
		private Set<String> categories;
		private String imageUrl;
		private String url;
		private double distance;

		public void setItemId(String itemId) {
			this.itemId = itemId;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setRating(double rating) {
			this.rating = rating;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public void setCategories(Set<String> categories) {
			this.categories = categories;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}

		public Item build() {
			return new Item(this);  // call the constructor (above) of Item class.
		}
	}


	// -------------------------------------- other utility methods -------------------------------------------
	/**
	 * Method that converts our Item object to JSON object to pass to the front end.
	 * b/c: front-end only understand JSON not our Java class.
	 */
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("item_id", itemId);
			obj.put("name", name);
			obj.put("rating", rating);
			obj.put("address", address);
			obj.put("categories", new JSONArray(categories));
			obj.put("image_url", imageUrl);
			obj.put("url", url);
			obj.put("distance", distance);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
