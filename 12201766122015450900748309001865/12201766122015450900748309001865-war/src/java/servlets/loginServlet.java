/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import entityClasses.Users;
import java.io.IOException;
import java.io.PrintWriter;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sessionBeans.*;

/**
 *
 * @author Administrator
 */
public class loginServlet extends HttpServlet {
    
    @EJB
    private UsersBeanLocal usersBean;
    
    
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
        //read the possible loginUser cookies from the browser.
        //display the loginUser login form.
        boolean loginFailed = false;
        if (request.getSession().getAttribute("loginFailed") != null)
        {
            //if previous login failed, display some information.
            Object o = request.getSession().getAttribute("loginFailed");
            loginFailed = request.getSession().getAttribute("loginFailed").equals(true);
        }
        fillLoginForm(request, response, loginFailed);
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
        userAuthenticateAndRedirect(request, response);
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
     * print out the html code for loginUser login form displaying.
     * @param request
     * @param response 
     */
    private void fillLoginForm(HttpServletRequest request, HttpServletResponse response, 
            boolean loginFailed) throws IOException
    {
        //user name in previous cookie
        String userNameInCookieString = null;
        //read the cookies from the loginUser browser.
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals("userName"))
                {
                    //get the previous loginUser name from the cookie.
                    userNameInCookieString = cookie.getValue();
                }
            }
        }
        /////////////////////////////////////////////
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Login Page</title>");            
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Welcome to our online shop!</h1>");
        
        if (loginFailed)
        {
            //login in failed deal to missing loginUser name, password or incorrect credentials
            out.println("<font color=\"red\">Login/Register Failed, Please try again:</font><br>");
        }
        else
        {
            //normal loginUser login in.
            out.println("<font color=\"blue\">Please login to continue...</font><br>");
        }
        
        //write down the main login form of the the html
        out.println("<form action=\"loginServlet\" method=\"POST\">");
        out.println("<p>Please login or register</p>");
        out.println("User Name: <input type=\"text\" name=\"userName\"");
        //if the loginUser logined in once in the past:
        if (userNameInCookieString != null)
        {
            out.println(" value=\"" + userNameInCookieString + "\"");
        }
        out.println("><br>");
        out.println("Password:&nbsp <input type=\"password\" name=\"password\"><br>");
        out.println("<input type=\"submit\" name=\"button\" value=\"Login\">");
        out.println("<input type=\"submit\" name=\"button\" value=\"Register\">");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
        
        out.println("</body>");
        out.println("</html>");        
    }

    /**
     * authenticate the customer and administrator login and redirect to the corresponding pages.
     * @param request
     * @param response
     * @throws IOException 
     */
    private void userAuthenticateAndRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //get the loginUser name and password from the parameters and store in cookies
        String userNameString = request.getParameter("userName");
        String passwordString = request.getParameter("password");
        
        //make sure valid loginUser name and password are given before pass them to queries.
        if (!userNameString.equals("") && !passwordString.equals(""))
        {
            String clickedButtonString = request.getParameter("button");
            //check if it was the login button pressed or the register button pressed.
            if (clickedButtonString.equals("Login"))
            {//Login button pressed.
                //check the login status.
                Users loginUser = usersBean.getUserByName(userNameString);
                if (loginUser != null)
                {   //user with the loginUser name found.
                    String passwordInDataBase = loginUser.getPassword();
                    if (passwordInDataBase.equals(passwordString))
                    {//login successful
                        //set the login status.
                        boolean loginFailed = false;
                        request.getSession().setAttribute("loginFailed", loginFailed);
                        
                        //create and send cookie to loginUser's browser.
                        Cookie nameCookie = new Cookie("userName", userNameString);
                        //set the max living time of the cookie
                        //nameCookie.setMaxAge(5*60);
                        //add the cookies
                        response.addCookie(nameCookie);
                        
                        ///////////////////////////////////////////////////////////
                        //set the session attributes about current user to prevent broken session authentication.
                        request.getSession().setAttribute("authenticatedUserName", userNameString);
                        ///////////////////////////////////////////////////////////
                        
                        //distinguish loginUser type: for adiministrator, show the manage products page.
                        //for customer loginUser: show the view products page.
                        if (loginUser.getType().equals("admin"))
                        {
                            //send the manage products page.
                            response.sendRedirect("manageProductsServlet");
                        }
                        else if (loginUser.getType().equals("customer"))
                        {
                            //send the view products page.
                            response.sendRedirect("viewProductsServlet");
                        }
                    }//if wrong password
                    else
                    {//no such loginUser in the data base.
                        boolean loginfailed = true;
                        request.getSession().setAttribute("loginFailed", loginfailed);
                        //send login form again.
                        response.sendRedirect("loginServlet");
                    }
                }//if no such user found in database.
                else
                {//no such loginUser in the data base.
                    boolean loginfailed = true;
                    request.getSession().setAttribute("loginFailed", loginfailed);
                    //send login form again.
                    response.sendRedirect("loginServlet");
                }
            }//if (clickedButtonString.equals("Login"))
            else if (clickedButtonString.equals("Register"))
            {//new loginUser registration.
                //add the new loginUser into the database table USERS
                //usersBean.createNewUser(userNameString, passwordString);
                
                if(usersBean.createNewCustomer(userNameString, passwordString))
                {
                    boolean loginFailed = false;
                    request.getSession().setAttribute("loginFailed", loginFailed);
                    //new user created in the database, now send login form again.

                    //create and send cookie to loginUser's browser.
                    Cookie nameCookie = new Cookie("userName", userNameString);
                    Cookie passwordCookie = new Cookie("password", passwordString);
                    //add the cookies
                    response.addCookie(nameCookie);
                    response.addCookie(passwordCookie);

                    response.sendRedirect("loginServlet");
                } else {
                    //the user with the same name exists, registration failed.
                    boolean loginFailed = true;
                    request.getSession().setAttribute("loginFailed", loginFailed);
                    response.sendRedirect("loginServlet");
                }
            }
        }//if (!userNameString.equals("") && !passwordString.equals(""))
        else
        {
            //incomplete loginUser registration requset.
            //set the attribute for login.
            String loginStatus = "failed";
            request.getSession().setAttribute("loginStatus", loginStatus);
            //send login form again.
            response.sendRedirect("loginServlet");
        }
    }
}
