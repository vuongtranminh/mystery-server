package com.vuong.app.doman;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Entity
@Table(name = "verification_credentials")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "user" })
@ToString(exclude = { "user" })
@Data
@Builder
public class VerificationCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_credential_id", unique = true, nullable = false, updatable = false)
    private Integer verificationCredentialId;

    @Column(name = "verification_token", unique = true)
    private String verificationToken;

    @Column(name = "verification_otp", unique = true)
    private String verificationOtp;

    @JsonFormat(shape = STRING)
    @Column(name = "expire_date", nullable = false)
    private Instant expireDate;

    /**
     * Therefore, the @ManyToOne and the @OneToOne child-side association are best to represent a FOREIGN KEY relationship.
     * <p>
     * The parent-side @OneToOne association requires bytecode enhancement so that the association can be loaded lazily. Otherwise, the parent-side association is always fetched even if the association is marked with FetchType.LAZY.
     * <p>
     * For this reason, it’s best to map @OneToOne association using @MapsId so that the PRIMARY KEY is shared between the child and the parent entities. When using @MapsId, the parent-side association becomes redundant since the child-entity can be easily fetched using the parent entity identifier.
     **/

//    @JsonIgnore
//    @OneToOne(mappedBy = "credential", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @PrimaryKeyJoinColumn
//    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id") // name là tên cột khóa ngoại trong bảng VC, referencedColumnName là id của của bảng user
    private User user;

    //	Khi bạn thực hiện một câu lệnh insert vào bảng chứa một quan hệ One-to-One trong Hibernate, Hibernate có thể sinh ra một câu lệnh select để tải đối tượng liên quan trước, trước khi thực hiện insert, và đây là hành vi mặc định của Hibernate.
//
//	Lý do để Hibernate tải đối tượng liên quan khi thực hiện insert là để đảm bảo tính toàn vẹn của quan hệ One-to-One. Nếu quan hệ One-to-One được định nghĩa với optional=false, nghĩa là quan hệ đó là bắt buộc và không thể là null, khi bạn thực hiện insert vào bảng chứa quan hệ đó, bạn phải cung cấp một giá trị cho quan hệ đó. Nếu giá trị được cung cấp là một đối tượng mới, Hibernate cần phải thực hiện insert cho đối tượng đó trước khi thực hiện insert cho đối tượng chứa quan hệ đó. Tuy nhiên, để thực hiện insert cho đối tượng chứa quan hệ đó, Hibernate cần biết khóa chính của đối tượng liên quan, và do đó, nó cần phải tải đối tượng liên quan trước.
//
//	Ví dụ, giả sử bạn có hai đối tượng User và Address với quan hệ One-to-One bắt buộc giữa chúng:
//
//	@Entity
//	public class User {
//		@Id
//		@GeneratedValue
//		private Long id;
//
//		@OneToOne(optional = false, cascade = CascadeType.PERSIST)
//		private Address address;
//
//		// other fields and methods
//	}
//
//	@Entity
//	public class Address {
//		@Id
//		@GeneratedValue
//		private Long id;
//
//		// other fields and methods
//	}
//	Khi bạn thực hiện insert cho đối tượng User mới, Hibernate sẽ cần phải tải đối tượng Address liên quan trước, trước khi thực hiện insert cho đối tượng User. Nếu bạn không cung cấp đối tượng Address trước khi thực hiện insert cho đối tượng User, Hibernate có thể sẽ sinh ra một câu select để tải đối tượng Address từ cơ sở dữ liệu trước khi thực hiện insert cho đối tượng User.
//
//	Để tránh việc Hibernate tải đối tượng liên quan khi thực hiện insert, bạn có thể sử dụng cấu hình hibernate.use_direct_reference_optimization và đặt giá trị của nó là true. Khi cấu hình này được bật, Hibernate sẽ không tải đối tượng liên quan khi thực hiện insert, mà sẽ sử dụng trực tiếp đối tượng được cung cấp. Tuy nhiên, điều này chỉ nên được sử dụng khi bạn đã chắc chắn rằng đối tượng liên quan đã tồn tại trong cơ sở dữ liệu. Nếu không, việc thực hiện insert có thể sẽ gây ra lỗi do khóa ngoại không hợp lệ.
}
