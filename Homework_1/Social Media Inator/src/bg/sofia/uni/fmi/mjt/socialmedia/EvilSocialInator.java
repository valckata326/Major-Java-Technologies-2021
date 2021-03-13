package bg.sofia.uni.fmi.mjt.socialmedia;

import bg.sofia.uni.fmi.mjt.socialmedia.content.Content;
import bg.sofia.uni.fmi.mjt.socialmedia.content.ContentType;
import bg.sofia.uni.fmi.mjt.socialmedia.content.Post;
import bg.sofia.uni.fmi.mjt.socialmedia.content.Story;
import bg.sofia.uni.fmi.mjt.socialmedia.customcomparators.PopularityComparator;
import bg.sofia.uni.fmi.mjt.socialmedia.exceptions.ContentNotFoundException;
import bg.sofia.uni.fmi.mjt.socialmedia.exceptions.NoUsersException;
import bg.sofia.uni.fmi.mjt.socialmedia.exceptions.UsernameAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.socialmedia.exceptions.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class EvilSocialInator implements SocialMediaInator {
    private Set<String> usernames;
    private Map<String, TreeSet<Content>> userNewsFeed;
    private Map<String, Content> allContents;
    private Map<String, Integer> allMentions;
    private Map<String, HashSet<Content>> allTags;
    private Map<String, List<String>> activityLog;

    public EvilSocialInator() {
        userNewsFeed = new HashMap<>();
        usernames = new TreeSet<>();
        allContents = new HashMap<>();
        allMentions = new TreeMap<>(Comparator.reverseOrder());
        allTags = new HashMap<>();
        activityLog = new HashMap<>();
    }

    @Override
    public void register(String username) {
        verifyNullUsername(username);
        verifyNotContainsUser(username);
        usernames.add(username);
        activityLog.put(username, new ArrayList<>());
    }

    @Override
    public String publishPost(String username, LocalDateTime publishedOn, String description) {

        verifyNullUsername(username);
        verifyNullPushedOn(publishedOn);
        verifyNullDescription(description);
        verifyContainsUser(username);
        Content current = new Post(username, publishedOn, description);
        updateMentions(current);
        updateTags(current);
        contentsActivityLogUpdate(username, publishedOn, current);
        properAddContent(username, current);
        return current.getId();
    }

    @Override
    public String publishStory(String username, LocalDateTime publishedOn, String description) {
        verifyNullUsername(username);
        verifyNullPushedOn(publishedOn);
        verifyNullDescription(description);
        verifyContainsUser(username);
        Content current = new Story(username, publishedOn, description);
        updateMentions(current);
        updateTags(current);
        contentsActivityLogUpdate(username, publishedOn, current);
        properAddContent(username, current);
        return current.getId();
    }

    @Override
    public void like(String username, String id) {
        verifyNullUsername(username);
        verifyNullId(id);
        verifyContainsUser(username);
        verifyContainsContent(id);
        allContents.get(id).incrementNumberOfLikes();
        likeActivityLogUpdate(username, LocalDateTime.now(), id);
    }

    @Override
    public void comment(String username, String text, String id) {
        verifyNullUsername(username);
        verifyNullId(id);
        verifyNullText(text);
        verifyContainsUser(username);
        verifyContainsContent(id);
        allContents.get(id).incrementNumberOfComments();
        commentActivityLogUpdate(username, LocalDateTime.now(), id, text);
    }

    @Override
    public Collection<Content> getNMostPopularContent(int n) {
        verifyPositiveArgument(n);
        Comparator<Content> popularityComp = new PopularityComparator();
        Set<Content> copy = new TreeSet<>(popularityComp);
        if (allContents.isEmpty()) {
            return copy;
        }
        copy.addAll(allContents.values());
        Set<Content> toReturn = new TreeSet<>(popularityComp);
        int counter = 0;
        for (Content curr : copy) {
            if (!curr.isExpired()) {
                toReturn.add(curr);
                counter++;
            }
            if (counter == n) {
                break;
            }
        }
        return toReturn;
    }

    @Override
    public Collection<Content> getNMostRecentContent(String username, int n) {
        verifyNullUsername(username);
        verifyPositiveArgument(n);
        verifyContainsUser(username);
        if (!userNewsFeed.containsKey(username)) {
            return new TreeSet<>();
        }
        Set<Content> userContent = new TreeSet<>(userNewsFeed.get(username));
        Set<Content> toReturn = new TreeSet<>();
        int counter = 0;
        for (Content curr : userContent) {
            if (!curr.isExpired()) {
                toReturn.add(curr);
                counter++;
            }
            if (counter == n) {
                break;
            }
        }
        return toReturn;
    }

    @Override
    public String getMostPopularUser() {
        verifyUsernamesAvailable();
        Object maxEntry = Collections.max(allMentions.entrySet(),
                Map.Entry.comparingByValue()).getKey();
        return maxEntry.toString().substring(1);
    }

    @Override
    public Collection<Content> findContentByTag(String tag) {
        verifyNullTag(tag);
        if (allTags.get(tag) == null) {
            return Collections.emptyList();
        }
        Set<Content> tagsToReturn = new HashSet<>();
        for (Content curr : allTags.get(tag)) {
            if (!curr.isExpired()) {
                tagsToReturn.add(curr);
            }
        }
        return tagsToReturn;
    }

    @Override
    public List<String> getActivityLog(String username) {
        verifyNullUsername(username);
        verifyContainsUser(username);
        if (!activityLog.containsKey(username)) {
            return Collections.emptyList();
        }
        return activityLog.get(username);
    }

    private void verifyNullUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username is null");
        }
    }

    private void verifyNullPushedOn(LocalDateTime publishedOn) {
        if (publishedOn == null) {
            throw new IllegalArgumentException("Date is null");
        }
    }

    private void verifyNullDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description is null");
        }
    }

    private void verifyNullId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID is null");
        }
    }

    private void verifyContainsUser(String username) {
        if (!usernames.contains(username)) {
            throw new UsernameNotFoundException("Username not found");
        }
    }

    private void verifyNullText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text is null");
        }
    }

    private void properAddContent(String username, Content current) {
        if (userNewsFeed.containsKey(username)) {
            userNewsFeed.get(username).add(current);
            allContents.put(current.getId(), current);
        } else {
            userNewsFeed.put(username, new TreeSet<>());
            userNewsFeed.get(username).add(current);
            allContents.put(current.getId(), current);
        }
    }

    private void verifyContainsContent(String id) {
        if (!allContents.containsKey(id)) {
            throw new ContentNotFoundException("Like: No such content found on the platform");
        }
    }

    private void verifyPositiveArgument(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("N should be positive number");
        }
    }

    private void verifyUsernamesAvailable() {
        if (usernames.isEmpty()) {
            throw new NoUsersException("No users in the social media");
        }
    }

    private void verifyNullTag(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag is null");
        }
    }

    private String dateTimeFormatter(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yy");
        return formatter.format(dateTime);
    }

    private void updateMentions(Content current) {
        Set<String> mentions = new HashSet<>(current.getMentions());
        for (String curr : mentions) {
            if (usernames.contains(curr.substring(1))) {
                if (allMentions.containsKey(curr)) {
                    allMentions.replace(curr, allMentions.get(curr), allMentions.get(curr) + 1);
                } else {
                    allMentions.put(curr, 1);
                }
            }
        }
    }

    private void updateTags(Content current) {
        Set<String> tags = new HashSet<>(current.getTags());
        for (String curr : tags) {
            if (allTags.containsKey(curr)) {
                allTags.get(curr).add(current);
            } else {
                allTags.put(curr, new HashSet<>());
                allTags.get(curr).add(current);
            }
        }
    }

    private void contentsActivityLogUpdate(String username, LocalDateTime publishedOn, Content current) {
        String dateTime = dateTimeFormatter(publishedOn);
        String typeOfContent = "";
        if (current.getContentType() == ContentType.STORY) {
            typeOfContent = "story";
        } else if (current.getContentType() == ContentType.POST) {
            typeOfContent = "post";
        }
        String currentActivity = dateTime + ": Created a " + typeOfContent + " with id " + current.getId();
        if (activityLog.containsKey(username)) {
            activityLog.get(username).add(currentActivity);
        } else {
            activityLog.put(username, new ArrayList<>());
            activityLog.get(username).add(currentActivity);
        }
    }

    private void likeActivityLogUpdate(String username, LocalDateTime publishedOn, String id) {
        String dateTime = dateTimeFormatter(publishedOn);
        String currentActivity = dateTime + ": Liked a content with id " + id;
        if (activityLog.containsKey(username)) {
            activityLog.get(username).add(currentActivity);
        } else {
            activityLog.put(username, new ArrayList<>());
            activityLog.get(username).add(currentActivity);
        }
    }

    private void commentActivityLogUpdate(String username, LocalDateTime publishedOn, String id, String description) {
        String dateTime = dateTimeFormatter(publishedOn);
        String currentActivity = dateTime + ": Commented \"" + description + "\" on a content with id " + id;
        if (activityLog.containsKey(username)) {
            activityLog.get(username).add(currentActivity);
        } else {
            activityLog.put(username, new ArrayList<>());
            activityLog.get(username).add(currentActivity);
        }
    }

    private void verifyNotContainsUser(String username) {
        if (usernames.contains(username)) {
            throw new UsernameAlreadyExistsException("Register: Username already exists");
        }
    }
}
