package com.example.CentralLAApp.entity.user
import com.example.CentralLAApp.entity.Notification
import com.example.CentralLAApp.entity.NotificationPreference
import com.example.CentralLAApp.entity.auth.Token
import com.example.CentralLAApp.enums.UserRole
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.PreRemove
import jakarta.persistence.Table
import lombok.NonNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "users")
open class User protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var userID: Int = 0,

    @Column(unique = true)
    open var email: String = "",

    open var passw: String = "",

    @Column(name="name")
    open var _name: String = "",

    open var surname: String = "",

    open var graduationType: String? = null,

    @Enumerated(EnumType.STRING)
    open var role: UserRole = UserRole.USER,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    open var tokens: Set<Token> = emptySet(),

    @OneToOne( cascade = [CascadeType.ALL], fetch = FetchType.EAGER, optional = false)
    open var notificationPreferences: NotificationPreference = NotificationPreference(),

    open var universityId: String = "",
    open var photoUrl: String? = null

) : UserDetails, Principal{

    override  fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(role.value))
    }


    override fun getPassword(): String = passw

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    override fun getName(): String {
        return this.userID.toString()
    }

    companion object {

        fun builder(): Builder<out User, out Builder<out User, *>> {
            return Builder(User())
        }
    }
    open class Builder<R : User, B : Builder<R, B>>(protected val user: R) {
        fun id(id: Int): B {
            user.userID = id
            return self()
        }

        fun email(email: String): B {
            user.email = email
            return self()
        }

        fun password(password: String): B {
            user.passw = password
            return self()
        }

        fun name(name: String): B {
            user._name = name
            return self()
        }

        fun surname(surname: String): B {
            user.surname = surname
            return self()
        }

        fun graduationType(graduationType: String?): B {
            user.graduationType = graduationType
            return self()
        }

        fun universityId(universityId: String): B {
            user.universityId = universityId
            return self()
        }
        fun photoUrl(photoUrl: String?): B {
            user.photoUrl = photoUrl
            return self()
        }

        open fun role(role: UserRole): B {
            user.role = role
            return self()
        }

        open fun build(): R {
            return user
        }

        protected open fun self(): B {
            return this as B
        }


    }

    // Constructor to initialize a User object from the Builder
    protected constructor(theBuilder: Builder<out User, *>) : this() {
        // Initialize User object from the builder properties
        theBuilder.build().copyTo(this)
    }

    private fun copyTo(target: User) {
        target.userID = userID
        target.email = email
        target.passw = passw
        target._name = _name
        target.surname = surname
        target.graduationType = graduationType
        target.role = role
        target.tokens = tokens
        target.universityId = universityId
        target.photoUrl = photoUrl
    }

}






