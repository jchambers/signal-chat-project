package com.eatthepath.signal.exercise.chat;

import com.eatthepath.signal.exercise.contacts.ContactService;
import com.eatthepath.signal.exercise.model.Chat;
import com.eatthepath.signal.exercise.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class InMemoryChatServiceTest {

    @Mock
    private ContactService contactService;

    @InjectMocks
    private InMemoryChatService chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createChat() throws Exception {
        final Chat chat = new Chat(1, new long[] {1, 2});

        when(contactService.usersAreMutualContacts(anyLong(), anyLong())).thenReturn(false);
        assertThrows(UsersAreNotMutualContactsException.class, () -> chatService.createChat(chat));

        when(contactService.usersAreMutualContacts(anyLong(), anyLong())).thenReturn(true);
        chatService.createChat(chat);

        assertThrows(ChatAlreadyExistsException.class, () -> chatService.createChat(chat));
    }

    @Test
    void getChatsForUser() throws Exception {
        assertTrue(chatService.getChatsForUser(1).isEmpty());

        final Chat chat = new Chat(1, new long[] {1, 2});

        when(contactService.usersAreMutualContacts(anyLong(), anyLong())).thenReturn(true);
        chatService.createChat(chat);

        final List<Chat> expectedChats = Collections.singletonList(chat);

        assertEquals(expectedChats, chatService.getChatsForUser(1));
        assertEquals(expectedChats, chatService.getChatsForUser(2));
    }

    @Test
    void postMessage() throws Exception {
        when(contactService.usersAreMutualContacts(anyLong(), anyLong())).thenReturn(true);

        {
            final Message message = new Message("test-message-id", Instant.now(), "This is only a test", 1, 2);

            assertThrows(ChatNotFoundException.class, () -> chatService.postMessage(message));

            final Chat chat = new Chat(1, new long[]{1, 2});

            assertThrows(ChatNotFoundException.class, () -> chatService.getMessagesForChat(chat.getId()));

            chatService.createChat(chat);
            chatService.postMessage(message);

            final List<Message> expectedMessages = Collections.singletonList(message);

            assertEquals(expectedMessages, chatService.getMessagesForChat(chat.getId()));
        }

        {
            final Message messageToOneUnknownUser =
                    new Message("one-unknown-user", Instant.now(), "This is only a test", 1, 3);

            assertThrows(ChatNotFoundException.class, () -> chatService.postMessage(messageToOneUnknownUser));
        }
    }

    @Test
    void getMessagesForChat() throws Exception {
        final Chat chat = new Chat(1, new long[]{1, 2});

        when(contactService.usersAreMutualContacts(anyLong(), anyLong())).thenReturn(true);
        chatService.createChat(chat);

        final Instant now = Instant.now();

        final Message earlierMessage = new Message("earlier", now.minusMillis(1000), "This message was sent first, but arrived second", 1, 2);
        final Message laterMessage = new Message("later", now, "This message was sent second, but arrived first", 1, 2);

        chatService.postMessage(laterMessage);
        chatService.postMessage(earlierMessage);

        final List<Message> expectedMessages = Arrays.asList(earlierMessage, laterMessage);

        assertEquals(expectedMessages, chatService.getMessagesForChat(chat.getId()));
    }
}