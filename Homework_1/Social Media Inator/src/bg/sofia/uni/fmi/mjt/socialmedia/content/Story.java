package bg.sofia.uni.fmi.mjt.socialmedia.content;

import java.time.LocalDateTime;

public class Story extends BasicContent {

    public Story(String username, LocalDateTime publishedOn, String description) {
        super(username, publishedOn, description);
        contentType = ContentType.STORY;
    }
}
