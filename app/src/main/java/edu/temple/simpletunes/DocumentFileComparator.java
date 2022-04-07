package edu.temple.simpletunes;

import androidx.documentfile.provider.DocumentFile;

import java.util.Comparator;

public class DocumentFileComparator implements Comparator<DocumentFile> {
    public DocumentFileComparator(){
    }
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
