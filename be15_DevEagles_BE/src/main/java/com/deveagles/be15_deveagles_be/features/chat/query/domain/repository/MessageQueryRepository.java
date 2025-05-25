package com.deveagles.be15_deveagles_be.features.chat.query.domain.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import java.util.List;
import java.util.Map;

public interface MessageQueryRepository {

  List<ChatMessage> findMessages(String chatroomId, String beforeMessageId, int limit);

  Map<String, Object> getMessageReadStatus(String chatroomId, String messageId);
}
