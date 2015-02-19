/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

public class AntiSqlInjectionfilter implements Filter {

    public void destroy() {
        // TODO Auto-generated method stub
    }

    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub
    }

    public void doFilter(ServletRequest args0, ServletResponse args1,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) args0;
        //get the name of getparamenter
        Enumeration params = req.getParameterNames();
        String sql = "";
        String sql1 = "";
        while (params.hasMoreElements()) {
            //get the name
            String name = params.nextElement().toString();
            //get the value
            String[] value = req.getParameterValues(name);
            for (int i = 0; i < value.length; i++) {
                sql = sql + value[i];
            }
        }
        //System.out.println("============================SQL"+sql);
        //if there is something wrong ,redirect to error.html
        sql1=clean(sql);
        if (sqlValidate(sql1)) {
            //throw new IOException("It is too dangerour");
            //String ip = req.getRemoteAddr();
            chain.doFilter(args0, args1);
        } else {
            chain.doFilter(args0, args1);
        }
    }

    //check
    protected static boolean sqlValidate(String str) {
        str = str.toLowerCase();//change to low case
        String badStr = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|chr|mid|master|truncate|"
                + "char|declare|sitename|net user|xp_cmdshell|;|or|-|+|,|like'|and|exec|execute|insert|create|drop|"
                + "table|from|grant|use|group_concat|column_name|"
                + "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|"
                + "chr|mid|master|truncate|char|declare|or|;|-|--|+|,|like|//|/|%|#";//filer the danger words.
        String[] badStrs = badStr.split("\\|");
        for (int i = 0; i < badStrs.length; i++) {
            if (str.indexOf(badStrs[i]) >= 0) {
                return true;
            }
        }
        return false;
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
