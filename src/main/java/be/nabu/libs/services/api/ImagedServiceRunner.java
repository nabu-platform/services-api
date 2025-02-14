package be.nabu.libs.services.api;

import java.util.Date;

public interface ImagedServiceRunner extends ServiceRunner {
	public Date getImageDate();
	public String getImageName();
	public String getImageVersion();
	public String getImageEnvironment();
}
