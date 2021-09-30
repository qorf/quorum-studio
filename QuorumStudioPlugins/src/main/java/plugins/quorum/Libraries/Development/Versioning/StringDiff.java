/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Versioning;

import com.github.difflib.algorithm.Change;
import com.github.difflib.algorithm.DiffAlgorithmListener;
import java.util.ArrayList;
import java.util.List;
import com.github.difflib.patch.*;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;

/**
 *
 * @author stefika
 */
public class StringDiff {
    public static List<Change> Diff(String a, String b, DiffAlgorithmListener progress) {
        if (progress != null) {
            progress.diffStart();
        }
        
        String[] source = ConvertToList(a);
        String[] target = ConvertToList(b);
        EditList diffList = new EditList();
        diffList.addAll(new org.eclipse.jgit.diff.HistogramDiff().diff(new DataListComparator<>(progress), new DataList<>(source), new DataList<>(target)));
        List<Change> patch = new ArrayList<>();
        for (Edit edit : diffList) {
            DeltaType type = DeltaType.EQUAL;
            switch (edit.getType()) {
                case DELETE:
                    type = DeltaType.DELETE;
                    break;
                case INSERT:
                    type = DeltaType.INSERT;
                    break;
                case REPLACE:
                    type = DeltaType.CHANGE;
                    break;
            }
            patch.add(new Change(type, edit.getBeginA(), edit.getEndA(), edit.getBeginB(), edit.getEndB()));
        }
        if (progress != null) {
            progress.diffEnd();
        }
        return patch;
    }
    
    public static String[] ConvertToList(String value) {
        String lines[] = value.split("\\r?\\n");
        return lines;
    }
}

class DataListComparator<T> extends SequenceComparator<DataList<T>> {

    private final DiffAlgorithmListener progress;

    public DataListComparator(DiffAlgorithmListener progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(DataList<T> original, int orgIdx, DataList<T> revised, int revIdx) {
        if (progress != null) {
            progress.diffStep(orgIdx + revIdx, original.size() + revised.size());
        }
        return original.data[orgIdx].equals(revised.data[revIdx]);
    }

    @Override
    public int hash(DataList<T> s, int i) {
        return s.data[i].hashCode();
    }

}

class DataList<T> extends Sequence {

    final String[] data;

    public DataList(String[] data) {
        this.data = data;
    }

    @Override
    public int size() {
        return data.length;
    }
}