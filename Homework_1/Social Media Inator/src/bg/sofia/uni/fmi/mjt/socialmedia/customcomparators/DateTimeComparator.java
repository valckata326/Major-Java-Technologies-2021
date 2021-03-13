package bg.sofia.uni.fmi.mjt.socialmedia.customcomparators;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class DateTimeComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        if (o1.equals(o2)) {
            return 0;
        }
        String[] attrib1 = o1.split(": ");
        String[] attrib2 = o2.split(": ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yy");
        LocalDateTime firstDateTime = LocalDateTime.parse(attrib1[0], formatter);
        LocalDateTime secondDateTime = LocalDateTime.parse(attrib2[0], formatter);
        if (firstDateTime.isAfter(secondDateTime)) {
            return -1;
        }
        return 1;

    }
}
