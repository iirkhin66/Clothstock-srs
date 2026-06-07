package ru.vstu.clothstock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vstu.clothstock.model.HistoryLog;

@Repository
public interface HistoryLogRepository extends JpaRepository<HistoryLog, Long> {
}