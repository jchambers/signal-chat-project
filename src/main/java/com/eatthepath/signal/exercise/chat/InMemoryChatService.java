package com.eatthepath.signal.exercise.chat;

import com.eatthepath.signal.exercise.contacts.ContactService;
import com.eatthepath.signal.exercise.model.Chat;
import com.eatthepath.signal.exercise.model.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class InMemoryChatService implements ChatService {

    private final ContactService contactService;

    private final Map<Long, Chat> chatsById = new ConcurrentHashMap<>();
    private final Map<Long, List<Chat>> chatsByUserId = new ConcurrentHashMap<>();

    private final Map<Chat, SortedSet<Message>> messagesByChat = new ConcurrentHashMap<>();

    private static final Comparator<Message> MESSAGE_TIMESTAMP_COMPARATOR =
            Comparator.comparing(Message::getTimestamp);

    public InMemoryChatService(final ContactService contactService) {
        this.contactService = contactService;
    }

    @Override
    public void createChat(final Chat chat) throws UsersAreNotMutualContactsException, ChatAlreadyExistsException, IllegalUserCountException {
        final long[] participantIds = chat.getParticipantIds();

        if (participantIds.length != 2) {
            throw new IllegalUserCountException();
        }

        if (!contactService.usersAreMutualContacts(participantIds[0], participantIds[1])) {
            throw new UsersAreNotMutualContactsException(participantIds[0], participantIds[1]);
        }

        if (chatsById.containsKey(chat.getId())) {
            throw new ChatAlreadyExistsException(chat.getId());
        }

        chatsById.put(chat.getId(), chat);

        chatsByUserId.computeIfAbsent(participantIds[0], (userId) -> new ArrayList<>()).add(chat);
        chatsByUserId.computeIfAbsent(participantIds[1], (userId) -> new ArrayList<>()).add(chat);
    }

    @Override
    public List<Chat> getChatsForUser(final long userId) {
        return Collections.unmodifiableList(chatsByUserId.getOrDefault(userId, Collections.emptyList()));
    }

    @Override
    public void postMessage(final Message message) throws ChatNotFoundException {
        final Chat chat = chatsByUserId.getOrDefault(message.getSourceUserId(), Collections.emptyList())
                .stream()
                .filter(candidate -> candidate.getParticipantIds()[0] == message.getDestinationUserId() ||
                        candidate.getParticipantIds()[1] == message.getDestinationUserId())
                .findFirst()
                .orElseThrow(() -> new ChatNotFoundException(message.getSourceUserId(), message.getDestinationUserId()));

        messagesByChat.computeIfAbsent(chat, (key) -> new ConcurrentSkipListSet<>(MESSAGE_TIMESTAMP_COMPARATOR))
                .add(message);
    }

    @Override
    public List<Message> getMessagesForChat(final long chatId) throws ChatNotFoundException {
        if (!chatsById.containsKey(chatId)) {
            throw new ChatNotFoundException(chatId);
        }

        return Collections.unmodifiableList(
                new ArrayList<>(messagesByChat.getOrDefault(chatsById.get(chatId), Collections.emptySortedSet())));
    }
}
