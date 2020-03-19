package comp1206.sushi.common;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import comp1206.sushi.common.Staff;
import comp1206.sushi.server.StockManagement;

public class Staff extends Model implements Runnable, Serializable {

	private String name;
	private String status;
	private Number fatigue;
	private boolean shouldRun;
	private StockManagement management;
	private Dish dish;
	private static final long MIN_VALUE = 2000;
	private static final long MAX_VALUE = 6000;
	private boolean canStart;

	public Staff(String name) {
		this.setName(name);
		this.setFatigue(0);
		this.setStatus("Initializing");
		shouldRun = true;
		canStart = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getFatigue() {
		return fatigue;
	}

	public void setFatigue(Number fatigue) {
		this.fatigue = fatigue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status", this.status, status);
		this.status = status;
	}

	public void setManagement(StockManagement management) {
		this.management = management;
	}

	public void restockDish(Dish d) {
		this.dish = d;
	}

	public void stopThreads() {
		this.shouldRun = false;
	}

	public boolean canStart() {
		return canStart;
	}

	@Override
	public void run() {
		outer: while (shouldRun) {
			this.setStatus("Idle");
			while (this.dish == null) {
				if (shouldRun) {
					dish = management.checkDishesForRestock();
				}
			}
			try {
				while (dish.getStock().intValue() < dish.getRestockThreshold().intValue()) {
					this.setStatus("Prepareing " + dish.getName());
					for (Ingredient i : dish.getRecipe().keySet()) {
						while (i.getStock().intValue() < dish.getRecipe().get(i).intValue()) {
							if (!shouldRun) {
								break outer;
							}
						}
					}
					try {
						Thread.sleep(ThreadLocalRandom.current().nextLong(MIN_VALUE + (MAX_VALUE + 1)));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (!shouldRun) {
						break outer;
					}
					management.setDishStock(dish, (dish.getStock().intValue() + dish.getRestockAmount().intValue()));
					for (Ingredient in : dish.getRecipe().keySet()) {
						management.setIngredientStock(in,
								in.getStock().intValue() - dish.getRecipe().get(in).intValue());
					}
				}
				management.removeDishFromStocking(dish);
				this.dish = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!shouldRun) {
			canStart = true;
		}
	}
}
