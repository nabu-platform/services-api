package be.nabu.libs.services;

import java.util.List;
import java.util.Map;

public class NarrativeParts {
	private List<String> ids;
	private Map<String, String> values;
	public List<String> getIds() {
		return ids;
	}
	public void setIds(List<String> ids) {
		this.ids = ids;
	}
	public Map<String, String> getValues() {
		return values;
	}
	public void setValues(Map<String, String> values) {
		this.values = values;
	}
}
