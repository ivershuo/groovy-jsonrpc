package groovy.jsongrpc;

import groovy.jsongrpc.tools.RpcServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class jetty {

    public jetty() {
	// TODO Auto-generated constructor stub
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	Server server = new Server(8080);
	WebAppContext context = new WebAppContext(".", "/");
	ServletHolder holder = new ServletHolder(new RpcServlet());
	holder.setInitParameter("initbase", "test/testbase.groovy;test/testsub.groovy");
	context.addServlet(holder, "*.groovy");
	server.setHandler(context);
	server.start();
	server.join();
    }

}
