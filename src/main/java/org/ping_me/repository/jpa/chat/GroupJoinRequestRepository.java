package org.ping_me.repository.jpa.chat;

import org.ping_me.model.chat.GroupJoinRequest;
import org.ping_me.model.constant.GroupJoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {
    Optional<GroupJoinRequest> findByRoom_IdAndRequester_Id(Long roomId, Long requesterId);

    List<GroupJoinRequest> findByRoom_IdOrderByCreatedAtDesc(Long roomId);

    List<GroupJoinRequest> findByRoom_IdAndStatusOrderByCreatedAtDesc(Long roomId, GroupJoinRequestStatus status);
}
