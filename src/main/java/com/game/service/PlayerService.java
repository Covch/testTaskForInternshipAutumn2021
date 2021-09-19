package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import com.game.specification.PlayerSpecification;
import com.game.specification.PlayerSpecificationBuilder;
import com.game.specification.SearchCriteria;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

//    public ResponseEntity<List<Player>> getAll (List<SearchCriteria> filters, Pageable pageable) {
//        Page<Player> pagePlayers;
//        pagePlayers = playerSpecification.getQueryResult(filters, pageable);
//
//        List<Player> players;
//        players = pagePlayers.getContent();
//
//        return new ResponseEntity<>(players, HttpStatus.OK);
//    }

//    public ResponseEntity<Integer> getPlayersCount(List<SearchCriteria> filters, Pageable pageable) {
//
//    }

    public List<Player> getAllWithFilters(Map<String, String> allParams) {
        Pageable pageable = getPageable(allParams);
        List<Player> list;
        if (allParams.size() > 0) {
            list = playerRepository.findAll(getSpecification(allParams), pageable).getContent();
        } else {
            list = playerRepository.findAll(pageable).getContent();
        }
        return list;

    }

    public Integer getAllWithFiltersCount(Map<String, String> allParams) {
        Pageable pageable = getPageable(allParams);
        int size = 0;
        if (allParams.size() > 0) {
            size = (int) playerRepository.findAll(getSpecification(allParams), pageable).getTotalElements();
        } else {
            size = (int) playerRepository.findAll(pageable).getTotalElements();
        }
        return size;
    }

    public Optional<Player> getPlayerById(long id) {
        return playerRepository.findById(id);
    }

    public Player save(Player player) {
        player.setLevel(calculateLevel(player.getExperience()));
        player.setUntilNextLevel(calculateExperienceTillNextLevel(player.getLevel(), player.getExperience()));
        return playerRepository.save(player);
    }

    public Player update(Player updatablePlayer,Player player) {
        if (updatablePlayer.getName() != null) player.setName(updatablePlayer.getName());
        if (updatablePlayer.getTitle() != null) player.setTitle(updatablePlayer.getTitle());
        if (updatablePlayer.getRace() != null) player.setRace(updatablePlayer.getRace());
        if (updatablePlayer.getProfession() != null)
            player.setProfession(updatablePlayer.getProfession());
        if (updatablePlayer.getBirthday() != null)
            player.setBirthday(updatablePlayer.getBirthday());
        if (updatablePlayer.isBanned() != null) player.setBanned(updatablePlayer.isBanned());
        if (updatablePlayer.getExperience() != null) {
            player.setExperience(updatablePlayer.getExperience());
            player.setLevel(calculateLevel(player.getExperience()));
            player.setUntilNextLevel(calculateExperienceTillNextLevel(player.getLevel(), player.getExperience()));
        }
        return player;
    }

    public void delete(Player player) {
        playerRepository.delete(player);
    }

    private Pageable getPageable (Map<String, String> allParams) {
        int pageNumber, pageSize;
        PlayerOrder playerOrder;
        if (allParams.containsKey("pageNumber")) {
            pageNumber = Integer.parseInt(allParams.get("pageNumber"));
        } else {
            pageNumber = 0;
        }
        if (allParams.containsKey("pageSize")) {
            pageSize = Integer.parseInt(allParams.get("pageSize"));
        } else  {
            pageSize = 3;
        }
        if (allParams.containsKey("playOrder")) {
            playerOrder = PlayerOrder.valueOf(allParams.get("playOrder"));
        } else {
            playerOrder = PlayerOrder.ID;
        }
        Sort sort = Sort.by(playerOrder.getFieldName());
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        return pageRequest;
    }

    private Specification<Player> getSpecification(Map<String, String> allParams) {
        PlayerSpecificationBuilder playerSpecificationBuilder = new PlayerSpecificationBuilder();
        for (Map.Entry<String, String> entry: allParams.entrySet()
             ) {
            if (entry.getValue() != null && !entry.getKey().equals("pageNumber") && !entry.getKey().equals("pageSize") && !entry.getKey().equals("playOrder")) {
                playerSpecificationBuilder.with(new PlayerSpecification(new SearchCriteria(entry.getKey(), entry.getValue())));
            }
        }
        return playerSpecificationBuilder.build();
    }

    private int calculateLevel(int experience) {
        return (int) ((Math.sqrt(2500 + 200.0 * experience) - 50) / 100);
    }

    private int calculateExperienceTillNextLevel(int currentLvl, int experience) {
        return 50 * (currentLvl + 1) * (currentLvl + 2) - experience;
    }


}
