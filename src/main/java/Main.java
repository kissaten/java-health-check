import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends HttpServlet {

  private static AtomicBoolean status = new AtomicBoolean(true);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (req.getRequestURI().endsWith("/health")) {
      showHealth(req,resp);
    } else if (req.getRequestURI().endsWith("/toggle")) {
      showToggle(req,resp);
    } else {
      showHome(req,resp);
    }
  }

  private void showHome(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.getWriter().print("Status is: " + status.get());
  }

  private void showToggle(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    status = new AtomicBoolean(!status.get());
    resp.getWriter().print("Changing status to: " + status.get());
  }

  private void showHealth(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (status.get()) {
      resp.getWriter().print("up");
    } else {
      resp.getWriter().print("down");
    }
  }

  public static void main(String[] args) throws Exception{
    Server server = new Server(Integer.valueOf(System.getenv("PORT")));
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(new Main()),"/*");
    server.start();
    server.join();
  }
}
