package traderalchemy.analyst.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    
    public static <T> List<List<T>> batch(List<T> list, int batchSize) {
        List<List<T>> batchList = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batchList.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batchList;
    }
}
