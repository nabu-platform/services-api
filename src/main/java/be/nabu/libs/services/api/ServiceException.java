package be.nabu.libs.services.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import be.nabu.libs.services.ServiceRuntime;

public class ServiceException extends Exception {

	private static final long serialVersionUID = -9187883601550297628L;
	
	private Object [] arguments;
	
	private String code;
	
	public String bundleName = "exceptions";
	
	private List<String> serviceStack = new ArrayList<String>();
	
	private void calculateServiceStack() {
		ServiceRuntime runtime = ServiceRuntime.getRuntime();
		while (runtime != null) {
			if (runtime.getService() instanceof DefinedService) {
				serviceStack.add(((DefinedService) runtime.getService()).getId());
			}
			runtime = runtime.getParent();
		}
	}
	
	public ServiceException(String code, String message, Throwable cause, Object...arguments) {
		super(message, cause);
		this.code = code;
		this.arguments = arguments;
		calculateServiceStack();
	}
	
	public ServiceException(String code, String message, Object...arguments) {
		super(message);
		this.code = code;
		calculateServiceStack();
	}
	
	public ServiceException(Throwable cause, Object...arguments) {
		super(cause);
		this.arguments = arguments;
		calculateServiceStack();
	}

	@Override
	public String getLocalizedMessage() {
		String message;
		try {
			// bundles are cached by default
			ResourceBundle bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
			message = bundle.getString(code == null ? super.getMessage() : code);
			if (message == null) {
				message = serviceStack + " - " + (code == null ? super.getMessage() : code) + ": " + Arrays.asList(arguments);
			}
			else {
				message = serviceStack + " - " + String.format(message, arguments);
			}
		}
		catch (MissingResourceException e) {
			message = serviceStack + (code != null ? " - " + code : "")  + (super.getMessage() != null ? " - " + super.getMessage() : "") + " (no bundle: " + bundleName + ")";
		}
		return message;
	}

	@Override
	public String getMessage() {
		return getLocalizedMessage();
	}
	
	public String getPlainMessage() {
		return super.getMessage();
	}
	
	public List<String> getServiceStack() {
		return serviceStack;
	}

	public String getCode() {
		return code == null ? "SYSTEM-0" : code;
	}
	
}
