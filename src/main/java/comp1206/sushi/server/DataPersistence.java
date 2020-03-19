package comp1206.sushi.server;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import comp1206.sushi.common.*;

public class DataPersistence {
	private File persistentFile;
	private List<Supplier> supplierList;
	private List<Ingredient> ingredientList;
	private List<Dish> dishList;
	private List<User> userList;
	private List<Postcode> postcodeList;
	private List<Staff> staffList;
	private List<Drone> droneList;
	private List<Order> orderList;
	private Server server;

	public DataPersistence(Server server) {
		File f = new File(System.getProperty("user.dir") + "/server");
		f.mkdir();
		persistentFile = new File(f + "/ser.txt");
		supplierList = new ArrayList<>();
		ingredientList = new ArrayList<>();
		droneList = new ArrayList<>();
		dishList = new ArrayList<>();
		userList = new ArrayList<>();
		postcodeList = new ArrayList<>();
		staffList = new ArrayList<>();
		orderList = new ArrayList<>();
		this.server = server;
	}

	// writes a persistence file in the same manner as the config files are written
	// in
	public void write(){
        try {
        	persistentFile.setWritable(true);
            if (!persistentFile.createNewFile()) {
                boolean deleted = persistentFile.delete();
                boolean created = persistentFile.createNewFile();
                System.out.println(deleted + " "+created+" "+persistentFile.getAbsolutePath());
            }
            updateLists();
            PrintWriter out = new PrintWriter(new FileWriter(persistentFile, true));
            Restaurant r = server.getRestaurant();
            String restPost = "POSTCODE:"+ r.getLocation().getName();
            out.println(restPost);
            String rest = "RESTAURANT:"+r.getName()+":"+r.getLocation().getName();
            out.println(rest);
            for(Postcode p: postcodeList){
            	if(p.getName().equals(r.getLocation().getName())) {
            		
            	} else {
                String toWrite = "POSTCODE:"+p.getName();
                out.println(toWrite);
            	}
            }
            for(Supplier s: supplierList){
                String toWrite = "SUPPLIER:"+s.getName()+":"+s.getPostcode();
                out.println(toWrite);
            }
            for(Ingredient i: ingredientList){
                String toWrite = "INGREDIENT:"+i.getName()+":"+i.getUnit()+":"+i.getSupplier().getName()+":"+i.getRestockThreshold()+":"+i.getRestockAmount()+":"+i.getWeight();
                out.println(toWrite);
//                String stock = "STOCK:"+i.getName()+":"+i.getStock();
//                out.println(stock);
            }
            for(Dish d: dishList){
                String toWrite = "DISH:"+d.getName()+":"+d.getDescription()+":"+d.getPrice()+":"
                        +d.getRestockThreshold()+":"+d.getRestockAmount();
                for(Ingredient i: d.getRecipe().keySet()){
                    toWrite+=":"+d.getRecipe().get(i)+" * "+i.getName();
                }
                out.println(toWrite);
//                String stock = "STOCK:"+d.getName()+":"+d.getStock();
//                out.println(stock);
            }
            for(User u: userList){
                String toWrite = "USER:"+u.getName()+":"+u.getPassword()+":"+u.getAddress()+":"+u.getPostcode().getName();
                out.println(toWrite);
            }
            for(Staff s: staffList){
                String toWrite = "STAFF:"+s.getName();
                out.println(toWrite);
            }
            for(Drone d: droneList){
                String toWrite = "DRONE:"+d.getSpeed();
                out.println(toWrite);
            }
            for(Order o: orderList){
                String toWrite = "ORDER:"+o.getUser().getName() + ":";
                for(Entry<Dish, Number> x : o.getOrderedItems().entrySet()){
                    toWrite+= x.getValue() + " * "+x.getKey().getName() + ",";
                }
                out.println(toWrite);
            }
            out.close();
            Thread.sleep(5000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

	// update the lists prior to writing
	public void updateLists() {
		supplierList = server.getSuppliers();
		ingredientList = server.getIngredients();
		dishList = server.getDishes();
		userList = server.getUsers();
		postcodeList = server.getPostcodes();
		staffList = server.getStaff();
		droneList = server.getDrones();
		orderList = server.getOrders();
	}

	// read uses loadConfuration as the config and persistent files are in the same
	// format
	public void read() {
		try {
			// stopping threaded applications to ensure not a multitude of hanging threads
			// when new read made
			for (Staff s : staffList) {
				s.stopThreads();
			}
			for (Drone d : droneList) {
				d.stopThreads();
			}
			server.loadConfiguration(String.valueOf(persistentFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
