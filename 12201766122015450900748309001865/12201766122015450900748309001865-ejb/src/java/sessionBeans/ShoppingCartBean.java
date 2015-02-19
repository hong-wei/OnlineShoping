/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sessionBeans;

import entityClasses.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;

/**
 *
 * @author Administrator
 */
@Stateful(mappedName="cart")
@StatefulTimeout(unit = TimeUnit.MINUTES, value = 30)
public class ShoppingCartBean implements ShoppingCartBeanLocal {
    @EJB
    private ProductsBeanLocal productsBean;
   
    //create a hashmap
    //represnent the number of each items in the shopping cart.
    //HaspMap<type of keys maintained, type of index>
    private HashMap<Integer, Integer> items = new HashMap<Integer, Integer>();
    
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    /**
     * get the quantity of a product in the shopping cart with the given id
     * @param id
     * @return quantity of the product in the cart.
     */
    public int getQuantityById(int id)
    {
        if(items.get(id) == null)
        {
            return 0;
        }
        else
        {
            return items.get(id);
        }
    
    }
    
    /**
     * add the item with id into the "shopping cart" which is a stateful session bean.
     * @param id id number of the item.
     * @param quantity quantity of the item that would like to be added into the shopping cart.
     * @return number of the items in the cart.
     */
    @Override
    public int addItem(int id, int quantity)
    {
        // get the current number of the item in the shopping cart.
        Integer currentQuantity = items.get(id);
        if (currentQuantity == null){
            currentQuantity = 0;
        }
        //increse the number of the item by the number in the cart.
        currentQuantity += quantity;
        int stock = productsBean.getProductById(id).getQuantity();
        //compare the quantity of a item in the cart with the quantity in the database.
        if (currentQuantity > stock)
        {
            currentQuantity = stock;
        }
        items.put(id, currentQuantity);
        return currentQuantity;
    }
    
    /**
     * decrease the quantity of the item with id in the 
     * "shopping cart" which is a stateful session bean.
     * @param id id number of the item.
     * @param quantity quantity of the item that would like to be removed from the shopping cart.
     * @return quantity of a product remaining in the shopping cart.
     */    
    @Override
    public int removeItem(int id, int quantity)
    {
        //obtain the current number of a item in the cart.
        Integer currentQuantity = items.get(id);
        if (currentQuantity == null)
        {
            currentQuantity = 0;
        }
        currentQuantity -= quantity;
        if (currentQuantity <= 0)
        {
            //remove the item from the cart.
            items.remove(id);
            return 0;
        }
        else
        {
            items.put(id, currentQuantity);
            return currentQuantity;
        }
    }
    
    /**
     * checkout the customer and fire the changes in the database.
     * @return
     */
    @Remove
    public String checkout()
    {
        ///////////////////////////////////
        //adjust the quantity of items in the database correspondingly.
        //the quantity of items in the cart should be less than in the database.
        //////////////////////////////////
        for (Integer id : items.keySet())
        {
            int quantityInCart = items.get(id);
            int stock = productsBean.getProductById(id).getQuantity();
            productsBean.getProductById(id).setQuantity(stock - quantityInCart);
        }
            
        String message = getItemList() + "<b>Thanks for shopping in our store!</b><br>";
        return message;
    }
    
    @Remove
    public String cancel()
    {
        // no action required - annotation @Remove indicates that
        // calling this method should remove the EJB which will
        // automatically destory instance variables.
        return "<b>Thanks for coming into our store!</b><br>";
    }
    
    /**
     * print out the items list currently in the shopping cart
     * @return 
     */
    public String getItemList()
    {
        String message = "Products in your shopping cart: <br>";
        Set<Integer> keys = items.keySet();
        Iterator<Integer> it = keys.iterator();
        String productName;
        while (it.hasNext())
        {
            int id = it.next();
            productName = productsBean.getProductById(id).getName();
            message += productName + " ---- quantity: " + items.get(id) + "<br>";
        }
        return message;
    }

    /**
     * get the total price of the products in the shopping cart.
     * @return 
     */
    public String getTotalPrice() {
        double totalPrice = 0.0;
        Products tempProduct;
        Set<Integer> keys = items.keySet();
        Iterator<Integer> it = keys.iterator();

        while (it.hasNext())
        {
            int id = it.next();
            tempProduct = productsBean.getProductById(id);
            totalPrice += tempProduct.getPrice() * items.get(id);
        }
        return "Total price: " + totalPrice + " Euros.<br>";
    }
}
