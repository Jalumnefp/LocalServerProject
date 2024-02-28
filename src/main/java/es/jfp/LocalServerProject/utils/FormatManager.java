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
		String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
		return getPattern(regex);
	}
	
	public Pattern validatePortFormat() {
		String regex = "^(?:[0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";
		return getPattern(regex);
	}
	
	public Pattern validatePasswordFormat() {
		String regex = "^(?=.*[A-Z])(?=.*\\d).{10,}$";
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
