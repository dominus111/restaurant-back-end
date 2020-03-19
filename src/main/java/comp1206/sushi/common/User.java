package comp1206.sushi.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.User;

public class User extends Model  implements Serializable{
	
	private String name;
	private String password;
	private String address;
	private Postcode postcode;
	private Map<Dish, Number> basket;
	private List<Order> orders;

	public User(String username, String password, String address, Postcode postcode) {
		this.name = username;
		this.password = password;
		this.address = address;
		this.postcode = postcode;
		basket = new HashMap<>();
		orders = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getDistance() {
		return postcode.getDistance();
	}

	public Postcode getPostcode() {
		return this.postcode;
	}
	
	public void setPostcode(Postcode postcode) {
		this.postcode = postcode;
	}
	
	public String getPassword() {
		return password;
	}
	
	public Map<Dish, Number> getBasket() {
		return this.basket;
	}
	
	public void setBasket(Map<Dish, Number> basket) {
		this.basket = basket;
	}
	
	public void addToBasket(Dish dish, Number number) {
		basket.put(dish, number);
	}
	
	public List<Order> getOrders() {
		return orders;
	}
	
	public void addToOrders(Order order) {
		orders.add(order);
	}
	
	public void removeOrder(Order order) {
		orders.remove(order);
	}

	public String getAddress() {
		// TODO Auto-generated method stub
		return address;
	}

}
