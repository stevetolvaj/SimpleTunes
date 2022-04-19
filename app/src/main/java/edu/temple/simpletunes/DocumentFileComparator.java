package edu.temple.simpletunes;

import androidx.documentfile.provider.DocumentFile;

import java.util.Comparator;

/**
 * The DocumentFileComparator class is used to compare the filenames to be sorted in
 * the correct order.
 */
public class DocumentFileComparator implements Comparator<DocumentFile> {
    /**
     * The compare method is used to check the order of the DocumentFile names in ascending order.
     * @param df1 The first DocumentFile to compare.
     * @param df2 The second DocumentFile to compare.
     * @return The result of the comparison.
     */
    @Override
    public int compare(DocumentFile df1, DocumentFile df2) {
        String s1 = df1.getName();
        String s2 = df2.getName();
        if(s1 != null && s2 != null){
            return s1.compareTo(s2);
        }else{
            return 0;
        }
    }
}
