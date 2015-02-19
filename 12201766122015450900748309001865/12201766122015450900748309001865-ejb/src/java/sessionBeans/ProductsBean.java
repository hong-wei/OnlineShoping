/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sessionBeans;

import entityClasses.Products;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Administrator
 */
@Stateless
public class ProductsBean implements ProductsBeanLocal {
    @PersistenceContext(unitName = "12201766122015450900748309001865-ejbPU")
    private EntityManager em;

    public void persist(Object object) {
        em.persist(object);
    }
    
    /**
     * returns all the product in the database
     * @return   List<Products>
     */
    public List<Products> getAllProducts()
    {
        Query q = em.createNamedQuery("Products.findAll");
        return q.getResultList();
}

    /**
     * create and add new product into the database.
     * @param name
     * @param price
     * @param quantity
     * @return 0 for failure other greater than 0 for the new product Id
     */
    public int addNewProduct(String name, double price, int quantity) {
        
        //check if the same product exists in the database.
        Products newProduct = this.getProductByName(name);
        if (newProduct != null)
        {
            return 0;
        }
        //get the highest product Id from the database.
        int id = (Integer) em.createNamedQuery("Products.getHighestId").getSingleResult();
        id++;
        //create new object of the Products entity class.
        newProduct = new Products(id);
        //set the other attribute through entity class methods.
        newProduct.setName(name);
        newProduct.setPrice(price);
        newProduct.setQuantity(quantity);
        newProduct.setComment("");
        //make the new object persistent.
        persist(newProduct);
        //return the id of the newly created Products objest.
        return id;
    }
    
    public void removeProductById(int id)
    {
        Query q = em.createNamedQuery("Products.removeById");
        q.setParameter("id", id);
        //call the named query to fire update in the database of deletion.
        try {
            q.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error deleting product");
        }
    }
    
    /**
    * returns the products of the specified name.
    * @param name
    * @return 
    */
    public Products getProductByName(String name)
    {
        //create a named query, check the database for the product and return the product of the name.
        try {
            return (Products) em.createNamedQuery("Products.findByName").setParameter("name", name).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
   /**
    * returns the product of the id number specified.
    * @param id
    * @return 
    */
    public Products getProductById(int id)
    {
        //create a named query, check the database for the product and return the product of the id.
        try {
            return (Products) em.createNamedQuery("Products.findById").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * set the quantity of the product specified by the product id.
     * @param id id number of the product
     * @param quantity new quantity of the product
     * called by the administrator
     */
    public void setProductQuantityById(long id, int quantity)
    {
        //set the quantity of the product by the id specified.
        Query query = em.createNamedQuery("Products.setQuantitybyId");
        query.setParameter("id", id);
        query.setParameter("newQuantity", quantity);
        try {
            //if the id is invalid an exception maybe thrown.
            query.executeUpdate();
        } catch (Exception e) {
        }
    }
    
    /**
     * increase the product amount
     * @param id
     * @param increment
     * called by the customers
     */
    public boolean increaseProductQuantity(int id, int increment)
    {
        //create an instance of an entity class to hold the database row.
        if (increment < 0)
        {
            increment = 0;
        }
        Products p = this.getProductById(id);
        if (p == null)
        {
            return false;
        }
        int currentQuantity = p.getQuantity();
        currentQuantity += increment;
        //update the new amount of the product.
        setProductQuantityById(id, currentQuantity);
        return true;
    }
    
    /**
     * decrease the product by the given amount.
     * @param id
     * @param decrement
     * called by the customers
     */
    public boolean decreaseProductQuantity(int id, int decrement)
    {
        //create an instance of an entity class to hold the database row.
        if (decrement < 0)
        {
            decrement = 0;
        }
        Products p = this.getProductById(id);
        if (p == null)
        {
            return false;
        }
        int currentQuantity = p.getQuantity();
        currentQuantity -= decrement;
        //update the new amount of the product.
        setProductQuantityById(id, currentQuantity);
        return true;
    }

    /**
     * append the comment of a product with the given id number.
     * @param id 
     */
    public void addProductCommentById(int id, String newComment) {
        //get the original product comment.
        Query q = em.createNamedQuery("Products.findById");
        q.setParameter("id", id);
        Products product = (Products) q.getSingleResult();
        //read out the previous comment and form the new comment.
        newComment = product.getComment() + newComment;
        q = em.createNamedQuery("Products.setCommentById");
        q.setParameter("id", id);
        q.setParameter("newComment", newComment);
        //store the new comment into the database.
        try {
            q.executeUpdate();
        } catch (Exception e) {
        }
    }
    
    
}
