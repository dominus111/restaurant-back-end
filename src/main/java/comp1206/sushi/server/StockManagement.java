package comp1206.sushi.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Drone;
import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Staff;

public class StockManagement implements Serializable{
	private ArrayList<Ingredient> ingredientsBeingStocked;
	private ServerInterface server;
	private List<Dish> dishesBeingRestocked;
	private Thread x;
	private Drone drone;
	private Runnable run;
	private int num;
	private boolean getIn;

	public StockManagement(ServerInterface server) {
		this.server = server;
		ingredientsBeingStocked = new ArrayList<>();
		dishesBeingRestocked = new ArrayList<>();
		getIn = false;
	}

	public synchronized Dish checkDishesForRestock() {
		Iterator<Dish> itr = server.getDishes().iterator();
		while (itr.hasNext()) {
			Dish d = itr.next();
			int stock = getDishStock(d);
			int tresh = d.getRestockThreshold().intValue();
			if (stock < tresh) {
				if (!dishesBeingRestocked.contains(d)) {
					dishesBeingRestocked.add(d);
					return d;
				}
			}
		}
		return null;
	}

	synchronized public void setDishStock(Dish dish, int i) {
		dish.setStock(i);
	}
	synchronized public Integer getDishStock(Dish d) {
		return d.getStock().intValue();
	}

	public void removeDishFromStocking(Dish dish) {
		dishesBeingRestocked.remove(dish);
	}

	public synchronized Ingredient checkIngredientsForRestock() {
		Iterator<Ingredient> itr = server.getIngredients().iterator();
		while (itr.hasNext()) {
			Ingredient i = itr.next();
			int stock = getIngredientStcok(i);
			int tresh = i.getRestockThreshold().intValue();
			if (stock < tresh) {
				if (!ingredientsBeingStocked.contains(i)) {
					ingredientsBeingStocked.add(i);
					return i;
				}
			}
		}
		return null;
	}
	
	synchronized public Integer getIngredientStcok(Ingredient i) {
		return i.getStock().intValue();
	}

	public void removeIngredientFromStocking(Ingredient ing) {
		ingredientsBeingStocked.remove(ing);
	}


	public synchronized void setIngredientStock(Ingredient in, int i) {
		in.setStock(i);
		
	}
}
