package manhuntrestfulservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * EJB/Data Access Object for Movies objects
 * contains functions for retrieving and updating 
 * information in the database
 *
 * @author Devin Grant-Miles
 */
@WebService
@Stateless 
@LocalBean
public class UserDAOBean implements UserDAO 
{
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public void updateUserLatLng(String userName, String latLng) 
    {
        User user = new User();
                
        user.setUserName(userName);
        user.setLatLng(latLng);

        this.entityManager.persist(user);
    }
    
    @Override
    public void addUser(String userName) 
    {
        System.out.println("Working");
        User user = new User();
                
        user.setUserName(userName);

        this.entityManager.persist(user);
    }
    
    @Override
    public Collection<User> getAllUsers() 
    {
        List<User> userList = new ArrayList();

        String jpqlCommand = "SELECT p FROM User p";
        Query query = entityManager.createQuery(jpqlCommand);
        userList = query.getResultList();

        return userList;
    }
     
    @Override
    public User getOneUser(String userName) 
    {
        List<User> userList = new ArrayList();
        
        String jpqlCommand = "SELECT p FROM User p WHERE p.Username LIKE :userName";
        Query query = entityManager.createQuery(jpqlCommand);
        query.setParameter("userName", "%" + userName + "%");

        userList = query.getResultList();

        return userList.get(0);
    }
}
