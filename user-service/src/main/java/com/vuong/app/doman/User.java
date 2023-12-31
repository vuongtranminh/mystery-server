package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = { "refreshTokens", "verificationCredential" })
@ToString(exclude = { "refreshTokens", "verificationCredential" })
@Data
@Builder
@DynamicInsert
@DynamicUpdate
public final class User extends AbstractMappedEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @PrePersist
    public void prePersist() {
        this.bio = "";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false, updatable = false)
    private Integer userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "avatar", nullable = false)
    private String avatar;

    @Column(name = "bio", nullable = false)
    private String bio;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "verified", nullable = false)
    private Boolean verified;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY)
    private Set<RefreshToken> refreshTokens;

    // https://www.baeldung.com/jpa-one-to-one

    // use mappedBy để khi xoá VerificationCredential không xoá user
    // khi xoá user sẽ xoá cả VerificationCredential

    // https://vladmihalcea.com/the-best-way-to-map-a-onetoone-relationship-with-jpa-and-hibernate/
    @JsonIgnore
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private VerificationCredential verificationCredential;

    public void addRefreshToken(RefreshToken refreshToken) {
        refreshTokens.add(refreshToken);
        refreshToken.setUser(this);
    }

    public void removeRefreshToken(RefreshToken refreshToken) {
        refreshTokens.remove(refreshToken);
        refreshToken.setUser(null);
    }
}
