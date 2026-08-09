package com.novamens.wicket.util;

public class TextCleaner {

    public static String clean(String s) {
        return clean(s,-1);
    }

    public static String truncate(String source, int maxSize) {
        
        if (source==null)
            return null;
        
        if (maxSize>0 && source.length()>maxSize)
            return source.substring(0, maxSize)+"...";
        else
            return source;
    }
    
    public static String clean(String source, int maxSize) {
    
            if (source==null)
                return "";
            
            StringBuilder str = new StringBuilder();
            
            String s = source;

            while (s.endsWith("\\\\n"))
                s=s.substring(0, str.length()-2);
            
            while (s.endsWith("\\n"))
                s=s.substring(0, str.length()-2);
            
            while (s.endsWith("<br>"))
                s=s.substring(0, str.length()-4);
            
            while (s.endsWith("<br/>"))
                s=s.substring(0, str.length()-5);    
            
            if (maxSize>0 && s.length()>maxSize)
                str.append(s.substring(0, maxSize)+"...");
            else
                str.append(s);
            
            String a=str.toString().       
            replaceAll("\\\\b{1,}", "<br/>");
                     
             String ret = "<p>"+a.
                    replaceAll("\\.\\.\n", ".\n").
                    replaceAll("\\\\n{2,}", "\n").
                    replaceAll("\\\\n{1,}", "</p><p>").
                    replaceAll("\\n{1,}", "</p><p>").
                    replaceAll("\\\\n{1,}", "</p><p>");
                    
                    ret.replaceAll("<p></p>", "");
             
             return ret+"</p>";
    }

    
    /**
     * 
      public static String clean(String s, int maxSize) {
        
        if (s==null)
            return null;
        
        String str;
        
        if (maxSize>0 && s.length()>maxSize)
            str=s.substring(0, maxSize)+"...";
        else
            str=s;
        
        while (str.endsWith("\\n"))
            str=str.substring(0, str.length()-1);
        
        while (str.endsWith("<br>"))
            str=str.substring(0, str.length()-4);
        
        while (str.endsWith("<br/>"))
            str=str.substring(0, str.length()-5);    
        
        str=str.replaceAll("\\\\n{1,}", "<br/>");
        str=str.replaceAll("\n{1,}", "<br/>");
        str=str.replaceAll("\\.\n", ".<br/>");
        str=str.replaceAll("\\.\\.\n", ".<br/>");
        
        str=str.replaceAll("\n", "<br/>");
        
        return str;
    }
    **/
    
}
