/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sessionBeans.*;
import messageDriveBeans.*;
/**
 *
 * @author Administrator
 */
public class viewShoppingCartServlet extends HttpServlet {
//    @Resource(mappedName = "jms/logMessage")
//    private Queue logMessage;
//    @Resource(mappedName = "jms/logMessageFactory")
//    private ConnectionFactory logMessageFactory;

    @Resource(mappedName = "jms/loggingMessageBean")
    private Queue loggingMessageBean;
    @Resource(mappedName = "jms/loggingMessageBeanFactory")
    private ConnectionFactory loggingMessageBeanFactory;
    
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
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        if (request.getSession().getAttribute("authenticatedUserName") != null)
        {
            //get the stateful session bean shoppingCart from the request.
            ShoppingCartBeanLocal shoppingCart = (ShoppingCartBeanLocal) request.getSession().getAttribute("cart");
            if (shoppingCart == null)
            {
                out.println("Error getting shopping cart!<br>");
            }
            String appreciationString = "<h2>Thank you for choosing our store " + request.getSession().getAttribute("authenticatedUserName").toString() + "!</h2>";

            try {
                /* TODO output your page here. You may use following sample code. */
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Shopping Cart</title>");            
                out.println("</head>");
                out.println("<body>");
                out.println(appreciationString);
                //display the shopping cart.
                out.println(shoppingCart.getItemList());

                out.println("<br>");
                out.println("<form action=\"viewShoppingCartServlet\" method=\"POST\">");
                out.println("<input type=\"submit\" name=\"button\" value=\"Checkout\">");
                out.println("&nbsp&nbsp&nbsp&nbsp");
                out.println("<input type=\"submit\" name=\"button\" value=\"Cancel\">");
                out.println("</form>");
                out.println("<form action=\"viewProductsServlet\" method=\"GET\">");
                out.println("<input type=\"submit\" value=\"Add More Products\">");
                out.println("</form>");


                out.println("</body>");
                out.println("</html>");
            } finally {            
                out.close();
            }
        }
        else
        {
            //send the login form.
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
        ShoppingCartBeanLocal shoppingCart = (ShoppingCartBeanLocal) request.getSession().getAttribute("cart");
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Checkout</title>");            
        out.println("</head>");
        out.println("<body>");
        if (shoppingCart == null)
        {
            out.println("Error getting shopping cart!<br>");
        }
        String buttonPressed = request.getParameter("button");
        if (buttonPressed != null)
        {
            String currentUserName = request.getSession().getAttribute("authenticatedUserName").toString();
            String resultString;
            String messageString;
            if (buttonPressed.equals("Checkout"))
            {
                out.println(shoppingCart.getTotalPrice());
                //get the check out message.
                messageString = currentUserName + " have checked out with message: ";
                resultString = shoppingCart.checkout();
                messageString += resultString;
                out.println(resultString);
                try {
                    //send the result message to the message driven bean.
                    this.sendJMSMessageToMessageDrivenBean(messageString);
                } catch (JMSException ex) {
                    Logger.getLogger(viewShoppingCartServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                //remove the shopping cart attribute from the session
                request.getSession().removeAttribute("cart");
            }
            else if (buttonPressed.equals("Cancel"))
            {
                //get the check out message.
                messageString = currentUserName + " have canceled his/her shopping cart with message:\n";
                resultString = shoppingCart.cancel();
                messageString += resultString;
                out.println(resultString);
                try {
                    this.sendJMSMessageToMessageDrivenBean(messageString);
                } catch (JMSException ex) {
                    Logger.getLogger(viewShoppingCartServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                //remove the shopping cart attribute from the session
                request.getSession().removeAttribute("cart");
            }
        }
        out.println("<br>");
        out.println("<a href=\"viewProductsServlet\">Buy something else</a>");
        out.println("</body>");
        out.println("</html>");
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
    
    private Message createJMSMessageForMessageDrivenBean(Session session, Object messageData) throws JMSException {
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
    private void sendJMSMessageToMessageDrivenBean(Object messageData) throws JMSException {
        Connection connection = null;
        Session session = null;
        try {
            connection = loggingMessageBeanFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(loggingMessageBean);
            messageProducer.send(createJMSMessageForMessageDrivenBean(session, messageData));
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
