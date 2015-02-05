package com.appdynamics.extensions.cassandra;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexHashMap<V> extends HashMap<String,V>{

    private static final boolean DEFAULT_ESCAPE_KEY = true;
    private ArrayList<Pattern> patterns = new ArrayList<Pattern>();
    boolean escapeKey = DEFAULT_ESCAPE_KEY;

    public RegexHashMap() {
        this(DEFAULT_ESCAPE_KEY);
    }

    public RegexHashMap(boolean escapeKeyString) {
        this.escapeKey = escapeKeyString;
    }

    @Override
    public V put(String key, V value) {
        if(key == null){
            throw new IllegalArgumentException("Key cannot be null");
        }
        String escapedKey = escape(key);
        patterns.add(Pattern.compile(escapedKey));
        return super.put(escapedKey, value);
    }

    @Override
    public V get(Object key) {
        Pattern matchedPattern = null;
        if(key == null){
            throw new IllegalArgumentException("Key cannot be null");
        }
        for(Pattern pattern : patterns){
            if(pattern.matcher(key.toString()).matches()){
                matchedPattern = pattern;
                break;
            }
        }
        if(matchedPattern != null){
            return super.get(matchedPattern.pattern());
        }
        return null;
    }

    private String escape(java.lang.String pattern) {
        if(escapeKey) {
            return pattern.replaceAll("\\|", "\\\\|");
        }
        return pattern;
    }
}
