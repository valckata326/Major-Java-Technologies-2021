package bg.sofia.uni.fmi.mjt.socialmedia.content;

import java.time.LocalDateTime;

public class Post extends BasicContent {
    public Post(String username, LocalDateTime publishedOn, String description) {
        super(username, publishedOn, description);
        this.contentType = ContentType.POST;
    }
}
