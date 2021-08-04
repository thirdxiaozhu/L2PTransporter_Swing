package zoujiaxv;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;

public class MyListModel extends AbstractListModel {
    ArrayList<String> dArray;

    public MyListModel(ArrayList<String> dArray){
        this.dArray = dArray;
    }
    @Override
    public int getSize() {
        return dArray.size();
    }

    @Override
    public Object getElementAt(int index) {
        return dArray.get(index);
    }

}
