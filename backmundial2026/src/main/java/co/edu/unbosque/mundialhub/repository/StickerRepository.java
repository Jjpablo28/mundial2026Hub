package co.edu.unbosque.mundialhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.mundialhub.model.Rarity;
import co.edu.unbosque.mundialhub.model.Sticker;
import co.edu.unbosque.mundialhub.model.Team;

public interface StickerRepository extends JpaRepository<Sticker,Long> {
	
	
	public List<Sticker> findByTeam(Team team);
	
	public List<Sticker> findByTeamId(Long idTeam);	
	
    public void deleteById(Long id);
    
	public Optional<Sticker> findById(Long stickerId);
	
	public List<Sticker> findByRarity(Rarity rarity);
	
    public Optional<Sticker> findByPlayerName(String playerName);

	
	



	

}
