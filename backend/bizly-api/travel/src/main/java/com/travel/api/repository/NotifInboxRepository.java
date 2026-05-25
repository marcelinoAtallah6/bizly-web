package com.travel.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travel.api.model.NotifInbox;

@Repository
public interface NotifInboxRepository extends JpaRepository<NotifInbox, Long> {

	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NotifInbox n "
			+ "WHERE n.username = :username AND n.resourceType = :rt AND n.resourceId = :rid "
			+ "AND (n.businessId IS NULL OR n.businessId = :businessId)")
	boolean existsForUserAndBusiness(@Param("username") String username,
			@Param("rt") String resourceType, @Param("rid") String resourceId,
			@Param("businessId") Long businessId);

	boolean existsByUsernameAndResourceTypeAndResourceId(String username, String resourceType, String resourceId);
}
