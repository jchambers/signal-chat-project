package com.eatthepath.signal.exercise.chat;

import com.eatthepath.signal.exercise.contacts.ContactService;
import com.eatthepath.signal.exercise.model.Chat;
import com.eatthepath.signal.exercise.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(InMemoryChatService.class);

    public InMemoryChatService(final ContactService contactService) {
        this.contactService = contactService;
    }

    @Override
    public void createChat(final Chat chat) throws UsersAreNotMutualContactsException, ChatAlreadyExistsException, IllegalUserCountException {
        final List<Long> participantIds = chat.getParticipantIds();

        if (participantIds.size() != 2) {
            throw new IllegalUserCountException();
        }

        if (!contactService.usersAreMutualContacts(participantIds.get(0), participantIds.get(1))) {
            throw new UsersAreNotMutualContactsException(participantIds.get(0), participantIds.get(1));
        }

        if (chatsById.containsKey(chat.getId())) {
            throw new ChatAlreadyExistsException(chat.getId());
        }

        chatsById.put(chat.getId(), chat);

        chatsByUserId.computeIfAbsent(participantIds.get(0), (userId) -> new ArrayList<>()).add(chat);
        chatsByUserId.computeIfAbsent(participantIds.get(1), (userId) -> new ArrayList<>()).add(chat);

        log.debug("Created {}", chat);
    }

    @Override
    public List<Chat> getChatsForUser(final long userId) {
        return Collections.unmodifiableList(chatsByUserId.getOrDefault(userId, Collections.emptyList()));
    }

    @Override
    public void postMessage(final long chatId, final Message message) throws ChatNotFoundException, IllegalMessageParticipantException {
        if (!chatsById.containsKey(chatId)) {
            throw new ChatNotFoundException(chatId);
        }

        final Chat chat = chatsById.get(chatId);

        if (!chat.getParticipantIds().contains(message.getSourceUserId()) || !chat.getParticipantIds().contains(message.getDestinationUserId())) {
            throw new IllegalMessageParticipantException();
        }

        messagesByChat.computeIfAbsent(chat, (key) -> new ConcurrentSkipListSet<>(MESSAGE_TIMESTAMP_COMPARATOR))
                .add(message);

        log.debug("Added message {} to chat {}", message.getId(), chat.getId());
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
