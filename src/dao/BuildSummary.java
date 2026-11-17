package dao;

import java.time.LocalDateTime;

/** Riga di un elenco di build salvate (senza i componenti: per quelli si usa BuildDao.findById). */
public record BuildSummary(int id, String name, LocalDateTime createdAt) {
}
