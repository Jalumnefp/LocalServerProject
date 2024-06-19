package es.jfp.LocalServerProject.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FormatManager {
	
	private static FormatManager instance;
	
	private FormatManager() {}
	
	public static FormatManager getInstance() {
		synchronized (FormatManager.class) {
			if (instance == null) {
				instance = new FormatManager();
			}
			return instance;
		}
	}
	
	public String getFormatedConfigData(String ipv4, String port, String password, String storagePath) {
		return String.format("IPV4=%s\nPORT=%s\nPASSWORD=%s\nROOT_DIRECTORY=%s", ipv4, port, password, storagePath);
	}
	
	public Pattern validateIpv4Format() {
		String regex = "^([0-9]{1,3}(\\.[0-9]{1,3}){3})$";
		return getPattern(regex);
	}
	
	public Pattern validatePortFormat() {
		String regex = "^(\\d{1,5})$";
		return getPattern(regex);
	}
	
	public Pattern validatePasswordFormat() {
		String regex = "^([a-zA-Z0-9]+)$";
		return getPattern(regex);
	}
	
	public Pattern validatePathFormat() {
		String regex = "^([\\/\\\\a-zA-Z0-9:]+)$";
		return getPattern(regex);
	}
	
	private Pattern getPattern(String regex) {
		return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

}



