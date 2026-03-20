package co.edu.unbosque.mundialhub.dto;

public class LoguinRequestDTO {

	private String identifier;
	
	private String password;

	public LoguinRequestDTO() {
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
