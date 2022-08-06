package manhuntrestfulservice;


import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Java class that represents a Movie object Uses entities to manage
 * interactions with database Based on petrestfulservice
 *
 * @author Devin Grant-Miles
 */

@Entity
@Table(name = "ManhuntUsers")
public class User implements Serializable 
{
    @Id
    @Column(name = "Username")
    private String userName;
    @Column(name = "LatLng")
    private String latLng;

    public User()
    {}
    
    public User(String userName)
    {
        this.userName = userName;
    }
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }
    
    //converts movie object and fields to XML
    public String getXMLString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<users>");
        buffer.append("<userName>").append(getUserName()).append("</userName>");
        buffer.append("<latLng>").append(getLatLng()).append("</latLng>");
        buffer.append("</users>");
        return buffer.toString();
    }
}







    

