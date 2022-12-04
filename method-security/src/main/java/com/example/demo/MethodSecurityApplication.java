package com.example.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.transaction.Transactional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        jsr250Enabled = true,
        securedEnabled = true
)
@SpringBootApplication
public class MethodSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(MethodSecurityApplication.class, args);
    }

}

@Transactional
@Component
@Log4j2
class Runner implements ApplicationRunner {

//    final Logger log = LogManager.getLogger();

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final MessageRepository messageRepository;

    private final UserDetailsService userDetailsService;

    public Runner(UserRepository userRepository, AuthorityRepository authorityRepository, MessageRepository messageRepository, UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.messageRepository = messageRepository;
        this.userDetailsService = userDetailsService;
    }

    private void authenticate(String username) {
        UserDetails user = this.userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user,
                user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // install some data

        Authority admin = this.authorityRepository.save(new Authority("ADMIN"));
        Authority user = this.authorityRepository.save(new Authority("USER"));

        User bhaskar = this.userRepository.save(new User("bhaskar", "password", admin, user));
        Message messageForBhaskar = this.messageRepository.save(new Message("Hi Bhaskar!", bhaskar));
//		admin.getUsers().add(bhaskar);
        admin.adduser(bhaskar);

        User jlong = this.userRepository.save(new User("jlong", "password", user));
        Message messageFoJosh = this.messageRepository.save(new Message("Hey Josh!", jlong));
        admin.adduser(jlong);

        log.info("Bhaskar: ", bhaskar.toString());
        log.info("josh: ", jlong);

        log.info("bhaskar: " + bhaskar.email);

//        authenticate(bhaskar.getEmail());

//        log.info("result for bhaskar: " + messageRepository.findByIdRolesAllowed(messageForBhaskar.getId()));

//        try {
//            authenticate(jlong.getEmail());
//            log.info("result for josh: " + messageRepository.findByIdRolesAllowed(messageFoJosh.getId()));
//        } catch (Exception e) {
//            log.error("oops! could not obtain result for jlong");
//        }

    }

}


@Service
class UserRepositoryUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserRepositoryUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User usr = this.userRepository.findByEmail(username);
        if (usr != null) {
            return new UserUserDetails(usr);
        } else {
            throw new UsernameNotFoundException("couldn't find " + username + "!");
        }
    }

    public static class UserUserDetails implements UserDetails {

        private final User user;
        private final Set<GrantedAuthority> authorities;

        public UserUserDetails(User user) {
            this.user = user;
            this.authorities = this.user.getAuthorities()
                    .stream()
                    .map(au -> new SimpleGrantedAuthority("ROLE_"+au.getAuthority()))
                    .collect(Collectors.toSet());
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return this.authorities;
        }

        @Override
        public String getPassword() {
            return this.user.getPassword();
        }

        @Override
        public String getUsername() {
            return this.user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

    }

}

interface MessageRepository extends JpaRepository<Message, Long> {

    String QUERY = "select m from Message m where m.id = ?1";

    @Query(QUERY)
    @RolesAllowed("ROLE_ADMIN")
    Message findByIdRolesAllowed(Long id);
}

interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}

interface AuthorityRepository extends JpaRepository<Authority, Long> {

}

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
class Message {
    @Id
    @GeneratedValue //(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @OneToOne
    private User toUser;

    public Message(String text, User to) {
        this.text = text;
        this.toUser = to;
    }


}

@Entity(name = "`user`") //user is restricted keyword in h2
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "authorities") // so that toString doesn't recursively try to print
@ToString
//@Getter
@Data
class User {
    @Id
    @GeneratedValue //(strategy = GenerationType.IDENTITY)
    private Long id;

    String email;
    private String password;

    @ManyToMany(mappedBy = "users",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // user has many authorities, user owns authorities
    private List<Authority> authorities = new ArrayList<>();

    public User(String email, String password, Set<Authority> authorities) {
        this.email = email;
        this.password = password;
        this.authorities.addAll(authorities);
    }


    public String getEmail() {
        return this.email;
    }


    public String getPassword() {
        return this.password;
    }


    public Collection<Authority> getAuthorities() {
        return this.authorities;
    }


    public User(String email, String password) {
        this(email, password, new HashSet<>());
    }

    public User(String email, String password, Authority... authorities) {
        this(email, password, new HashSet<>(Arrays.asList(authorities)));

    }

}

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "users")
@Data
class Authority {
    @Id
    @GeneratedValue //(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    private String authority;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "authority_user", joinColumns = @JoinColumn(name = "authority_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users = new ArrayList<>();

    public Authority(String authority, Set<User> users) {
        this.authority = authority;
        this.users.addAll(users);
    }

    public String getAuthority() {
        return this.authority;
    }

    public Authority(String authority) {
        this(authority, new HashSet<>());
    }

    public void adduser(User user) {
        if (!users.contains(user)) {  //users as set - better
            users.add(user);
        }
    }

}