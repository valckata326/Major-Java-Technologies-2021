package bg.sofia.uni.fmi.mjt.socialmedia.content;

import java.time.LocalDateTime;
import java.util.Collection;

public interface Content extends Comparable<Content> {
    /**
     * Returns the total number of likes.
     */
    int getNumberOfLikes();

    /**
     * Returns the total number of comments.
     */
    int getNumberOfComments();

    /**
     * Returns the unique id of the content
     */
    String getId();

    /**
     * Returns a Collection of all tags used in the description.
     * Аll tags should start with '#'.
     */
    Collection<String> getTags();

    /**
     * Returns a Collection of all users mentioned in the description.
     * Аll mentions should start with '@'.
     */
    Collection<String> getMentions();

    LocalDateTime getPublishedOn();

    void incrementNumberOfLikes();

    void incrementNumberOfComments();

    boolean isExpired();

    ContentType getContentType();

}