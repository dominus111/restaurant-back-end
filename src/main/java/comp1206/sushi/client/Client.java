package comp1206.sushi.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import comp1206.sushi.common.*;
import comp1206.sushi.server.*;

public class Client implements ClientInterface {

    private static final Logger logger = LogManager.getLogger("Client");

    private Restaurant restaurant;
    Map<Dish, Number> basket = new HashMap<>();
    private List<UpdateListener> updateListeners;
    private List<User> users;
    private User user;
    boolean firstTime = true;
    private List<Postcode> postcodes;
    private List<Dish> dishes;
    private List<Order> orders;
    private Comms comms;
    // private ServerInterface server;

    public Client() {
        comms = new Comms(false);
        logger.info("Starting up client...");
        Postcode restaurantPostcode = new Postcode("SO17 1BJ");
        restaurant = new Restaurant("Mock Restaurant", restaurantPostcode);
        updateListeners = new ArrayList<>();
        users = new ArrayList<>();
        postcodes = new ArrayList<>();
        postcodes = getPostcodes2();
        dishes = new ArrayList<>();
        dishes = getDishes2();
        orders = new ArrayList<>();
    }

    @Override
    public Restaurant getRestaurant() {
        // TODO Auto-generated method stub
        return restaurant;
    }

    @Override
    public String getRestaurantName() {
        // TODO Auto-generated method stub
        return restaurant.getName();
    }

    @Override
    public Postcode getRestaurantPostcode() {
        // TODO Auto-generated method stub
        return restaurant.getLocation();
    }

    @Override
    public User register(String username, String password, String address, Postcode postcode) {
        User user = new User(username, password, address, postcode);
        comms.send("REGISTER;" + username + ";" + password + ";" + address + ";" + postcode.getName() + ";" + postcode.getDistance());
        Object message = comms.recieve();
        while (message == null) {
            message = comms.recieve();
        }
        if (message.equals("SUCCESS;REGISTER;" + username)) {
            return user;
        }
        return null;
    }

    @Override
    public User login(String username, String password) {
        if (firstTime) {
            System.out.println("CLIENT LOGIN " + password);
            user = login2(username, password);
            System.out.println("CLIENT LOGIN " + user.getName());
            firstTime = false;
            return user;
        } else {
            return user;
        }
    }

    public User login2(String username, String password) {
        comms.send("LOGIN;" + username + ";" + password);
        Object message = comms.recieve();
        while (message == null) {
            //System.out.println("IN FIRST LOOP");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            message = comms.recieve();
        }
        if (message.equals("SUCCESS;LOGIN;" + username)) {
            comms.send("GET;USER;" + username);
            message = comms.recieve();
            while (message == null) {
                System.out.println("IN Seocn LOOP");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                message = comms.recieve();
            }
            User loggedin = (User) message;
            return loggedin;
        }
        return null;
    }

    public List<Postcode> getPostcodes2() {
        try {
            String command = "GET;POSTCODES";
            comms.send(command);

            List<Postcode> postcodeList = (ArrayList) comms.recieve();
            while (postcodeList == null) {
                Thread.sleep(200);
                postcodeList = (ArrayList) comms.recieve();
            }
            return postcodeList;
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public List<Postcode> getPostcodes() {
        return postcodes;
    }

    @Override
    public List<Dish> getDishes() {
        return dishes;
    }

    public List<Dish> getDishes2() {
        comms.send("GET;DISHES");
        try {
            List<Dish> dishList = (List<Dish>) comms.recieve();
            while (dishList == null) {
                Thread.sleep(200);
                dishList = (List<Dish>) comms.recieve();
            }
            return dishList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getDishDescription(Dish dish) {
        // TODO Auto-generated method stub
        return dish.getDescription();
    }

    @Override
    public Number getDishPrice(Dish dish) {
        // TODO Auto-generated method stub
        return dish.getPrice();
    }

    @Override
    public Map<Dish, Number> getBasket(User user) {

        return user.getBasket();
    }

    @Override
    public Number getBasketCost(User user) {

        Double totalCost = 0.0;
        Map<Dish, Number> userBasket = user.getBasket();
        Set<Dish> dishSet = userBasket.keySet();
        for (Dish dish : dishSet) {
            totalCost += dish.getPrice().doubleValue() * userBasket.get(dish).doubleValue();
        }
        return totalCost;
    }

    @Override
    public void addDishToBasket(User user, Dish dish, Number quantity) {
        user.addToBasket(dish, quantity);
        this.notifyUpdate();

    }

    @Override
    public void updateDishInBasket(User user, Dish dish, Number quantity) {
        Map<Dish, Number> basket = user.getBasket();
        Iterator<Dish> iterator = basket.keySet().iterator();

        while (iterator.hasNext()) {
            Dish d = iterator.next();
            if (d == dish) {
                if (quantity.intValue() > 0) {
                    basket.replace(dish, quantity);
                } else if (quantity.intValue() == 0) {
                    iterator.remove();
                } else {
                    System.out.println("Cannot use negative values");
                    break;
                }
            }

        }
        user.setBasket(basket);
        this.notifyUpdate();
    }

    @Override
    public Order checkoutBasket(User user) {
        Order order = new Order(user, user.getBasket());
        comms.send("CHECKOUT;");
        Object message = comms.recieve();
        while (message == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            message = comms.recieve();
        }
        if (message.equals("SUCCESS")) {
            System.out.println("SUCESSS");


            user.addToOrders(order);
            clearBasket(user);
            this.notifyUpdate();
            comms.send(order);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return order;
    }

        @Override
        public void clearBasket (User user){
            Map<Dish, Number> basket2 = new HashMap<>();
            user.setBasket(basket2);
            ;
            this.notifyUpdate();

        }

        @Override
        public List<Order> getOrders (User user){
            // TODO Auto-generated method stub
            return user.getOrders();
        }

        @Override
        public boolean isOrderComplete (Order order){
            return order.isComplete();
        }

        @Override
        public String getOrderStatus (Order order){
            // TODO Auto-generated method stub
            return order.getStatus();
        }

        @Override
        public Number getOrderCost (Order order){
            Double totalCost = 0.0;
            Map<Dish, Number> orderedItems = order.getOrderedItems();
            Set<Dish> dishes = orderedItems.keySet();
            for (Dish d : dishes) {
                totalCost += d.getPrice().doubleValue() * orderedItems.get(d).doubleValue();
            }
            return totalCost;
        }

        @Override
        public void cancelOrder (Order order){
            outerLoop:
            for (User u : users) {
                for (Order o : u.getOrders()) {
                    if (o == order) {
                        comms.send("CAN;");
                        Object message = comms.recieve();
                        while (message == null) {
                            message = comms.recieve();
                        }
                        if (message.equals("SUCCESS")) {
                            comms.send(order);
                        }
                        u.removeOrder(o);
                        break outerLoop;
                    } else {
                        System.out.println("Not an existing order");
                        break outerLoop;
                    }
                }
            }
            this.notifyUpdate();

        }

        public void updateLists () {

        }

        @Override
        public void addUpdateListener (UpdateListener listener){
            updateListeners.add(listener);
        }

        @Override
        public void notifyUpdate () {
            this.updateListeners.forEach(listener -> listener.updated(new UpdateEvent()));
        }

    }
