/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import entityClasses.Products;
import entityClasses.Users;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sessionBeans.*;
import messageDriveBeans.*;

/**
 *
 * @author Administrator
 */
public class manageProductsServlet extends HttpServlet {
//    @Inject
//    @JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
//    private JMSContext context;
    
    @Resource(mappedName = "jms/loggingMessageBean")
    private Queue loggingMessageBean;
    @Resource(mappedName = "jms/loggingMessageBeanFactory")
    private ConnectionFactory loggingMessageBeanFactory;
    
    @EJB
    private UsersBeanLocal usersBean;
    
    @EJB
    private ProductsBeanLocal productsBean;
    

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //authenticate user before given permission to access to the data management.
        authenticateAdministrator(request, response);
        //display the management page for the administrator.
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        request.getSession().setAttribute("incompleteFields", false);
        
        displayManagementPage(request, response, out);
    }

    
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession().getAttribute("authenticatedUserName") != null)
        {
            processRequest(request, response);
        }
        else
        {
            response.sendRedirect("loginServlet");
        }
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        //get the current user of the page.
        String currentUser = request.getSession().getAttribute("authenticatedUserName").toString();
        request.getSession().setAttribute("incompleteFields", true);
        
        String pressedButton = request.getParameter("button");
        if (pressedButton != null)
        {
            if (pressedButton.equals("View Log"))
            {
                //open the database log in notepad.
                request.getSession().setAttribute("incompleteFields", false);
                Process p = Runtime.getRuntime().exec("notepad C:/12201766DataBaseLog.txt");
            }
            String parameter = request.getParameter("productId");
            if (!parameter.equals(""))
            {
                //valid product id
                int productId = Integer.parseInt(parameter);
                if (pressedButton.equals("Remove Product"))
                {
                    //////////////////////////////////////
                    //check for potential mistakes in the method with wrong id.
                    //////////////////////////////////////
                    
                    Products productToDelete = productsBean.getProductById(productId);
                    //set the shared session attribute "incompleteFields" to false
                    request.getSession().setAttribute("incompleteFields", false);
                    //remove the product from the data base.
                    productsBean.removeProductById(productId);
                    try {
                        //write the database change into log.
                        sendJMSMessageToLoggingMessageBean("Administrator "+ currentUser +" has deleted a kind of product from the database: " + productToDelete.getName() + "\n");
                    } catch (JMSException ex) {
                        Logger.getLogger(manageProductsServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else
                {
                    //get the quantity from the text box
                    if (!request.getParameter("quantity").equals(""))
                    {
                        //set the shared session attribute "incompleteFields" to false
                        request.getSession().setAttribute("incompleteFields", false);
                        int quantity = Integer.parseInt(request.getParameter("quantity"));
                        if (pressedButton.equals("Increase Quantity"))
                        {
                            //increase the quantity of the product.
                            productsBean.increaseProductQuantity(productId, quantity);
                        } 
                        else if (pressedButton.equals("Decrease Quantity"))
                        {
                            //decrease the quantity of the product.
                            productsBean.decreaseProductQuantity(productId, quantity);
                        }
                        else if (pressedButton.equals("Add New Product"))
                        {
                            String name = request.getParameter("name");
                            String price = request.getParameter("price");
                            if (  !(name.equals("") || price.equals(""))  )
                            {
                                //set the shared session attribute "incompleteFields" to false
                                request.getSession().setAttribute("incompleteFields", false);
                                int id = productsBean.addNewProduct(name, Double.parseDouble(price), quantity);
                                String messageString = "Administrator "+ currentUser +" has added a new product into the database: " +
                                        "Id: " + id + " Name: " + name + " Price: " + Double.parseDouble(price) + " Quantity: " + quantity + "\n";
                                //write new product adding event into log.
                                if (id != 0)
                                {//successful new product registration.
                                    try {
                                        this.sendJMSMessageToLoggingMessageBean(messageString);
                                    } catch (JMSException ex) {
                                        Logger.getLogger(manageProductsServlet.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                    }
                }//else increase/decrease/add new product
            }//if operation successful.
        }//button pressed.
        
        //the bussiness methods are all finished, display the rest of the form
        
        this.displayManagementPage(request, response, out);
        
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

    /**
     * make sure the user of the page is the legitimate administrator.
     * @param request
     * @param response
     * @throws IOException 
     */
    private void authenticateAdministrator(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //authenticate user before display the webpage in case of broken authentication and session management.
        String validUserNameString;

        validUserNameString = request.getSession().getAttribute("authenticatedUserName").toString();
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (int i=0; i<cookies.length; i++)
            {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals("userName"))
                {
                    String userNameString = cookie.getValue();
                    if (userNameString.equals(validUserNameString))
                    {
                        //check the user is administrator or not.
                        Users currentUser = usersBean.getUserByName(userNameString);
                        if (!currentUser.getType().equals("admin"))
                        {
                            //the user is not the administrator of the system.
                            response.sendRedirect("loginServlet");
                        }
                        try {
                            //administrator authenticated, write the event into log.
                            this.sendJMSMessageToLoggingMessageBean("Administrator " + validUserNameString + " logged into the system.");
                        } catch (JMSException ex) {
                            Logger.getLogger(manageProductsServlet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else
                    {
                        //user authentication failed.
                        response.sendRedirect("loginServlet");
                    }
                }
            }
        }
        else
        {// the user has not logged in before.
            response.sendRedirect("loginServlet"); 
        }
        
    }
    
    /**
     * display all the products in the list in HTML format.
     * @param items products in the list
     * @param out PrintWriter
     */
    private void showItemsInList(List<Products> items, PrintWriter out)
    {
        out.println("<table>");
        out.println("<tr><th>ID</th><th>Name</th><th>Price</th><th>Quantity</th><th>Comments</th><th></th></tr>");
        int id;
        String name;
        double price;
        int quantity;
        String comment;
        //display all the items in the list in a table in html
        for (int i = 0; i<items.size(); i++)
        {
            Products product = items.get(i);
            id = product.getId();
            name = product.getName();
            price = product.getPrice();
            quantity = product.getQuantity();
            comment = product.getComment();
            out.println("<tr><td>"+id+"</td><td>"+name+"</td><td>"+price+"</td><td>"+quantity+"</td><td>"+comment+"</td></tr>");
        }
        out.println("</table>");
        out.println("<br>");
    }
    
    /**
     * display the management page for the system administrator.
     * @param request
     * @param response
     * @param out 
     */
    private void displayManagementPage(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
    {
        //display the contents of the management page.
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Manage Products</title>");            
        out.println("</head>");
        out.println("<body>");

        String currentUserString = request.getSession().getAttribute("authenticatedUserName").toString();
        String welcomeString = "Good day: " + currentUserString + "<br><br>";
        out.println(welcomeString);
        //display all the products in the database.
        out.println("<b>All Products in Store:</b>");
        List<Products> items = productsBean.getAllProducts();
        this.showItemsInList(items, out);
        //display the input text boxes and buttons.
        out.println("Please input product id:<br>");
        
        if (request.getSession().getAttribute("incompleteFields") != null && !request.getSession().getAttribute("incompleteFields").equals(false))
        {
            //the last input has incomplete parameter.
            out.println("<font color=\"red\">Please fill in all necessary blanks to continue</font><br>");
        }
        
        out.println("<form action=\"manageProductsServlet\" method=\"POST\">");
        out.println("Product ID:&nbsp");
        out.println("<input type=\"text\" name=\"productId\">");
        out.println("<input type=\"submit\" name=\"button\" value=\"Remove Product\"><br>");
        out.println("Quantity:&nbsp&nbsp");
        out.println("<input type=\"text\" name=\"quantity\">");
        out.println("<input type=\"submit\" name=\"button\" value=\"Increase Quantity\">");
        out.println("<input type=\"submit\" name=\"button\" value=\"Decrease Quantity\"><br>");
        out.println("Please also input the follow for adding new products:<br>");
        out.println("Product Name: &nbsp");
        out.println("<input type=\"text\" name=\"name\">");
        out.println("Price: &nbsp&nbsp&nbsp&nbsp");
        out.println("<input type=\"text\" name=\"price\"><br>");
        out.println("<input type=\"submit\" name=\"button\" value=\"Add New Product\"><br>");
        out.println("<input type=\"submit\" name=\"button\" value=\"View Log\">");
        out.println("</form>");     
        
        
        out.println("<a href=\"logoutServlet\">Click to logout</a>");
        out.println("</body>");
        out.println("</html>");
    }

    private Message createJMSMessageForjmsLoggingMessageBean(Session session, Object messageData) throws JMSException {
        // TODO create and populate message to send
        TextMessage tm = session.createTextMessage();
        tm.setText(messageData.toString());
        return tm;
    }

    /**
     * send message to message driven bean for logging in a log file
     * the log file is store in the location of D:\12201766DataBaseLog.txt
     * @param messageData
     * @throws JMSException 
     */
    private void sendJMSMessageToLoggingMessageBean(Object messageData) throws JMSException {
        Connection connection = null;
        Session session = null;
        try {
            connection = loggingMessageBeanFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(loggingMessageBean);
            messageProducer.send(createJMSMessageForjmsLoggingMessageBean(session, messageData));
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot close session", e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
