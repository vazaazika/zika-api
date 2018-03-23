package br.les.opus.dengue.gamification.controllers;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.les.opus.gamification.domain.Player;
import br.les.opus.gamification.domain.pojos.PlayerRecords;
import br.les.opus.gamification.repositories.PerformedTaskRepository;
import br.les.opus.gamification.repositories.PlayerRepository;
import br.les.opus.gamification.services.GamificationService;

@RestController
@Transactional
@RequestMapping("/game/player")
public class RecordsController {
	
	@Autowired
	private PerformedTaskRepository pTaskDao;
	
	@Autowired
	private PlayerRepository playerDao;
	
	@Autowired
	private GamificationService gameService;
	
	@RequestMapping(value="{playerId}/xp/records", method = RequestMethod.GET) 
	public ResponseEntity<PlayerRecords> playerRecords(@PathVariable Long playerId) {
		Player player = playerDao.findOne(playerId);
		if (player == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		Date begin = LocalDateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).toDate();
		Date end = LocalDateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).toDate();
		
		PlayerRecords records = new PlayerRecords();
		records.setTodayXp(pTaskDao.sumXpByPlayerAndInterval(player, begin, end));
		records.setTotalXp(player.getXp());
		records.setBestDayRecord(pTaskDao.dailyRecord(player));
		return new ResponseEntity<>(records, HttpStatus.OK);
	}
	
	@RequestMapping(value="self/xp/records", method = RequestMethod.GET) 
	public ResponseEntity<PlayerRecords> playerSelfRecords(HttpServletRequest request) {
		Player player = gameService.loadPlayer(request);
		return playerRecords(player.getId());
	}
	
}
