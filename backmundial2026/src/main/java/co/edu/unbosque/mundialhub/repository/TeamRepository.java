package co.edu.unbosque.mundialhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.mundialhub.model.Team;
import co.edu.unbosque.mundialhub.model.User;

public interface TeamRepository extends JpaRepository<Team, Long>{

	public Optional<Team> findByName(String name);
	
	public Optional<Team> findByShortName(String shortName);

    public void deleteByName(String name);
    
    public void deleteById(Long id);

    public Optional<User> findByGroupName(String email);

	public Optional<User> findById(Integer userId);
	
	List<Team> findAllByOrderByNameAsc();
	
	List<Team> findAllByOrderByNameDesc();
	
	
}
