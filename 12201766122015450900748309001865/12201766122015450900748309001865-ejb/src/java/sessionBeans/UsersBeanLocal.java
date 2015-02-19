/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sessionBeans;

import entityClasses.Users;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Administrator
 */
@Local
public interface UsersBeanLocal {
    public List<Users> showAllUsers();
    public Users getUserByName(String name);
    public Users getUserById(String Id);
    public boolean createNewCustomer(String userName, String password);
     
}
