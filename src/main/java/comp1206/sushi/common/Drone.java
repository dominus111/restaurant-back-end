package comp1206.sushi.common;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.Timer;

import comp1206.sushi.common.Drone;
import comp1206.sushi.server.StockManagement;

public class Drone extends Model implements Runnable, Serializable {

	private Number speed;
	private Number progress;

	private Number capacity;
	private Number battery;

	private String status;

	private Postcode source;
	private Postcode destination;
	Ingredient ingr;
	boolean shouldRun;
	Thread x = null;
	boolean canStart;
	StockManagement management;
	boolean b;

	public Drone(Number speed) {
		this.setSpeed(speed);
		this.setCapacity(1);
		setStatus("Idle");
		this.setBattery(100);
		canStart = false;
		shouldRun = true;
		setSpeed(100);
	}

	public Number getSpeed() {
		return speed;
	}

	public Ingredient getIngredient() {
		return ingr;
	}

	private void setIngredient() {
		ingr = null;
	}

	public synchronized Number getProgress() {
		return progress;
	}

	public synchronized void setProgress(Number progress) {
		this.progress = progress;
	}

	public void setSpeed(Number speed) {
		this.speed = speed;
	}

	@Override
	public String getName() {
		return "Drone (" + getSpeed() + " speed)";
	}

	public Postcode getSource() {
		return source;
	}

	public void setSource(Postcode source) {
		this.source = source;
	}

	public Postcode getDestination() {
		return destination;
	}

	public void setDestination(Postcode destination) {
		this.destination = destination;
	}

	public Number getCapacity() {
		return capacity;
	}

	public void setCapacity(Number capacity) {
		this.capacity = capacity;
	}

	public Number getBattery() {
		return battery;
	}

	public void setBattery(Number battery) {
		this.battery = battery;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status", this.status, status);
		this.status = status;
	}

	public void stopThreads() {
		this.shouldRun = false;
	}

	public boolean canStart() {
		return canStart;
	}

	public void setManagement(StockManagement management) {
		this.management = management;
	}

	public void restockIngredient(Ingredient i) {
		this.ingr = i;
	}

	@Override
	public void run() {
		outer: while (shouldRun) {
			this.setStatus("Idle");
			setProgress(null);
			while (this.ingr == null) {
				if (shouldRun) {
					ingr = management.checkIngredientsForRestock();
					if (getBattery().floatValue() < 100) {
						setBattery((getBattery().floatValue() + 0.00001f));
					} else {
						setBattery(100);
					}
				}
			}
			try {
				if(getBattery().floatValue() < 90) {
				setBattery((getBattery().floatValue() + 10f));
				}
				while (ingr.getStock().intValue() < ingr.getRestockThreshold().intValue()) {
					if (!shouldRun) {
						break outer;
					}
					setDestination(ingr.getSupplier().getPostcode());
					long timeinSec = (2 * getDestination().getDistance().longValue() / getSpeed().longValue()) * 1000;
					if(getBattery().floatValue() < timeinSec/1000) {
						management.removeIngredientFromStocking(ingr);
						this.ingr = null;
						break outer;
					}
					this.setStatus("Restocking ingredient " + ingr.getName());
					long waitingTime = timeinSec / 100;
					for (int i = 1; i <= 100; i++) {
						if (!shouldRun) {
							break outer;
						}
						try {						
							setBattery(getBattery().floatValue() - ((float) timeinSec) / 100000);
							Thread.sleep(waitingTime);
							setProgress(i);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					management.setIngredientStock(ingr,
							(ingr.getStock().intValue() + ingr.getRestockAmount().intValue()));
				}
				management.removeIngredientFromStocking(ingr);
				this.ingr = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!shouldRun) {
			canStart = true;
		}
	}

}
