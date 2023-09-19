package com.vuong.app.business.auth.model;

public interface UserDto {

    Integer getUserId();

    void setUserId(Integer userId);

    String getName();

    void setName(String name);

    String getAvatar();

   void setAvatar(String avatar);

    String getBio();

    void setBio(String bio);

    String getEmail();

    void setEmail(String email);

    String getPassword();

    void setPassword(String password);

    AuthProvider getProvider();

    void setProvider(AuthProvider provider);

    String getProviderId();

    void setProviderId(String providerId);
}
