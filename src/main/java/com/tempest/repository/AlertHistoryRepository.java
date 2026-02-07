package com.tempest.repository;

import com.tempest.entity.AlertHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing AlertHistory entities.
 */
@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {

    /**
     * Find history for a specific alert, ordered by triggered timestamp descending.
     *
     * @param alertId  the alert ID
     * @param pageable pagination information
     * @return page of alert history records
     */
    Page<AlertHistory> findByAlertIdOrderByTriggeredAtDesc(Long alertId, Pageable pageable);

    /**
     * Find the most recent 100 history entries across all alerts.
     *
     * @return list of recent alert history records
     */
    List<AlertHistory> findTop100ByOrderByTriggeredAtDesc();

    /**
     * Count triggers for an alert after a specific time.
     *
     * @param alertId the alert ID
     * @param after   the timestamp to count after
     * @return count of triggers
     */
    long countByAlertIdAndTriggeredAtAfter(Long alertId, LocalDateTime after);

    /**
     * Delete old history entries before a specific timestamp.
     * Used for cleanup to maintain database size.
     *
     * @param before the cutoff timestamp
     */
    void deleteByTriggeredAtBefore(LocalDateTime before);
}
