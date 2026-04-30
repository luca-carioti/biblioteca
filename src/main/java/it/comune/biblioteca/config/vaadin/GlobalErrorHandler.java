package it.comune.biblioteca.config.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import it.comune.biblioteca.enums.ExceptionCodeEnum;
import it.comune.biblioteca.exception.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public void error(ErrorEvent event) {
	Throwable cause = event.getThrowable();
	log.error("Unhandled error: {}", cause.getMessage(), cause);

	String message = extractMessage(cause);

	// Must run on UI thread
	UI ui = UI.getCurrent();
	if (ui != null) {
	    ui.access(() -> showError(message));
	} else {
	    showError(message);
	}
    }

    private String extractMessage(Throwable cause) {
	// Unwrap to find your PersistenceException if wrapped
	Throwable current = cause;
	while (current != null) {
	    if (current instanceof PersistenceException pe) {
		return pe.getCode().getDescription();
	    }
	    current = current.getCause();
	}
	return ExceptionCodeEnum.G_000.getDescription();
    }

    private void showError(String message) {
	Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
	notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}