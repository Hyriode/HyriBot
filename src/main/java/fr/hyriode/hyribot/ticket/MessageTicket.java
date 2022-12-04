package fr.hyriode.hyribot.ticket;

import net.dv8tion.jda.api.entities.Message;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MessageTicket {

    private final long messageId;
    private final long authorId;
    private final String content;
    private final String pseudo;
    private final String tag;
    private final String avatarUrl;
    private final long timeCreated;
    private final long timeEdited;
    private final List<Attachment> attachments;

    public MessageTicket(Message message) {
        this.messageId = message.getIdLong();
        this.authorId = message.getAuthor().getIdLong();
        this.content = message.getContentRaw();
        this.pseudo = message.getAuthor().getName();
        this.tag = message.getAuthor().getAsTag();
        this.avatarUrl = message.getAuthor().getAvatarUrl();
        this.timeCreated = message.getTimeCreated().toInstant().toEpochMilli();
        OffsetDateTime timeEdited = message.getTimeEdited();
        this.timeEdited = timeEdited != null ? timeEdited.toInstant().toEpochMilli() : -1L;
        this.attachments = message.getAttachments().stream().map(Attachment::new).collect(Collectors.toList());
    }

    public long getMessageId() {
        return messageId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getTag() {
        return tag;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public long getTimeEdited() {
        return timeEdited;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    static class Attachment {
        private final long id;
        private final String url;
        private final String proxyUrl;
        private final String fileName;
        private final String contentType;
        private final String description;
        private final int size;
        private final int height;
        private final int width;
        private final boolean ephemeral;

        public Attachment(net.dv8tion.jda.api.entities.Message.Attachment attachment) {
            this.id = attachment.getIdLong();
            this.url = attachment.getUrl();
            this.proxyUrl = attachment.getProxyUrl();
            this.fileName = attachment.getFileName();
            this.contentType = attachment.getContentType();
            this.description = attachment.getDescription();
            this.size = attachment.getSize();
            this.height = attachment.getHeight();
            this.width = attachment.getWidth();
            this.ephemeral = attachment.isEphemeral();
        }

        public Attachment(long id, String url, String proxyUrl, String fileName, String contentType, String description, int size, int height, int width, boolean ephemeral) {
            this.id = id;
            this.url = url;
            this.proxyUrl = proxyUrl;
            this.fileName = fileName;
            this.contentType = contentType;
            this.description = description;
            this.size = size;
            this.height = height;
            this.width = width;
            this.ephemeral = ephemeral;
        }

        public long getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public String getProxyUrl() {
            return proxyUrl;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public String getDescription() {
            return description;
        }

        public int getSize() {
            return size;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public boolean isEphemeral() {
            return ephemeral;
        }
    }


}
