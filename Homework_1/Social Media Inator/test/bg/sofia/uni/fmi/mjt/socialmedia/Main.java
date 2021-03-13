package bg.sofia.uni.fmi.mjt.socialmedia;
import bg.sofia.uni.fmi.mjt.socialmedia.content.Content;
import bg.sofia.uni.fmi.mjt.socialmedia.customcomparators.DateTimeComparator;
import java.time.LocalDateTime;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        /*Content test1 = new Story("valiopralio", "This is my first string with 3 mentions"
                + " @valio @pralio @mentions #firstTime #hopeToSucceed #CoronaFun");
        System.out.println(test1.getId());
        System.out.println(test1.getMentions());
        System.out.println(test1.getNumberOfComments());
        System.out.println(test1.getNumberOfLikes());
        System.out.println(test1.getTags());

        Content test2 = new Post("valiopralio2",
                "This is my second string with 0 mentions and 2 hashtags #first #second");
        System.out.println(test2.getId());
        System.out.println(test2.getMentions());
        System.out.println(test2.getNumberOfComments());
        System.out.println(test2.getNumberOfLikes());
        System.out.println(test2.getTags());
        */

        SocialMediaInator socialMedia = new EvilSocialInator();
        socialMedia.register("valckata");
        socialMedia.register("valckata326");
        String first = socialMedia.publishPost("valckata",
                LocalDateTime.now().minusDays(6),
                "My first post @valckata326 #tag1");
        String second = socialMedia.publishPost("valckata",
                LocalDateTime.now().minusDays(6).plusHours(2),
                "My first post #tag2 #tag3 #tag1");
        String third = socialMedia.publishPost("valckata",
                LocalDateTime.now().plusDays(10),
                "My first post @mention2 #tag3 #tag1");
        String forth = socialMedia.publishPost("valckata",
                LocalDateTime.now().plusDays(3),
                "My first post @mention3 #tag4");
        System.out.println(first);
        System.out.println(second);

        socialMedia.like("valckata", "valckata-3");
        socialMedia.comment("valckata", "comment1", "valckata-3");
        socialMedia.like("valckata", "valckata-3");
        socialMedia.comment("valckata", "comment2", "valckata-3");
        socialMedia.like("valckata", "valckata-2");
        socialMedia.comment("valckata", "comment3", "valckata-1");
        socialMedia.like("valckata", "valckata-2");
        socialMedia.comment("valckata", "comment2", "valckata-2");
        socialMedia.like("valckata", "valckata-0");
        socialMedia.comment("valckata", "comment1", "valckata-0");
        Collection<Content> letsTry = socialMedia.getNMostPopularContent(2);
        for (Content current : letsTry) {
            System.out.println(current.getId()
                    + " "
                    + (current.getNumberOfLikes() + current.getNumberOfComments()));
        }
        Collection<Content> letsTryAgain = socialMedia.getNMostRecentContent("valckata", 10);
        for (Content current : letsTryAgain) {
            System.out.println(current.getPublishedOn());
        }
        String popular = socialMedia.getMostPopularUser();
        System.out.println(popular);


        for (Content curr : socialMedia.findContentByTag("tag1")) {
            System.out.println(curr.getId());
        }
        List<String> activityLog = new ArrayList<>(socialMedia.getActivityLog("valckata"));
        DateTimeComparator c = new DateTimeComparator();
        activityLog.sort(c);
        for (String curr : activityLog) {
            System.out.println(curr);
        }
    }
}
