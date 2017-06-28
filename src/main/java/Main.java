import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
    final Integer port = Integer.valueOf(System.getenv("PORT"));

    Runnable r = new Runnable() {
      public void run() {
        try {
          Thread.sleep(60000);
          CloseableHttpClient httpclient = HttpClients.createDefault();
          while (true) {
            HttpGet httpget = new HttpGet("http://localhost:" + port + "/health");
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
              HttpEntity entity = response.getEntity();
              if (entity != null) {
                long len = entity.getContentLength();
                if (len != -1 && len < 2048) {
                  String reportedStatus = EntityUtils.toString(entity);
                  if (!"up".equals(reportedStatus)) {
                    System.out.println("at=health-check status=down action=exiting");
                    System.exit(1);
                  } else {
                    System.out.println("at=health-check status=" + reportedStatus);
                  }
                } else {
                  System.out.println("at=health-check status=unknown");
                }
              }
              Thread.sleep(10000);
            } finally {
              response.close();
            }
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ClientProtocolException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };

    new Thread(r).start();

    Server server = new Server(port);
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(new Main()),"/*");
    server.start();
    server.join();
  }
}
