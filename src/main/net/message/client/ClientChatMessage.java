package net.message.client;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ClientChatMessage implements ClientToServerMessage {
    private static final long serialVersionUID = 1L;
    private static final String CHAT_MSG_HEADER = "CHAT MSG:";
    @NotNull
    private final String chatMessage; // send new messages

    public ClientChatMessage() {
        this("");
    }

    public ClientChatMessage(String chatMessage) {
        if (chatMessage == null) chatMessage = "";
        this.chatMessage = chatMessage;
    }

    public @NotNull String getChatMessage() {
        return chatMessage;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return CHAT_MSG_HEADER + chatMessage;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        ClientChatMessage that = (ClientChatMessage) o;
        return chatMessage.equals(that.chatMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatMessage);
    }
}
