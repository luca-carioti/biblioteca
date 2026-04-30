package it.comune.biblioteca.config.vaadin;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

@Component
public class AppServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
	event.getSource().addSessionInitListener(sessionInitEvent ->
		sessionInitEvent.getSession().setErrorHandler(new GlobalErrorHandler())
	);
    }
}