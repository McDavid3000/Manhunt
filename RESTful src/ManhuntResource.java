package manhuntrestfulservice;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * Restful web service resource for Manhunt Web Service DB Contains two GET
 * requests which retrieve and add data
 *
 * @author Devin Grant-Miles
 */
@RequestScoped
@Named
@Path("/manhunt")
public class ManhuntResource {

    @Context
    private UriInfo context;
    private static final char QUOTE = '\"';

    private Connection conn;
    private Session session;
    private MessageProducer producer;

    //dependency injection of connection factory and 
    //message queues reuiqred for MDB
    @Resource(mappedName = "jms/ManhuntConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(mappedName = "ManhuntMessageQueue")
    private Queue queue;
    @Resource(mappedName = "LocationUpdateQueue")
    private Queue updateQueue;

    /**
     * Creates a new instance of ManhuntResource
     */
    public ManhuntResource() {
    }

    /*
        GET resource which adds a user to DB and
        returns an XML file containing all users 
    */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{user}")
    public String getUserList(@PathParam("user") String userName) {

        String replyXML = "";
        
        try {
            
            //create connection and session
            System.out.println("ESTABLISHING CONNECTION");//for debugging
            conn = connectionFactory.createConnection();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //create message producer and message
            producer = session.createProducer(queue);
            System.out.println("CONNECTION ESTABLISHED => SENDING MESSAGE");//for debugging
            TextMessage message = session.createTextMessage();
            message.setText(userName);

            //create temporary queue to recieve reply to message
            Queue tmpQueue = session.createTemporaryQueue();
            MessageConsumer respConsumer = session.createConsumer(tmpQueue);
            message.setJMSReplyTo(tmpQueue);

            //send message and start connection to recieve reply message
            producer.send(message);
            conn.start();
            Message receivedMessage = respConsumer.receive(15000); // in ms or 15 seconds
            replyXML = ((TextMessage) receivedMessage).getText();
            
            //close connection
            conn.close();
        } catch (JMSException e) {
            System.err.println("Unable to send message: " + e);
        }

        //append XML info for HTTP response
        StringBuilder buffer = new StringBuilder();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buffer.append("<users uri=").append(QUOTE).append(
                context.getAbsolutePath()).append(QUOTE).append(">");
        buffer.append(replyXML);

        return buffer.toString();
    }

    /*
        GET resource which add the users lat and long to DB and
        returns an XML file containing the user and user lat long they want to locate
    */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{user}/{lat}/{lng}/{friend}")
    public String updateUserLocation(@PathParam("user") String user, @PathParam("lat") String lat, @PathParam("lng") String lng, @PathParam("friend") String friend) {

        String replyXML = "";
        try {
            //create connection and session
            System.out.println("ESTABLISHING CONNECTION");
            conn = connectionFactory.createConnection();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //create message producer and message
            producer = session.createProducer(updateQueue);
            System.out.println("CONNECTION ESTABLISHED => SENDING MESSAGE");
            TextMessage message = session.createTextMessage();

            //append the recieved parameters for sending as a message
            String inputMsg = user + "\n" + lat + "\n" + lng + "\n" + friend;
            message.setText(inputMsg);

            //create temporary queue to recieve reply to message
            Queue tmpQueue = session.createTemporaryQueue();
            MessageConsumer respConsumer = session.createConsumer(tmpQueue);
            message.setJMSReplyTo(tmpQueue);
            
            //send message and start connection to recieve reply message
            producer.send(message);
            conn.start();
            Message receivedMessage = respConsumer.receive(15000); // in ms or 15 seconds
            replyXML = ((TextMessage) receivedMessage).getText();

            //close connection
            conn.close();
        } catch (JMSException e) {
            System.err.println("Unable to send message: " + e);
        }
        
        //append XML info for HTTP response
        StringBuilder buffer = new StringBuilder();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buffer.append("<users uri=").append(QUOTE).append(
                context.getAbsolutePath()).append(QUOTE).append(">");
        buffer.append(replyXML);

        return buffer.toString();
    }
}
