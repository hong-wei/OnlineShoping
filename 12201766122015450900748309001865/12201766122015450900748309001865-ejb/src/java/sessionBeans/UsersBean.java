/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBeans;

import entityClasses.Users;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Administrator
 */
@Stateless
public class UsersBean implements UsersBeanLocal {

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")    
    @PersistenceContext(unitName = "12201766122015450900748309001865-ejbPU")
    private EntityManager em;

    public void persist(Object object) {
        em.persist(object);
    }

    /**
     *
     * @return
     *///list all the users from the database
    @Override
    public List<Users> showAllUsers() {
        Query q = em.createNamedQuery("Users.findAll");
        return q.getResultList();
    }

    /**
     *
     * @param name
     * @return get the use by name from the database
     */
    @Override
    public Users getUserByName(String name) {
        //create a named query from Users Entity Class.
        Query query = em.createNamedQuery("Users.findByName");
        //set the query parameter, the SQL is parameterized to prevent SQL injection attack.
        query.setParameter("name", name);
        //get the result from the named query.
        try {
            //if there is no entry with the given name the getSingleResult will lead to exception.
            return (Users) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     * @param Id
     * @return search the database using the ID
     */
    @Override
    public Users getUserById(String Id) {
        Query query = em.createNamedQuery("Users.findById");
        //the parameter should meet the definition in the corresponding entity class.
        query.setParameter("id", Id);
        try {
            return (Users) query.getSingleResult();
        } catch (Exception e) {
            //if there is no entry with the given name the getSingleResult will lead to exception.
            return null;
        }
    }

    /**
     *
     * @param userName
     * @param password
     * @return create the new accout
     */
    @Override
    public boolean createNewCustomer(String userName, String password) {

        //check if the same user exists in the database.
        Users newUser = getUserByName(userName);
        if (newUser != null) {// the user with the same user name found, registration failed.
            return false;
        }

        //get the highest user id number from the database.
//        Query query = em.createNamedQuery("Users.getHighestUserId");
//        int id = (Integer) query.getSingleResult();
        int id = (Integer) em.createNamedQuery("Users.getHighestUserId").getSingleResult();
        //increment by 1 to get the next id
        id++;
        //create a new entity class object:
        Users u = new Users(id);
        //set the name, password, set the type to customer.
        u.setName(userName);
        u.setPassword(password);
        u.setType("customer");
        //make the new user persistent
        persist(u);
        return true;
    }
}
