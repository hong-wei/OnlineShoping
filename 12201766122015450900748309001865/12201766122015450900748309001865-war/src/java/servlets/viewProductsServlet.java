/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import entityClasses.*;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sessionBeans.*;



/**
 *
 * @author Administrator
 */
public class viewProductsServlet extends HttpServlet {
    @EJB
    private ProductsBeanLocal productsBean;

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

        //authenticate user before display the webpage in case of broken authentication and session management.
        if (request.getSession().getAttribute("authenticatedUserName") != null)
        {
            String validUserNameString = request.getSession().getAttribute("authenticatedUserName").toString();
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
                            ///////////////////////////////////
                            //user authentication successful.
                            ////////////////////////////////////
                            displayProductSearchForm(request, response);
                        }
                        else
                        {   //user authentication failed.
                            response.sendRedirect("loginServlet"); 
                        }
                    }
                }
            }
            else
            {
                response.sendRedirect("loginServlet"); 
            }
        }
        else
        {
            //send login form.
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
        
        PrintWriter out = response.getWriter();
        //add the stateful session bean shopping cart into the session context
        ShoppingCartBeanLocal shoppingCart = (ShoppingCartBeanLocal) request.getSession().getAttribute("cart");
        if (shoppingCart == null)
        {
            System.out.println("Request new stateful session bean\n");
            //look up and get the new stateful session bean.
            shoppingCart = lookupShoppingCartBeanBean();
            request.getSession().setAttribute("cart", shoppingCart);
        }
        //variable representing the current product searched.
        Products currentProduct;
        //the quantity of the product adding to or remove from database.
        int changeInQuantity;
        //authenciated user's name.
        String currentUser = request.getSession().getAttribute("authenticatedUserName").toString();
        

        
        
        String welcomeString = "Welcome: " + currentUser;
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Search Result</title>");            
        out.println("</head>");
        out.println("<body>");
        out.println(welcomeString);
        out.println("<h2>Search Result:</h2>");
        
        String pressedButton = request.getParameter("button");
        if (pressedButton.equals("Search")){
            //display the product search result for the customer.
            displayProductSearchResult(request, response, out);
        }else{
            currentProduct = (Products) request.getSession().getAttribute("currentProduct");
            if (request.getParameter("productQuantity").equals("")){
                changeInQuantity = 0;
            }else{   //have a valid quantity user input.
                changeInQuantity = Integer.parseInt(request.getParameter("productQuantity"));
            }
            if (changeInQuantity < 0){
                changeInQuantity = 0;
            }
            
            //determine the button pressed.
            if (pressedButton.equals("Add to Cart"))
            {
                //add certain amount of product into the shopping cart.
                shoppingCart.addItem(currentProduct.getId(), changeInQuantity);      

            }//add to cart.
            else if (pressedButton.equals("Remove from Cart"))
            {
                //remove certain amount of the product from the shopping cart.
                shoppingCart.removeItem(currentProduct.getId(), changeInQuantity);
            }//remove from cart.
            else if (pressedButton.equals("Add Comment")){
                //prevent XSS by sanitizing the string before put it into database.
                String rawCommentString = request.getParameter("productComment");
                if (!rawCommentString.equals("")){
             
                    //add the comment into the database.
                    productsBean.addProductCommentById(currentProduct.getId(), rawCommentString);
                    //update the current product
                    currentProduct = productsBean.getProductById(currentProduct.getId());
                }

            }//else if add comment
            
            //display the body part of the HTML            
            displayCurrentProductForm(currentProduct, request, response, out);
            
        }//other buttons other than search pressed.
        
        //show quantity of the product in the shopping cart.
        out.println(shoppingCart.getItemList());
        //show the bottom of the HTML page.
        out.println("<br>");
        
        out.println("<a href=\"logoutServlet\">Click to logout</a>");
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

    /**
     * called in doGet
     * @param request
     * @param response
     * @throws IOException 
     */
    private void displayProductSearchForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ////////////////////////////////////////////
        //authenticate user here before showing the page to the user.
        ////////////////////////////////////////////
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String welcomeString = "Welcome: " + request.getSession().getAttribute("authenticatedUserName").toString();
        
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>View Products</title>");            
            out.println("</head>");
            out.println("<body>");
            
            out.println("<P>" + welcomeString + "</P>");
            out.println("Please input ether product name or id to search:");
            out.println("<form action=\"viewProductsServlet\" method=\"POST\">");
            out.println("<table><tr><td>Product Name: </td>");
            out.println("<td><input type=\"text\" name=\"productName\"></td>");
            out.println("<tr><td>Product Id: </td>");
            out.println("<td><input type=\"text\" name=\"productId\"></td>");
            out.println("<tr><td><input type=\"submit\" name=\"button\" value=\"Search\"></td></tr></table>");
            out.println("</form>");
            if (request.getSession().getAttribute("incompleteSearchParameter") != null &&
                request.getSession().getAttribute("incompleteSearchParameter").equals(true))
            {
                //input imcomplete search parameters.
                out.println("<font color=\"red\">Please input product name or id</font><br>");
            }
            out.println("<p>Items in Store:</p>");
            /////////////////////////
            //display all the products in the database.
            ////////////////////////
            List<Products> productList = productsBean.getAllProducts();
            showItemsInList(productList, out);
            ////////////////////////////////////////////
            
            
            out.println("<a href=\"logoutServlet\">Click to logout</a>");
            out.println("</body>");
            out.println("</html>");
            
        } finally {            
            out.close();
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
     * called in doPost
     * @param request
     * @param response
     * @param out
     * @throws IOException 
     */
    private void displayProductSearchResult(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        //get the product name or id input by the user.
        String searchProductName = request.getParameter("productName");
        String searchProductId = request.getParameter("productId");
        Products resultProduct = null;
        if (searchProductId.equals("") && searchProductName.equals(""))
        {
            //no valid input found.
            request.getSession().setAttribute("incompleteSearchParameter", true);
            response.sendRedirect("viewProductsServlet"); 
        }
        else
        {
            request.getSession().setAttribute("incompleteSearchParameter", false);
        }
        //query the database with the given product name or id.
        if (!searchProductId.equals(""))
        {
            //query the database for the item.
            resultProduct = productsBean.getProductById(Integer.parseInt(searchProductId));
            //set the search result as the shared parameter.
            request.getSession().setAttribute("currentProduct", resultProduct);
        }//if id parameter is empty
        else if (!searchProductName.equals(""))
        {
            //query the database for the item.
            resultProduct = productsBean.getProductByName(searchProductName);
            //set the search result as the shared parameter.
            request.getSession().setAttribute("currentProduct", resultProduct);
        }
        if (resultProduct == null)
        {
            out.println("<p>Sorry, there is no such a product in the store.</p>");
            out.println("<form action=\"viewProductsServlet\" method=\"GET\">");
            out.println("<input type=\"submit\" value=\"View Other Products\">");
            out.println("</form>");
        }
        else
        {
            //add the product into an arraylist and display the search result.
            ArrayList<Products> items = new ArrayList<Products>();
            items.add(resultProduct);
            this.showItemsInList(items, out);
            //display the function buttons
            displayFunctionButtons(request, response, out);
        }
    }

    /**
     * Display the HTML component in the page.
     * @param request
     * @param response
     * @param out 
     */
    private void displayFunctionButtons(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        
        out.println("<b>Please input quantity you want to add to or remove from your shopping cart: </b>");
        out.println("<form action=\"viewProductsServlet\" method=\"POST\">");
        out.println("Quantity: <input type=\"text\" name=\"productQuantity\">");
        out.println("<input type=\"submit\" name=\"button\" value=\"Add to Cart\">");
        out.println("<input type=\"submit\" name=\"button\" value=\"Remove from Cart\"><br>");
        out.println("Comment: <input type=\"text\" name=\"productComment\">");
        out.println("<input type=\"submit\" name=\"button\" value=\"Add Comment\">");
        out.println("</form>");
        out.println("<form action=\"viewShoppingCartServlet\" method=\"GET\">");
        out.println("<input type=\"submit\" value=\"Go to Shopping Cart\">");
        out.println("</form>");
        out.println("<form action=\"viewProductsServlet\" method=\"GET\">");
        out.println("<input type=\"submit\" value=\"View Other Products\">");
        out.println("</form>");
    }

    private ShoppingCartBeanLocal lookupShoppingCartBeanBean() {
        try {
            Context c = new InitialContext();
            return (ShoppingCartBeanLocal) c.lookup("java:global/12201766122015450900748309001865/12201766122015450900748309001865-ejb/ShoppingCartBean!sessionBeans.ShoppingCartBeanLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    /**
     * called after (Add to Cart)(Remove from Cart)(Add Comment) buttons are pressed
     * @param request
     * @param response
     * @param out 
     */
    private void displayCurrentProductForm(Products currentProduct, HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        ArrayList<Products> items = new ArrayList<Products>();
        items.add(currentProduct);
        //display the current product.
        this.showItemsInList(items, out);
        //display the rest of the form.
        this.displayFunctionButtons(request, response, out);
    }

    
}
