package be.nabu.libs.services.api;

import java.util.List;

public interface LanguageContext {
	public String getDefaultLanguage();
	public String getCurrentLanguage();
	public List<TranslatedTerm> translate(String language, List<TranslationTerm> terms);
	public void persist(List<TranslatedTerm> translations);
}
