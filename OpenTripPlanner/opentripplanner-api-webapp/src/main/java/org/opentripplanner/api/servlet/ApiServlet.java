/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.api.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

public class ApiServlet extends SpringServlet {
    private static final long serialVersionUID = -9081567116972786015L;

    @Autowired(required=false) private PeriodicGraphUpdater updater;

//    @Autowired(required=false) private PeriodicGraphUpdater updater;

//    public PeriodicGraphUpdater getPeriodicGraphUpdater() {
//        return updater;
//    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext servletContext = config.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        AutowireCapableBeanFactory autowireCapableBeanFactory = webApplicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(this);
        // do not use a postconstruct method to start threads - 
        // in this class it would be called before the XML context is prepared
        if (updater != null)
            updater.start();
    }
    
    @Override
    public void destroy() {
    	if (updater != null) {
    		// updater is a user (not daemon) thread
    		// shut it down properly so container shutdown does not stall
    		updater.stop();
    	}
    }
    
    public ApiServlet() {
        super();
    }

}
