/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.owasp.validator.html.*;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
	HttpServletRequest orgRequest = null;

	public XssHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		orgRequest = request;
	}

	/**
	 * rewrite the method of getParameter，filer the paraments and value。<br/>
	 */
	@Override
	public String getParameter(String name) {
		String value = super.getParameter(clean(name));
		if (value != null) {
			value = clean(value);
		}
		return value;
	}

         public static String clean(String s) {
           String cleanText = null;
           try {
               Policy policy = Policy.getInstance("c:\\antisamy\\antisamy-slashdot-1.4.4.xml");
               AntiSamy as = new AntiSamy();
               CleanResults cr = as.scan(s, policy);
               cleanText = cr.getCleanHTML();
           } catch (PolicyException pe) {
               cleanText = "PolicyException";
           } catch (ScanException se) {
               cleanText = "ScanException";
           } catch (Exception e) {
               cleanText = "Exception";
           }
           return cleanText;
       }
         
         

}