package comp1206.sushi.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import comp1206.sushi.common.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server implements ServerInterface, Serializable {

	private static final Logger logger = LogManager.getLogger("Server");

	public Restaurant restaurant;
	StockManagement management;

	DataPersistence data;
	Comms comms;
	boolean permission;
	public ArrayList<Dish> dishes = new ArrayList<Dish>();
	public ArrayList<Drone> drones = new ArrayList<Drone>();
	public ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
	public ArrayList<Order> orders = new ArrayList<Order>();
	public ArrayList<Staff> staff = new ArrayList<Staff>();
	public ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
	public ArrayList<User> users = new ArrayList<User>();
	public ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
	private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();

	public Server() {
		logger.info("Starting up server...");

		management = new StockManagement(this);
		comms = new Comms(true);
		data = new DataPersistence(this);
		ServerCommnunication serverComm = new ServerCommnunication();
		Runnable r = serverComm;
		Thread comm = new Thread(r);
		comm.start();

		Postcode restaurantPostcode = new Postcode("SO17 1BJ");
		restaurant = new Restaurant("Mock Restaurant", restaurantPostcode);

		Postcode postcode1 = addPostcode("SO17 1TJ");
		Postcode postcode2 = addPostcode("SO17 1BX");
		Postcode postcode3 = addPostcode("SO17 2NJ");
		Postcode postcode4 = addPostcode("SO17 1TW");
		Postcode postcode5 = addPostcode("SO17 2LB");
		Postcode postcode6 = addPostcode("SO17 3SH");


		Supplier supplier1 = addSupplier("Supplier 1", postcode1);
		Supplier supplier2 = addSupplier("Supplier 2", postcode2);
		Supplier supplier3 = addSupplier("Supplier 3", postcode3);

		Ingredient ingredient1 = addIngredient("Ingredient 1", "grams", supplier1, 1, 5, 1);
		Ingredient ingredient2 = addIngredient("Ingredient 2", "grams", supplier2, 1, 5, 1);
		Ingredient ingredient3 = addIngredient("Ingredient 3", "grams", supplier3, 1, 5, 1);

		Dish dish1 = addDish("Dish 1", "Dish 1", 1, 1, 10);
		Dish dish2 = addDish("Dish 2", "Dish 2", 2, 1, 10);
		Dish dish3 = addDish("Dish 3", "Dish 3", 3, 1, 10);

//		orders.add(new Order(null, null));

		addIngredientToDish(dish1, ingredient1, 1);
		addIngredientToDish(dish1, ingredient2, 2);
		addIngredientToDish(dish2, ingredient2, 3);
//		addIngredientToDish(dish2, ingredient3, 1);
//		addIngredientToDish(dish3, ingredient1, 2);
//		addIngredientToDish(dish3, ingredient3, 1);

		addStaff("Staff 1");
//		addStaff("Staff 2");
//		addStaff("Staff 3");

		addDrone(1);
		addDrone(2);
		addDrone(3);

		User user1 = addUser("John","Doe","UK",postcode4);
		for (User u : users) {
			System.out.println(u.getName());
		}


		File f = new File(System.getProperty("user.dir") + "/server");
		f.mkdir();
		File fileToCheck = new File(f + "/ser.txt");
		if (fileToCheck.isFile() && fileToCheck.length() != 0) {
			data.read();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				data.write();
			}
		});
	}

	@Override
	public List<Dish> getDishes() {
		return this.dishes;
	}

	@Override
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		Dish newDish = new Dish(name, description, price, restockThreshold, restockAmount);
		this.dishes.add(newDish);
		this.notifyUpdate();
		return newDish;
	}

	@Override
	public void removeDish(Dish dish) {
		this.dishes.remove(dish);
		this.notifyUpdate();
	}

	@Override
	public Map<Dish, Number> getDishStockLevels() {
		Random random = new Random();
		List<Dish> dishes = getDishes();
		HashMap<Dish, Number> levels = new HashMap<Dish, Number>();
		for (Dish dish : dishes) {
			levels.put(dish, random.nextInt(50));
		}
		return levels;
	}

	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) {

	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {

	}

	@Override
	public void setStock(Dish dish, Number stock) {

	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) {

	}

	@Override
	public List<Ingredient> getIngredients() {
		return this.ingredients;
	}

	@Override
	public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold,
			Number restockAmount, Number weight) {
		Ingredient mockIngredient = new Ingredient(name, unit, supplier, restockThreshold, restockAmount, weight);
		this.ingredients.add(mockIngredient);
		this.notifyUpdate();
		return mockIngredient;
	}

	@Override
	public void removeIngredient(Ingredient ingredient) {
		int index = this.ingredients.indexOf(ingredient);
		this.ingredients.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Supplier> getSuppliers() {
		return this.suppliers;
	}

	@Override
	public Supplier addSupplier(String name, Postcode postcode) {
		Supplier mock = new Supplier(name, postcode);
		this.suppliers.add(mock);
		return mock;
	}

	@Override
	public void removeSupplier(Supplier supplier) {
		int index = this.suppliers.indexOf(supplier);
		this.suppliers.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Drone> getDrones() {
		return this.drones;
	}

	@Override
	public Drone addDrone(Number speed) {
		Drone newDrone = new Drone(speed);
		newDrone.setSource(restaurant.getLocation());
		newDrone.setManagement(management);
		drones.add(newDrone);
		Runnable run = newDrone;
		Thread thr = new Thread(run);
		thr.start();
		notifyUpdate();
		return newDrone;

	}

	@Override
	public void removeDrone(Drone drone) {
		int index = this.drones.indexOf(drone);
		this.drones.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Staff> getStaff() {
		return this.staff;
	}

	@Override
	public Staff addStaff(String name) {
		Staff newStaff = new Staff(name);
		newStaff.setManagement(management);
		staff.add(newStaff);
		Runnable run = newStaff;
		Thread thr = new Thread(run);
		thr.start();
		notifyUpdate();
		return newStaff;

	}

	@Override
	public void removeStaff(Staff staff) {
		this.staff.remove(staff);
		this.notifyUpdate();
	}

	@Override
	public List<Order> getOrders() {
		return this.orders;
	}

	@Override
	public void removeOrder(Order order) {
		int index = this.orders.indexOf(order);
		this.orders.remove(index);
		this.notifyUpdate();
	}

	@Override
	public Number getOrderCost(Order order) {
		Double totalCost = 0.0;
		Map<Dish, Number> orderedItems = order.getOrderedItems();
		Set<Dish> dishes = orderedItems.keySet();
		for (Dish d : dishes) {
			totalCost += d.getPrice().doubleValue() * orderedItems.get(d).doubleValue();
		}
		return totalCost;
	}


	@Override
	public Map<Ingredient, Number> getIngredientStockLevels() {
		Random random = new Random();
		List<Ingredient> dishes = getIngredients();
		HashMap<Ingredient, Number> levels = new HashMap<Ingredient, Number>();
		for (Ingredient ingredient : ingredients) {
			levels.put(ingredient, random.nextInt(50));
		}
		return levels;
	}

	@Override
	public Number getSupplierDistance(Supplier supplier) {
		return supplier.getDistance();
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public Number getOrderDistance(Order order) {
		Order mock = (Order) order;
		return mock.getDistance();
	}

	@Override
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		if (quantity == Integer.valueOf(0)) {
			removeIngredientFromDish(dish, ingredient);
		} else {
			dish.getRecipe().put(ingredient, quantity);
		}
	}

	@Override
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		dish.getRecipe().remove(ingredient);
		this.notifyUpdate();
	}

	@Override
	public Map<Ingredient, Number> getRecipe(Dish dish) {
		return dish.getRecipe();
	}

	@Override
	public List<Postcode> getPostcodes() {
		return this.postcodes;
	}

	@Override
	public Postcode addPostcode(String code) {
		Postcode mock = new Postcode(code, restaurant);
		this.postcodes.add(mock);
		this.notifyUpdate();
		return mock;
	}

	@Override
	public void removePostcode(Postcode postcode) throws UnableToDeleteException {
		this.postcodes.remove(postcode);
		this.notifyUpdate();
	}

	@Override
	public List<User> getUsers() {
		return this.users;
	}

	@Override
	public void removeUser(User user) {
		this.users.remove(user);
		this.notifyUpdate();
	}

	@Override
	public User addUser(String username, String password, String address, Postcode postcode) {
		User user = new User(username, password, address, postcode);
		if (users.contains(user)) {
			return null;
		}
		users.add(user);
		this.notifyUpdate();
		return user;
	}

	@Override
	public void loadConfiguration(String filename) throws FileNotFoundException {
		Configuration configuration = new Configuration(filename);
		List<List<Model>> confList = configuration.parse();
		synchronized (this) {
			for (Drone d : drones) {
				d.stopThreads();
//				while(!d.canStart()) {}
			}
			drones.clear();
			for (Staff st : staff) {
				st.stopThreads();
//				while(!st.canStart()) {}
			}
			staff.clear();
			postcodes.clear();
			for (Model p : confList.get(0)) {
				addPostcode(((Postcode) p).getName());
			}
			suppliers.clear();
			for (Model s : confList.get(2)) {
				suppliers.add((Supplier) s);

			}
			users.clear();
			for (Model u : confList.get(7)) {
				users.add((User) u);
			}
			orders.clear();
			for (Model o : confList.get(6)) {
				orders.add((Order) o);
			}
			ingredients.clear();
			for (Model i : confList.get(1)) {
				ingredients.add((Ingredient) i);
			}
			dishes.clear();
			for (Model d : confList.get(3)) {
				dishes.add((Dish) d);
			}
			for (Model dr : confList.get(4)) {
				((Drone) dr).setManagement(management);
				addDrone(((Drone) dr).getSpeed().intValue());
			}
			for (Model st : confList.get(5)) {
				((Staff) st).setManagement(management);
				addStaff(((Staff) st).getName());

			}
		}

		System.out.println("Loaded configuration: " + filename);
	}

	@Override
	public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		for (Entry<Ingredient, Number> recipeItem : recipe.entrySet()) {
			if (recipeItem.getValue().intValue() > recipeItem.getKey().getStock().intValue()) {
				for (Entry<Ingredient, Number> recipeItem2 : recipe.entrySet()) {
					removeIngredientFromDish(dish, recipeItem2.getKey());
				}
				try {
					throw new Exception("Not enough " + recipeItem.getKey() + " in stock");
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
			addIngredientToDish(dish, recipeItem.getKey(), recipeItem.getValue());
		}
		this.notifyUpdate();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		return true;
	}

	@Override
	public String getOrderStatus(Order order) {
		Random rand = new Random();
		if (rand.nextBoolean()) {
			return "Complete";
		} else {
			return "Pending";
		}
	}

	@Override
	public String getDroneStatus(Drone drone) {
		Random rand = new Random();
		if (rand.nextBoolean()) {
			return "Idle";
		} else {
			return "Flying";
		}
	}

	@Override
	public String getStaffStatus(Staff staff) {
		Random rand = new Random();
		if (rand.nextBoolean()) {
			return "Idle";
		} else {
			return "Working";
		}
	}

	@Override
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
		dish.setRestockThreshold(restockThreshold);
		dish.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		ingredient.setRestockThreshold(restockThreshold);
		ingredient.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public Number getRestockThreshold(Dish dish) {
		return dish.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Dish dish) {
		return dish.getRestockAmount();
	}

	@Override
	public Number getRestockThreshold(Ingredient ingredient) {
		return ingredient.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Ingredient ingredient) {
		return ingredient.getRestockAmount();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void notifyUpdate() {
		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

	@Override
	public Postcode getDroneSource(Drone drone) {
		return drone.getSource();
	}

	@Override
	public Postcode getDroneDestination(Drone drone) {
		return drone.getDestination();
	}

	@Override
	public Number getDroneProgress(Drone drone) {
		return drone.getProgress();
	}

	@Override
	public String getRestaurantName() {
		return restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		return restaurant.getLocation();
	}

	@Override
	public Restaurant getRestaurant() {
		return restaurant;
	}

	public boolean getPermission() {
		return permission;
	}

	public void setPermission(boolean permission) {
		this.permission = permission;
	}

	class ServerCommnunication implements Runnable {

		public void run() {
			while (true) {
				//System.out.println("TRUE...");
				
				
//				Comms comms2 = null;
//				while (comms2 == null) {
//					System.out.println("waiting for message");
//					try {
//						Thread.sleep(3000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					System.out.println("waiting for message");
//					comms2 = (Comms) comms.recieve();
//				}
//				comms2.setC(true);
				
				String message = null;
				while (message == null) {

					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					message = (String) comms.recieve();
					//System.out.println("SERVER RECIEVES " + message);
				}
				String[] fragment = message.split(";");
				
				
				switch (fragment[0]) {
				case ("GET"):
					switch (fragment[1]) {
					case ("USER"):
						User toSend = null;
						for (User u : users) {
							if (u.getName().equals(fragment[2])) {
								System.out.println(u.getName());
								toSend = u;
								break;
							}
						}
						if (toSend != null) {
							comms.send(toSend);
						}
						break;
					case ("POSTCODES"):
						comms.send(postcodes);
						break;
					case ("DISHES"):
						comms.send(dishes);
						break;
					}
					break;
				case ("REGISTER"):
					Postcode po = null;
					for (Postcode p : postcodes) {
						if (p.getName().equals(fragment[4])) {
							po = p;
							break;
						}
					}
					User user = addUser(fragment[1], fragment[2], fragment[3], po);
					if (user != null) {
						comms.send("SUCCESS;REGISTER;" + user.getName());
					} else {
						comms.send("UNSUCCESSFUL");
					}
					break;
				case ("LOGIN"):
					boolean found = false;
					System.out.println("START LOOKING");
					for (User u : users) {
						System.out.println("User 1 "+ u.getName());
						if (u.getName().equals(fragment[1]) && u.getPassword().equals(fragment[2])) {
							System.out.println("IDENTITY CONFIRMED");
							comms.send("SUCCESS;LOGIN;" + u);
							break;
						} else {
							comms.send("UNSUCCESSFUL");
						}
					}
					break;
				case ("CHECKOUT"):
					System.out.println("CHECKOUT");
					comms.send("SUCCESS");
					Order o = null;
					while(o == null) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						//System.out.println("MESSAGE IS "+ comms.recieve());
						o = (Order) comms.recieve();
					}
					o.setStatus("CHECKOUT");
					orders.add(o);
					System.out.println("ORDER NAME " + o.getName());
					//comms.send(o);
					break;
				case ("CANCEL"):
					comms.send("SUCCESS");
					Order o1 = (Order) comms.recieve();
					while(o1 == null) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						o1 = (Order) comms.recieve();
					}
					orders.remove(o1);
					break;
				}
			}
		}
	}


}
