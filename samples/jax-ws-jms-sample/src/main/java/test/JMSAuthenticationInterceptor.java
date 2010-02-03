package test;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class JMSAuthenticationInterceptor extends AbstractPhaseInterceptor<Message> {
    public JMSAuthenticationInterceptor() {
        super(Phase.RECEIVE);
    }

    public void handleMessage(Message message) throws Fault {
        System.out.println(message.get(Message.PROTOCOL_HEADERS));
    }
}
