package com.breezedevs.shopmobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataClass {

    public static class DataStore {
        public int id;
        public String name;
    }

    public static List<DataStore> mStorages = new ArrayList();
    public static int indexOfStoreId(int id) {
        for (int i = 0; i < mStorages.size(); i++) {
            if (mStorages.get(i).id == id) {
                return i;
            }
        }
        return -1;
    }
    public static int idOfIndex(int index) {
        if (index < 0 || index > mStorages.size()) {
            return 0;
        }
        return mStorages.get(index).id;
    }
}
