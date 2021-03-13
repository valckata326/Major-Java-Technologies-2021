package bg.sofia.uni.fmi.mjt.socialmedia.content;

import bg.sofia.uni.fmi.mjt.socialmedia.IdIterator;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class BasicContent implements Content, Comparable<Content> {
    private String description;
    ContentType contentType;
    private String username;
    private String id;
    private LocalDateTime publishedOn;
    private int numberOfLikes;
    private int numberOfComments;
    private Set<String> tags;
    private Set<String> mentions;
    private String[] descriptionWords;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicContent that = (BasicContent) o;
        return getDescription().equals(that.getDescription())
                && getContentType() == that.getContentType()
                && getUsername().equals(that.getUsername())
                && getId().equals(that.getId())
                && getPublishedOn().equals(that.getPublishedOn());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDescription(), getContentType(), getUsername(), getId(), getPublishedOn());
    }

    BasicContent(String username, LocalDateTime publishedOn, String description) {
        this.id = username + "-" + IdIterator.id;
        IdIterator.id++;
        this.username = username;
        this.publishedOn = publishedOn;
        this.description = description;
        descriptionWords = description.split(" ");
        numberOfLikes = 0;
        numberOfComments = 0;
        mentions = new HashSet<>();
        tags = new HashSet<>();
        setTags();
        setMentions();
    }

    @Override
    public int getNumberOfLikes() {
        return this.numberOfLikes;
    }

    @Override
    public int getNumberOfComments() {
        return this.numberOfComments;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Collection<String> getTags() {
        return this.tags;
    }

    @Override
    public Collection<String> getMentions() {
        return this.mentions;
    }

    public String getUsername() {
        return username;
    }

    private void setTags() {
        for (String current : descriptionWords) {
            if (current.startsWith("#")) {
                tags.add(current);
            }
        }
    }

    private void setMentions() {
        for (String current : descriptionWords) {
            if (current.startsWith("@")) {
                mentions.add(current);
            }
        }
    }

    @Override
    public void incrementNumberOfLikes() {
        numberOfLikes++;
    }

    @Override
    public void incrementNumberOfComments() {
        numberOfComments++;
    }

    @Override
    public ContentType getContentType() {
        return this.contentType;
    }

    @Override
    public LocalDateTime getPublishedOn() {
        return this.publishedOn;
    }

    @Override
    public int compareTo(Content other) {
        if (this.getPublishedOn().isAfter(other.getPublishedOn())) {
            return -1;
        }
        return 1;
    }

    @Override
    public boolean isExpired() {
        if (getContentType() == ContentType.POST
                && getPublishedOn().plusDays(30).isBefore(LocalDateTime.now())) {
            return true;
        } else if (getContentType() == ContentType.STORY
                && getPublishedOn().plusHours(24).isBefore(LocalDateTime.now())) {
            return true;
        }
        return false;
    }

    public String getDescription() {
        return description;
    }
}
