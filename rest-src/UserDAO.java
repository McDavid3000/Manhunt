package manhuntrestfulservice;

import java.util.Collection;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bighairyballsack
 */
public interface UserDAO 
{
    public void addUser(String userName);    
    public void updateUserLatLng(String userName, String latLng); 
    public Collection<User> getAllUsers(); // maybe a list
    public User getOneUser(String userName); // maybe a list
}
