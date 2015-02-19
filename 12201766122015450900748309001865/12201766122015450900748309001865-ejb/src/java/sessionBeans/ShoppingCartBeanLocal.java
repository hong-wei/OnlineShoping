/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sessionBeans;

import javax.ejb.Local;

/**
 *
 * @author Administrator
 */
@Local
public interface ShoppingCartBeanLocal {
    
    public int getQuantityById(int id);

    public int addItem(int id, int quantity);

    public int removeItem(int id, int quantity);

    public String checkout();

    public String cancel();

    public String getItemList();

    public String getTotalPrice();
}
