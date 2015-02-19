/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBeans;

import entityClasses.Products;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Administrator
 */
@Local
public interface ProductsBeanLocal {

    public List<Products> getAllProducts();

    public int addNewProduct(String name, double price, int quantity);

    public void removeProductById(int id);

    public Products getProductByName(String name);

    public Products getProductById(int id);

    public void setProductQuantityById(long id, int quantity);

    public boolean increaseProductQuantity(int id, int increment);

    public boolean decreaseProductQuantity(int id, int decrement);

    public void addProductCommentById(int id, String newComment);

}
