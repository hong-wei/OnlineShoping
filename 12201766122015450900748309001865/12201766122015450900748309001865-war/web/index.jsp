<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>

        <%
            //String pageString = request.getContextPath() + "/loginServlet";
            response.sendRedirect("loginServlet"); 
//            response.sendRedirect("PersonServlet"); 
        %>
    </body>
</html>
