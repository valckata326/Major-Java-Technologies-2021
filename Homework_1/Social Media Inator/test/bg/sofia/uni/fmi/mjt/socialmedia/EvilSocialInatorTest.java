package bg.sofia.uni.fmi.mjt.socialmedia;

import bg.sofia.uni.fmi.mjt.socialmedia.content.Content;
import bg.sofia.uni.fmi.mjt.socialmedia.exceptions.ContentNotFoundException;
import bg.sofia.uni.fmi.mjt.socialmedia.exceptions.NoUsersException;
import bg.sofia.uni.fmi.mjt.socialmedia.exceptions.UsernameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.socialmedia.exceptions.UsernameNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class EvilSocialInatorTest {

    private EvilSocialInator testSocialInator;

    @Before
    public void setUp() {
        testSocialInator = new EvilSocialInator();
        IdIterator.id = 0;
    }

    @Test
    public void TestIDGeneration() {
        String username = "u1";
        testSocialInator.register(username);

        LocalDateTime now = LocalDateTime.now();
        testSocialInator.publishPost(username, now, "");
        testSocialInator.publishStory(username, now.plusMinutes(1), "");
        testSocialInator.publishPost(username, now.plusMinutes(2), "");


        Collection<Content> postedContent = testSocialInator.getNMostRecentContent(username, 3);
        assertEquals(3, postedContent.size());

        List<String> expectedIds = List.of("u1-2", "u1-1", "u1-0");
        List<String> actualIDs = new ArrayList<>();
        for (Content content : postedContent) {
            actualIDs.add(content.getId());
        }
        assertEquals(expectedIds, actualIDs);
    }


    @Test(expected = IllegalArgumentException.class)
    public void TestRegisterUserWithNullUser() {
        testSocialInator.register(null);
    }

    @Test(expected = UsernameAlreadyExistsException.class)
    public void TestRegisterUserWhenUserAlreadyExists() {
        String username = "u1";
        testSocialInator.register(username);
        testSocialInator.register(username);
    }

    @Test
    public void TestRegiserUserOK() {
        String username = "u1";
        testSocialInator.register(username);
        List<String> activityLog = testSocialInator.getActivityLog(username);
        assertEquals(activityLog.size(), 0);
    }


    @Test(expected = UsernameNotFoundException.class)
    public void TestPublishPostWhenUsernameNotFound() {
        testSocialInator.publishPost("non-existing", LocalDateTime.now(), "");
    }

    @Test()
    public void TestPublishPostOK() {
        LocalDateTime now = LocalDateTime.now();

        //Assert ID
        String username = "u1";
        registerUser(username);
        String postID = testSocialInator.publishPost(username, now, "desc1 #tag1 @non-existing @u1 #tag2");
        String expectedID = String.format("%s-0", username);
        assertEquals(expectedID, postID);

        //Assert Logs
        List<String> userLog = testSocialInator.getActivityLog(username);
        assertEquals(1, userLog.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yy");
        String expectedDateString = formatter.format(now);
        String expectedLogMessage = String.format("%s: Created a post with id %s", expectedDateString, postID);
        assertEquals(expectedLogMessage, userLog.get(0));

        //Assert tags
        Collection<Content> content = testSocialInator.getNMostRecentContent(username, 1);
        Content firstElement = content.iterator().next();
        assertEquals(Set.of("#tag1", "#tag2"), firstElement.getTags());

        //Assert mentions
        assertEquals(Set.of("@non-existing", "@u1"), firstElement.getMentions());

    }

    @Test(expected = UsernameNotFoundException.class)
    public void TestPublishStoryWhenUsernameNotFound() {
        testSocialInator.publishStory("non-existing", LocalDateTime.now(), "");
    }

    @Test
    public void TestPublishStoryOK() {
        LocalDateTime now = LocalDateTime.now();

        //Assert ID
        String username = "u1";
        registerUser(username);
        String postID = testSocialInator.publishStory(username, now, "desc1 #tag1 @non-existing @u1 #tag2");
        String expectedID = String.format("%s-0", username);
        assertEquals(expectedID, postID);

        //Assert Logs
        List<String> userLog = testSocialInator.getActivityLog(username);
        assertEquals(1, userLog.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yy");
        String expectedDateString = formatter.format(now);
        String expectedLogMessage = String.format("%s: Created a story with id %s", expectedDateString, postID);
        assertEquals(expectedLogMessage, userLog.get(0));

        //Assert tags
        Collection<Content> content = testSocialInator.getNMostRecentContent(username, 1);
        Content firstElement = content.iterator().next();
        assertEquals(Set.of("#tag1", "#tag2"), firstElement.getTags());

        //Assert mentions
        assertFalse(firstElement.getMentions().contains(Set.of("@u1", "@non-existing")));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void TestLikeWhenUsernameNotFound() {
        testSocialInator.like("non-existing", "");
    }

    @Test(expected = ContentNotFoundException.class)
    public void TestLikeWhenContentNotFound() {
        String username = "u1";
        testSocialInator.register(username);
        testSocialInator.like(username, "non-existing");
    }

    @Test()
    public void testLikeOK() {
        String username = "u1";
        testSocialInator.register(username);

        testSocialInator.publishPost(username, LocalDateTime.now(), "");
        String generatedId = "u1-0";

        testSocialInator.like(username, generatedId);
        testSocialInator.like(username, generatedId);
        testSocialInator.like(username, generatedId);


        Collection<Content> content = testSocialInator.getNMostRecentContent(username, 1);
        Content first = content.iterator().next();
        assertEquals(3, first.getNumberOfLikes());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void TestCommentWhenUsernameNotFound() {
        testSocialInator.comment("non-existing", "", "");
    }

    @Test(expected = ContentNotFoundException.class)
    public void TestCommentWhenContentNotFound() {
        String username = "u1";
        testSocialInator.register(username);
        testSocialInator.comment(username, "", "non-existing");
    }

    @Test
    public void TestCommentOK() {
        String username = "u1";
        testSocialInator.register(username);

        testSocialInator.publishPost(username, LocalDateTime.now(), "");
        String generatedId = "u1-0";

        testSocialInator.comment(username, "t1", generatedId);
        testSocialInator.comment(username, "t2", generatedId);
        testSocialInator.comment(username, "t3", generatedId);

        Collection<Content> content = testSocialInator.getNMostRecentContent(username, 1);
        Content first = content.iterator().next();
        assertEquals(3, first.getNumberOfComments());
    }


    @Test(expected = IllegalArgumentException.class)
    public void TestGetNMostPopularContentNegativeN() {
        testSocialInator.getNMostPopularContent(-1);
    }

    @Test
    public void TestGetNMostPopularContentOK() {
        LocalDateTime now = LocalDateTime.now();

        String username = "u1";
        registerUser(username);
        String infamousStoryID = testSocialInator.publishStory(username, now, "desc3");
        String famousPostID = testSocialInator.publishStory(username, now, "desc1");
        String famousStoryID = testSocialInator.publishStory(username, now, "desc2");

        testSocialInator.like(username, famousPostID);
        testSocialInator.like(username, famousStoryID);
        testSocialInator.comment(username, "text", famousPostID);

        Collection<Content> gotContent = testSocialInator.getNMostPopularContent(2);
        Collection<String> gotIDs = new ArrayList<>();
        for (Content content : gotContent) {
            gotIDs.add(content.getId());
        }
        Collection<String> expectedIDs = List.of(famousPostID, famousStoryID);
        assertEquals(expectedIDs, gotIDs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void TestGetNMostRecentContentNegativeN() {
        registerUser("u1");
        testSocialInator.getNMostRecentContent("u1", -3);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void TestGetNMostRecentContentWhenUserNotFound() {
        testSocialInator.getNMostRecentContent("non-existing", 1);
    }

    @Test
    public void TestGetNMostRecentContentOK() {
        LocalDateTime now = LocalDateTime.now();

        String username = "u1";
        registerUser(username);
        testSocialInator.publishPost(username, now.minusDays(5), "desc3");
        String content2ID = testSocialInator.publishStory(username, now, "desc1");
        String content3ID = testSocialInator.publishPost(username, now.minusDays(3), "desc2");


        Collection<Content> gotContent = testSocialInator.getNMostRecentContent(username, 2);
        Collection<String> gotIDs = new ArrayList<>();
        for (Content content : gotContent) {
            gotIDs.add(content.getId());
        }
        Collection<String> expectedIDs = List.of(content2ID, content3ID);
        assertEquals(expectedIDs, gotIDs);
    }

    @Test(expected = NoUsersException.class)
    public void TestGetMostPopularUserWhenNoUsers() {
        testSocialInator.getMostPopularUser();
    }

    @Test
    public void TestGetMostPopularUserOK() {
        String popularUser = "u1";
        String loser = "u2";
        testSocialInator.register(loser);
        testSocialInator.register(popularUser);
        testSocialInator.publishPost(loser, LocalDateTime.now(), "@u1 @u1 @u1 @u2");

        String gotUser = testSocialInator.getMostPopularUser();
        assertTrue(popularUser.equals(gotUser) || loser.equals(gotUser));
    }

    @Test(expected = IllegalArgumentException.class)
    public void TestFindContentByTagNullTag() {
        testSocialInator.findContentByTag(null);
    }

    @Test
    public void TestFindContentByTagOK() {
        String user = "u1";
        testSocialInator.register(user);

        String id1 = testSocialInator.publishPost(user, LocalDateTime.now(), "#tag asd");
        testSocialInator.publishPost(user, LocalDateTime.now(), "asdasd tag");
        String id2 = testSocialInator.publishStory(user, LocalDateTime.now(), "asd #tag");
        testSocialInator.publishPost(user, LocalDateTime.now(), "@tag asd");

        Collection<Content> gotContent = testSocialInator.findContentByTag("#tag");
        List<String> gotIDs = new ArrayList<>();
        for (Content entry : gotContent) {
            gotIDs.add(entry.getId());
        }
        assertTrue(gotIDs.contains(id1));
        assertTrue(gotIDs.contains(id2));
    }


    //TODO Test expirations
    private void registerUser(String username) {
        testSocialInator.register(username);
    }
}
