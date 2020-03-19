package comp1206.sushi.common;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import comp1206.sushi.common.Order;

public class Order extends Model implements Serializable{

	private String status;
	private String name;
	private Map<Dish, Number> orderedItems;
	private User user;
	
	public Order(User user, Map<Dish, Number> orderedItems) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		this.name = dtf.format(now);
		this.orderedItems = orderedItems;
		this.user = user;
		setStatus("Initialize");	
	}

	public Number getDistance() {
		return user.getDistance();
	}

	@Override
	public String getName() {
		return this.name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}
	
	public boolean isComplete() {
		return status.equals("Complete");
	}
	
	public Map<Dish, Number> getOrderedItems() {
		return this.orderedItems;
	}

	public User getUser() {
		// TODO Auto-generated method stub
		return user;
	}
}
