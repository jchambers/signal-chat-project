package com.eatthepath.signal.exercise.chat;

import com.eatthepath.signal.exercise.model.Chat;
import com.eatthepath.signal.exercise.model.Message;

import java.util.List;

/**
 * The chat service is the main point of interaction with chat sessions. The chat service is responsible for managing
 * the lifecycle and content of chat sessions between two users.
 */
public interface ChatService {

    /**
     * Creates a new chat session between two users. Chat sessions may only be created between two users who are mutual
     * contacts, and can only be created if another chat with the same ID does not already exist.
     *
     * @param chat the chat session to be created
     * @throws UsersAreNotMutualContactsException if the users identified in chat creation request are not mutual
     * contacts
     * @throws ChatAlreadyExistsException if another chat with the same ID has already been created
     * @throws IllegalParticipantCountException if the number of users identified in the chat creation request is not exactly
     * two
     */
    void createChat(Chat chat) throws UsersAreNotMutualContactsException, ChatAlreadyExistsException, IllegalParticipantCountException;

    /**
     * Returns a list of chat sessions in which the identified user is a participant.
     *
     * @param userId the ID of the user for whom to retrieve a list of chat sessions
     * @return a list of chat sessions in which the identified user is a participant; may be empty
     */
    List<Chat> getChatsForUser(long userId);

    /**
     * Posts a new message to the identified chat session. If the message is a duplicate (i.e. it has exactly the same
     * ID, participants, content, and timestamp as another message in the same session), no action will be taken.
     *
     * @param chatId the ID of the chat session to which to append the given message
     * @param message the message to append to the identified chat session
     * @throws ChatNotFoundException if no chat session was found with the given ID
     * @throws IllegalMessageParticipantException if one or more of the identified participants in the given message are
     * not participants in the chat session
     */
    void postMessage(long chatId, Message message) throws ChatNotFoundException, IllegalMessageParticipantException;

    /**
     * Retrieves a chronologically-ordered list of messages associated with the identified chat session.
     *
     * @param chatId the ID of the chat session for which to retrieve messages
     * @return a chronologically-ordered (oldest first) list of messages associated with the identified chat session
     * @throws ChatNotFoundException if no chat session was found with the given ID
     */
    List<Message> getMessagesForChat(long chatId) throws ChatNotFoundException;
}
