import { formatLastMessageTime } from './timeUtils';

export function getChatTypeClass(type) {
  switch (type) {
    case 'AI':
      return 'bg-[var(--color-primary-400)]';
    case 'TEAM':
      return 'bg-[var(--color-success-300)]';
    case 'GROUP':
      return 'bg-[var(--color-warning-300)]';
    case 'DIRECT':
    default:
      return 'bg-[var(--color-primary-300)]';
  }
}

export function getChatDisplayChar(chat) {
  if (chat.type === 'AI') {
    return '🤖';
  }
  if (chat.type === 'TEAM') {
    return '👥';
  }
  return chat.name?.charAt(0)?.toUpperCase() || '?';
}

export function getLastMessageDisplay(chat) {
  if (!chat.lastMessage) {
    if (chat.type === 'AI') {
      return 'AI와 대화를 시작해보세요!';
    }
    return '메시지가 없습니다.';
  }
  return chat.lastMessage;
}

export function getDisplayName(room, otherParticipant) {
  if (room.type === 'AI') {
    return '🤖 AI 어시스턴트';
  }

  if (room.type === 'GROUP') {
    return room.name || '그룹 채팅';
  }

  return otherParticipant?.userName || '알 수 없는 사용자';
}

export function groupChatsByDate(chats, getDateGroupKey) {
  const grouped = {};

  chats.forEach(chat => {
    const groupKey = getDateGroupKey(chat.lastMessageTime);
    if (!grouped[groupKey]) {
      grouped[groupKey] = [];
    }
    grouped[groupKey].push(chat);
  });

  const orderedGroups = {};
  const groupOrder = ['오늘', '어제', '이번 주', '이번 달', '이전', '기타'];

  groupOrder.forEach(key => {
    if (grouped[key]) {
      orderedGroups[key] = grouped[key];
    }
  });

  return orderedGroups;
}

export function groupChatsByType(chats) {
  const grouped = {
    AI: [],
    TEAM: [],
    DIRECT: [],
    GROUP: [],
  };

  chats.forEach(chat => {
    const type = chat.type || 'DIRECT';
    if (grouped[type]) {
      grouped[type].push(chat);
    }
  });

  return grouped;
}

export function filterChats(chats, searchTerm) {
  if (!searchTerm) return chats;

  const term = searchTerm.toLowerCase();
  return chats.filter(
    chat =>
      chat.name?.toLowerCase().includes(term) || chat.lastMessage?.toLowerCase().includes(term)
  );
}

export function sortChats(chats, sortBy = 'time', order = 'desc') {
  const sorted = [...chats].sort((a, b) => {
    let comparison = 0;

    switch (sortBy) {
      case 'time': {
        const timeA = new Date(a.lastMessageTimestamp || a.lastMessageTime || 0);
        const timeB = new Date(b.lastMessageTimestamp || b.lastMessageTime || 0);
        comparison = timeA - timeB;
        break;
      }
      case 'name':
        comparison = (a.name || '').localeCompare(b.name || '');
        break;
      case 'unread':
        comparison = (a.unreadCount || 0) - (b.unreadCount || 0);
        break;
      default:
        return 0;
    }

    return order === 'desc' ? -comparison : comparison;
  });

  return sorted;
}

export function getTotalUnreadCount(chats) {
  return chats.reduce((total, chat) => total + (chat.unreadCount || 0), 0);
}

export function transformChatRoom(room, currentUserId, teamMembers = [], currentTeam = null) {
  const otherParticipants = room.participants?.filter(p => p.userId !== currentUserId) || [];
  const otherParticipant = otherParticipants[0];
  const currentUserParticipant = room.participants?.find(p => p.userId === currentUserId);

  let thumbnail = null;
  let displayName = room.name;

  if (room.type === 'AI') {
    thumbnail = '/assets/image/suri.jpg';
    displayName = '수리AI';
  } else if (room.type === 'TEAM') {
    // 팀 전체 채팅방인 경우 - 팀 썸네일 사용
    displayName = room.name || '팀 전체 채팅방';
    thumbnail = currentTeam?.url || currentTeam?.thumbnailUrl || null; // 팀 썸네일 사용
  } else if (room.type === 'DIRECT' && otherParticipant) {
    // 1:1 채팅방인 경우 항상 상대방 이름을 표시
    const memberInfo = teamMembers.find(
      member => String(member.userId) === String(otherParticipant.userId)
    );

    if (memberInfo) {
      // 팀원 정보가 있으면 해당 정보 사용
      displayName = memberInfo.userName || memberInfo.nickname || '알 수 없는 사용자';
      thumbnail = memberInfo.userThumbnailUrl || memberInfo.profileImageUrl || null;
    } else {
      // 팀원 정보가 없으면 기본값 사용 (다른 팀 사용자일 가능성)
      displayName = otherParticipant?.userName || '다른 팀 사용자';
      thumbnail = otherParticipant?.userThumbnail || otherParticipant?.userThumbnailUrl || null;

      console.warn('[transformChatRoom] 현재 팀에 없는 사용자:', {
        roomId: room.id,
        roomType: room.type,
        otherParticipantId: otherParticipant?.userId,
        otherParticipantName: otherParticipant?.userName,
        teamMemberCount: teamMembers.length,
        teamMemberIds: teamMembers.map(m => m.userId),
      });
    }

    console.log('[transformChatRoom] 1:1 채팅방 이름 설정:', {
      roomId: room.id,
      roomName: room.name,
      displayName: displayName,
      otherParticipantId: otherParticipant?.userId,
      memberInfo: memberInfo ? 'found' : 'not found',
    });
  } else if (room.type === 'GROUP') {
    displayName = room.name || '그룹 채팅';
    thumbnail = null; // 그룹 채팅은 기본 아바타 사용
  } else {
    // 기타 경우
    displayName = getDisplayName(room, otherParticipant);
    thumbnail = otherParticipant?.userThumbnail || otherParticipant?.userThumbnailUrl || null;
  }

  return {
    id: room.id,
    name: displayName,
    type: room.type,
    isOnline: otherParticipant?.isOnline || false,
    thumbnail: thumbnail,
    lastMessage: room.lastMessage?.content || '',
    lastMessageTime: formatLastMessageTime(room.lastMessage?.timestamp),
    lastMessageTimestamp: room.lastMessage?.timestamp,
    unreadCount: room.unreadCount || 0,
    participants: room.participants || [],
    notificationEnabled: currentUserParticipant?.notificationEnabled ?? true,
    createdAt: room.createdAt,
    updatedAt: room.updatedAt,
    isAiChat: room.type === 'AI',
    currentUserId: currentUserId, // 현재 사용자 ID 추가
    teamId: room.teamId, // 팀 ID 추가
  };
}

/**
 * AI 채팅방인지 확인
 */
export function isAiChatRoom(chat) {
  return chat?.type === 'AI' || chat?.isAiChat === true;
}

/**
 * AI 채팅방 기본 데이터 생성
 */
export function createAiChatRoomData(userId, aiName = '수리 AI') {
  return {
    type: 'AI',
    name: aiName,
    userId: userId,
    isAiChat: true,
    thumbnail: '/assets/image/suri.jpg',
    lastMessage: 'AI와 대화를 시작해보세요!',
    lastMessageTime: '방금 전',
    unreadCount: 0,
    participants: [
      {
        userId: userId,
        notificationEnabled: true,
      },
      {
        userId: 'ai-assistant',
        userName: aiName,
        isOnline: true,
      },
    ],
  };
}
