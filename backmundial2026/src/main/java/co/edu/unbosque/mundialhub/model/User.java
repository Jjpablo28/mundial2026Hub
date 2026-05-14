package co.edu.unbosque.mundialhub.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String userName;

	private String name;

	@Column(unique = true)
	private String email;

	private String password;

	private String identification;

	private String code;
	
	private String avatarUrl;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserSticker> stickers;
	
	private Integer totalPoints;
	private Integer packets;

	private LocalDateTime verificationExpires;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime lastLogin;

	private String favoriteTeam;
	private String country;
	private String city;
	


	

	@Enumerated(EnumType.STRING)
	private Role role;

	private boolean accountNonExpired;

	private boolean accountNonLocked;

	private boolean credentialsNonExpired;

	private boolean enabled;

	public User() {
		this.accountNonExpired = true;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.enabled = false;
		this.role = null;
	}

	public enum Role {
		USER, ADMIN, TEMPORAL, OPERATOR, SUPPORT
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User(Long id, String userName, String name, String email, String password, String identification,
			String code, String avatarUrl, Integer totalPoints, Integer packets, LocalDateTime verificationExpires,
			LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLogin, String favoriteTeam,
			String country, String city,  Role role, boolean accountNonExpired,
			boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled) {
		super();
		this.id = id;
		this.userName = userName;
		this.name = name;
		this.email = email;
		this.password = password;
		this.identification = identification;
		this.code = code;
		this.avatarUrl = avatarUrl;
		this.totalPoints = totalPoints;
		this.verificationExpires = verificationExpires;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.lastLogin = lastLogin;
		this.favoriteTeam = favoriteTeam;
		this.country = country;
		this.city = city;
		this.packets = packets;
		this.role = role;
		this.accountNonExpired = accountNonExpired;
		this.accountNonLocked = accountNonLocked;
		this.credentialsNonExpired = credentialsNonExpired;
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}
	
	

	public List<UserSticker> getStickers() {
		return stickers;
	}

	public void setStickers(List<UserSticker> stickers) {
		this.stickers = stickers;
	}

	public Integer getPackets() {
		return packets;
	}

	public void setPackets(Integer packets) {
		this.packets = packets;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
	    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getUsername() {
		return userName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, password, userName);
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getFavoriteTeam() {
		return favoriteTeam;
	}

	public void setFavoriteTeam(String favoriteTeam) {
		this.favoriteTeam = favoriteTeam;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(id, other.id) && Objects.equals(password, other.password)
				&& Objects.equals(userName, other.userName);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDateTime getVerificationExpires() {
		return verificationExpires;
	}

	public void setVerificationExpires(LocalDateTime verificationExpires) {
		this.verificationExpires = verificationExpires;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	

	public Integer getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(Integer totalPoints) {
		this.totalPoints = totalPoints;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", userName=" + userName + ", name=" + name + ", email=" + email + ", password="
				+ password + ", identification=" + identification + ", code=" + code + ", avatarUrl=" + avatarUrl
				+ ", stickers=" + stickers + ", totalPoints=" + totalPoints + ", packets=" + packets
				+ ", verificationExpires=" + verificationExpires + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + ", lastLogin=" + lastLogin + ", favoriteTeam=" + favoriteTeam + ", country=" + country
				+ ", city=" + city + ", role=" + role + ", accountNonExpired=" + accountNonExpired
				+ ", accountNonLocked=" + accountNonLocked + ", credentialsNonExpired=" + credentialsNonExpired
				+ ", enabled=" + enabled + "]";
	}



}
