package com.vuong.app.security.oauth2.user;

import java.util.HashMap;
import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getImageUrl();

    // ADDED
    public void setEmail(String email) {
        attributes = new HashMap<String, Object>(attributes);
        attributes.put("email", email);
    }
}
