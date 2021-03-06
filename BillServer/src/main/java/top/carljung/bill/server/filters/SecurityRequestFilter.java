package top.carljung.bill.server.filters;

import java.io.IOException;
import java.security.Principal;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.carljung.bill.factory.SessionFactory;
import top.carljung.bill.server.Session;

/**
 *
 * @author wangchao
 */
@Provider
@PreMatching
public class SecurityRequestFilter implements ContainerRequestFilter{
    private static final Logger logger = LoggerFactory.getLogger(SecurityRequestFilter.class);
    @Override
    public void filter(ContainerRequestContext crc) throws IOException {
        crc.setSecurityContext(new SecurityContext(){
            private Session session;
            
            @Override
            public Principal getUserPrincipal() {
                if (session == null) {
                    session = SessionFactory.instance().getSession(crc);
                }
                return session;
            }

            @Override
            public boolean isUserInRole(String role) {
                Session session = (Session)getUserPrincipal();
                return session != null && session.getRoles().contains(role);
            }

            @Override
            public boolean isSecure() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getAuthenticationScheme() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

}
