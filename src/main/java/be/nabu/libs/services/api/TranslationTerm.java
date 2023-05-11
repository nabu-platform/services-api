package be.nabu.libs.services.api;

import java.util.List;
import java.util.UUID;

public interface TranslationTerm {
	public UUID getInstanceId();
	public String getTerm();
	// you can add tags to further clarify things like context
	// they can be relevant if multiple matches are found
	public List<String> getTags();
}
