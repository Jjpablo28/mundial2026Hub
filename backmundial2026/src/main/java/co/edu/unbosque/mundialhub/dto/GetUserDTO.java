package co.edu.unbosque.mundialhub.dto;

import co.edu.unbosque.mundialhub.model.User.Role;

public class GetUserDTO {

	private String name;
	private String userName;
	private String identification;
	private String email;
	private String favoriteTeam;
	private Role role;
	private String city;
	private String country;

	public GetUserDTO(String name, String userName, String id, String email, String favoriteTeam, Role role,
			String city, String country) {
		super();
		this.name = name;
		this.userName = userName;
		this.identification = id;
		this.email = email;
		this.favoriteTeam = favoriteTeam;
		this.role = role;
		this.city = city;
		this.country = country;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String id) {
		this.identification = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFavoriteTeam() {
		return favoriteTeam;
	}

	public void setFavoriteTeam(String favoriteTeam) {
		this.favoriteTeam = favoriteTeam;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

}
