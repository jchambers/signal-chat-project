package com.eatthepath.signal.exercise.chat;

import com.eatthepath.signal.exercise.model.Chat;
import com.eatthepath.signal.exercise.model.Message;

import java.util.List;

public interface ChatService {

    void createChat(Chat chat) throws UsersAreNotMutualContactsException, ChatAlreadyExistsException, IllegalUserCountException;

    List<Chat> getChatsForUser(long userId);

    void postMessage(Message message) throws ChatNotFoundException;

    List<Message> getMessagesForChat(long chatId) throws ChatNotFoundException;
}
