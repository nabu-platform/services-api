package be.nabu.libs.services.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.validator.api.Validation;

public class ServiceException extends Exception {

	private static final long serialVersionUID = -9187883601550297628L;
	
	private Object [] arguments;
	
	private String code, description;
	
	public String bundleName = "exceptions";
	
	private List<String> serviceStack = new ArrayList<String>();
	
	private List<? extends Validation<?>> validations;
	
	private Token token;
	
	private String id;
	
	// whether or not this exception has been reported already
	private boolean reported;
	
	private void calculateServiceStack() {
		ServiceRuntime runtime = ServiceRuntime.getRuntime();
		while (runtime != null) {
			if (token == null) {
				token = runtime.getExecutionContext().getSecurityContext().getToken();
			}
			if (runtime.getService() instanceof DefinedService) {
				serviceStack.add(((DefinedService) runtime.getService()).getId());
			}
			else if (runtime.getService() instanceof ServiceWrapper) {
				Service unwrap = ServiceUtils.unwrap(runtime.getService());
				if (unwrap instanceof DefinedService) {
					serviceStack.add(((DefinedService) unwrap).getId());	
				}
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

	public String getCoreMessage() {
		String message = super.getMessage();
		if (message == null) {
			message = getLocalizedMessage();
		}
		return message;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public List<? extends Validation<?>> getValidations() {
		return validations;
	}

	public void setValidations(List<? extends Validation<?>> validations) {
		this.validations = validations;
	}

	public boolean isReported() {
		return reported;
	}

	public void setReported(boolean reported) {
		this.reported = reported;
	}

	public String getId() {
		if (id == null) {
			id = UUID.randomUUID().toString().replace("-", "");
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
