package com.eskyray.im.server.pinin;


import java.util.Comparator;


/**
 *
 * @author
 *
 */
public class PinyinComparator{


    public static PinyinComparator instance = null;

    public static PinyinComparator getInstance() {
        if (instance == null) {
            instance = new PinyinComparator();
        }
        return instance;
    }


}
