/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package messageDriveBeans;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @author Administrator
 */
@MessageDriven(mappedName = "jms/logMessage",activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/loggingMessageBean"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
//    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
//    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class LoggingMessageBean implements MessageListener {
    
    private static final Logger logger = Logger.getLogger(LoggingMessageBean.class.getName());
    private static FileHandler fh;
    
    public LoggingMessageBean() {
        try {
            fh = new FileHandler("C:/12201766DataBaseLog.txt");
            // send logger output to the fileHandler.
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            
            StringBuffer sb= new StringBuffer("123");
            FileWriter writer = new FileWriter("c://numbers.txt");
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write(sb.toString());
            bw.close();
            writer.close();
            
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(LoggingMessageBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            java.util.logging.Logger.getLogger(LoggingMessageBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void onMessage(Message message) {
        TextMessage tMessage;
        if (message instanceof TextMessage)
        {
            try {
                //cast the incoming message to text message
                tMessage = (TextMessage) message;
                //put the incoming message into file
                logger.info(tMessage.getText());
            } catch (JMSException ex) {
                Logger.getLogger(LoggingMessageBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
