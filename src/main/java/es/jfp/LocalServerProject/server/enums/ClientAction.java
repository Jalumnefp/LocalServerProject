package es.jfp.LocalServerProject.server.enums;

public enum ClientAction {
	CLOSE_SESSION(-1),
	REGISTER(01),
	LOGIN(02),
	LOGOFF(03),
	READ_DIRECTORY(11),
	CREATE_FOLDER(12), 
	DELETE_FOLDER(14),
	UPLOAD_FILE(21), 
	DOWNLOAD_FILE(22), 
	DELETE_FILE(24),
	PING(30);
	
	private final int value;
	
	ClientAction(int value) {
		this.value = value;
	}
	
	private int getCode() {
		return this.value;
	}
	
	public static ClientAction getByCode(int code) {
		ClientAction action = null;
		for (ClientAction a: ClientAction.values()) {
			if (a.getCode() == code)  {
				action = a;
			}
		}
		return action;
	}
}
