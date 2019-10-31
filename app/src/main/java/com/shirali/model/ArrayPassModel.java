package com.shirali.model;

import java.util.ArrayList;

/**
 * Created by Pankaj on 3/8/17.
 */

public class ArrayPassModel {
        private static ArrayList<String> arraylist;

    public static void setArraylist(ArrayList<String> arraylist) {
        ArrayPassModel.arraylist = arraylist;
    }

    public static ArrayList<String> getArraylist() {
        return arraylist;
    }
}
