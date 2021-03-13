package bg.sofia.uni.fmi.mjt.socialmedia.customcomparators;

import bg.sofia.uni.fmi.mjt.socialmedia.content.Content;

import java.util.Comparator;

public class PopularityComparator implements Comparator<Content> {
    @Override
    public int compare(Content o1, Content o2) {
        if ((o1.getNumberOfLikes() + o1.getNumberOfComments())
                < o2.getNumberOfLikes() + o2.getNumberOfLikes()) {
            return 1;
        } else if ((o1.getNumberOfLikes() + o1.getNumberOfComments())
                < (o2.getNumberOfLikes() + o2.getNumberOfComments())) {
            return 0;
        }
        return -1;
    }
}
