package comp1206.sushi.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.*;

public class Configuration implements Serializable {

	private BufferedReader confReader;
	private Restaurant restaurant = null;
	private List<Model> postcodesList;
	private List<Model> suppliersList;
	private List<Model> ingredientsList;
	private List<Model> dishesList;
	private List<Model> usersList;
	private List<Model> ordersList;
	private List<Model> staffList;
	private List<Model> dronesList;

	public Configuration(String fileName) throws FileNotFoundException {
		this.confReader = new BufferedReader(new FileReader(fileName));
		postcodesList = new ArrayList<>();
		suppliersList = new ArrayList<>();
		ingredientsList = new ArrayList<>();
		dishesList = new ArrayList<>();
		usersList = new ArrayList<>();
		ordersList = new ArrayList<>();
		staffList = new ArrayList<>();
		dronesList = new ArrayList<>();
	}

	public List<List<Model>> parse() {
		List<List<Model>> confList = new ArrayList<>();
		try {
			while (confReader.ready()) {
				String line = confReader.readLine();
				String[] fragment = line.split(":");
				switch (fragment[0]) {
				case "POSTCODE":
//					if (restaurant != null) {
//						Postcode p = new Postcode(fragment[1], restaurant);
//						postcodesList.add(p);
//					} else {
						Postcode p = new Postcode(fragment[1]);
						postcodesList.add(p);
					//}
					break;
				case "SUPPLIER":
					Postcode postcode = null;
					for (Model p1 : postcodesList) {
						if (p1.getName().equals(fragment[2])) {
							postcode = (Postcode) p1;
							break;
						}
					}
					if (postcode.getName() == null) {
						throw new Exception("Configuration Error: Postcode " + fragment[2] + " could not be found");
					} else {
						Supplier s = new Supplier(fragment[1], postcode);
						suppliersList.add(s);
					}
					break;
				case "INGREDIENT":
					Supplier supplier = null;
					for (Model s1 : suppliersList) {
						if (s1.getName().equals(fragment[3])) {
							supplier = (Supplier) s1;
							break;
						}
					}
					if (supplier.getName() == null) {
						throw new Exception("Configuration Error: Supplier " + fragment[3] + " could not be found");
					} else {
						Ingredient i = new Ingredient(fragment[1], fragment[2], supplier, Integer.parseInt(fragment[4]),
								Integer.parseInt(fragment[5]), Integer.parseInt(fragment[6]));
						ingredientsList.add(i);
					}
					break;
				case "DISH":
					Map<Ingredient, Number> recipe = new HashMap<>();
					if (fragment.length > 6) {
						String[] recipeItem = fragment[6].split(",");
						Ingredient ingredient = null;
						for (String r : recipeItem) {
							String[] recipeIngredient = r.split(" \\* ");
							for (Model i1 : ingredientsList) {
								if (i1.getName().equals(recipeIngredient[1])) {
									ingredient = (Ingredient) i1;
									break;
								}
							}
							if (ingredient.getName() == null) {
								throw new Exception("Configuration Error: Ingredient " + recipeIngredient[1]
										+ " could not be found");
							} else {
								recipe.put(ingredient, Integer.parseInt(recipeIngredient[0]));
							}
						}
					}
					Dish d = new Dish(fragment[1], fragment[2], Integer.parseInt(fragment[3]),
							Integer.parseInt(fragment[4]), Integer.parseInt(fragment[5]));
					dishesList.add(d);
					d.setRecipe(recipe);
					break;
				case "RESTAURANT":
					Postcode postcode1 = null;
					for (Model p2 : postcodesList) {
						if (p2.getName().equals(fragment[2])) {
							postcode1 = (Postcode) p2;
							break;
						}
					}
					if (postcode1.getName() == null) {
						throw new Exception("Configuration Error: Postcode " + fragment[2] + " could not be found");
					} else {
						restaurant = new Restaurant(fragment[1], postcode1);
					}
					break;
				case "STOCK":
					for (Model dish : dishesList) {
						if (dish.getName().equals(fragment[1])) {
							((Dish) dish).setStock(Integer.parseInt(fragment[2]));
							break;
						}
					}
					for (Model ingredient1 : ingredientsList) {
						if (ingredient1.getName().equals(fragment[1])) {
							((Ingredient) ingredient1).setStock(Integer.parseInt(fragment[2]));
							break;
						}
					}
					break;
				case "USER":
					Postcode postcode2 = null;
					for (Model p3 : postcodesList) {
						if (p3.getName().equals(fragment[4])) {
							postcode2 = (Postcode) p3;
							break;
						}
					}
					User user = null;
					if (postcode2.getName() == null) {
						user = new User(fragment[1], fragment[2], fragment[3], new Postcode(fragment[4]));
						postcodesList.add(postcode2);
					} else {
						user = new User(fragment[1], fragment[2], fragment[3], postcode2);
					}
					usersList.add(user);
					break;

				case "STAFF":
					Staff staff = new Staff(fragment[1]);
					staffList.add(staff);
					break;
				case "ORDER":
					User user1 = null;
					for (Model u : usersList) {
						if (u.getName().equals(fragment[1])) {
							user1 = (User) u;
							break;
						}
					}
					Map<Dish, Number> orderedItems = new HashMap<>();
					if (fragment.length > 2) {
						String[] dishItems = fragment[2].split(",");
						for (String dishItem : dishItems) {
							String[] dish = dishItem.split(" \\* ");
							for (Model d1 : dishesList) {
								if (d1.getName().equals(dish[1])) {
									orderedItems.put((Dish) d1, Integer.parseInt(dish[0]));
									break;
								}
							}
						}
					}
					if (user1.getName() == null) {
						throw new Exception("Configuration Error: User " + fragment[1] + " could not be found");
					} else {
						Order order = new Order(user1, orderedItems);
						order.setStatus("CHECKOUT");
						ordersList.add(order);
					}
					break;
				case "DRONE":
					Drone drone = new Drone(Double.parseDouble(fragment[1]));
					dronesList.add(drone);
					break;

				}
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
		confList.add(postcodesList);
		confList.add(ingredientsList);
		confList.add(suppliersList);
		confList.add(dishesList);
		confList.add(dronesList);
		confList.add(staffList);
		confList.add(ordersList);
		confList.add(usersList);
		return confList;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}
}
