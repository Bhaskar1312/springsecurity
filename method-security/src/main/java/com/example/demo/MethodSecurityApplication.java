package com.example.demo;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.persistence.*;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.security.Principal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@EnableJpaAuditing //look for bean AuditorAware
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        jsr250Enabled = true,
        securedEnabled = true
)
@SpringBootApplication
public class MethodSecurityApplication {

    @Bean
    AuditorAware<String> auditor() {
        return new AuditorAware<String>() {
            @Override
            public Optional<String> getCurrentAuditor() {
                SecurityContext context = SecurityContextHolder.getContext();
                Authentication authentication = context.getAuthentication();
                if(authentication != null) {
                    return Optional.ofNullable(authentication.getName());
                }
                return Optional.empty();
            }
        };
    }
    @Bean
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript(JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION)
                .build();
    }

    @Bean
    SecurityEvaluationContextExtension securityEvaluationContextEvaluation() {
        return new SecurityEvaluationContextExtension();
    }

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
        log.info(">>>"+user.getUsername()+" " + user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // install some data

        Authority admin = this.authorityRepository.save(new Authority("ADMIN"));
        Authority user = this.authorityRepository.save(new Authority("USER"));

        User bhaskar = this.userRepository.save(new User("bhaskar", "password", admin, user));
        Message messageForBhaskar = this.messageRepository.save(new Message("Hi Bhaskar!", bhaskar));
        Message msg1 = this.messageRepository.save(new Message("Hi 1", bhaskar));
        Message msg2 = this.messageRepository.save(new Message("Hi 2", bhaskar));
//		admin.getUsers().add(bhaskar);
//        admin.adduser(bhaskar);

        User jlong = this.userRepository.save(new User("jlong", "password", user));
        Message messageFoJosh = this.messageRepository.save(new Message("Hey Josh!", jlong));
//        admin.adduser(jlong);
        this.messageRepository.save(new Message("Hi 3", jlong));

        log.info("Bhaskar: ", bhaskar.toString());
        log.info("josh: ", jlong);

        log.info("bhaskar: " + bhaskar.email);

        attemptAccess(bhaskar.getEmail(), jlong.getEmail(), messageForBhaskar.getId(), id->this.messageRepository.findByIdRolesAllowed(id));

        attemptAccess(bhaskar.getEmail(), jlong.getEmail(), messageForBhaskar.getId(), id->this.messageRepository.findByIdSecured(id));

        attemptAccess(bhaskar.getEmail(), jlong.getEmail(), messageForBhaskar.getId(), id->this.messageRepository.findByIdPreAuthorize(id));

        attemptAccess(bhaskar.getEmail(), jlong.getEmail(), messageFoJosh.getId(), id->this.messageRepository.findByIdPostAuthorize(id));


        System.out.println("Filtering data at query level");
        authenticate(bhaskar.getEmail());
        checkSPEL_Temp();
        this.messageRepository.findMessagesFor(PageRequest.of(0, 5)).forEach(log::info);

        authenticate(jlong.getEmail());
        this.messageRepository.findMessagesFor(PageRequest.of(0, 5)).forEach(log::info);


        log.info("audited: "+this.messageRepository.save(new Message("this is a test for audit", bhaskar))); //prints jlong

    }

    void checkSPEL_Temp() {
        System.out.println(">>"+ ((UserRepositoryUserDetailsService.UserUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser().getId());
//        ExpressionParser parser = new SpelExpressionParser();
//        Expression exp = parser.parseExpression("?#{ principal?.user?.id }");
//        String message = (String) exp.getValue();
//        System.out.println("spel"+message);
    }

    private void attemptAccess(String adminUser, String regularUser, Long msgId, Function<Long, Message> fn) {
        authenticate(adminUser);

        try {
            log.info("result for bhaskar: " + fn.apply(msgId));
        } catch (Exception e) {
            log.error(">>>Error for admin user<<<"+e.getMessage());
        }

        try {
            authenticate(regularUser);
            log.info("result for josh: " + fn.apply(msgId));
        } catch (Exception e) {
            log.error("oops! could not obtain result for jlong");
        }
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

        public User getUser() {
            return user;
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

    @Query(QUERY)
    @Secured("ROLE_ADMIN")
    Message findByIdSecured(Long id);

    @Query(QUERY)
    @PreAuthorize("hasRole('ADMIN')") // auth happens before going to this method
    Message findByIdPreAuthorize(Long id);

    @Query(QUERY)
    @PostAuthorize("@authz.check( returnObject, principal?.user )") // auth should happen after the method
    Message findByIdPostAuthorize(Long id); // or name it findByIdBeanCheck

    @Query("select m from Message m where m.toUser.id = ?#{ principal?.user?.id }") // ? - for if in case null
    Page<Message> findMessagesFor(Pageable pageable);  //security/filtering at query level instead of after result like PostAuthorize

}

@Log4j2
@Service("authz") //authz bean
class AuthService {
    public boolean check(Message message, User user) {
        log.info("checking "+user.getEmail()+"..");
        return message.getToUser().getId().equals(user.getId());
    }
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
@EntityListeners(AuditingEntityListener.class)
class Message {
    @Id
    @GeneratedValue //(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @OneToOne
    @Getter
    private User toUser;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public Message(String text, User to) {
        this.text = text;
        this.toUser = to;
    }


}

@Entity//(name = "`user`") //user is restricted keyword in h2
@Table(name = "`user`")
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

    public Long getId() {
        return id;
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

    public Authority(String authority) {
        this(authority, new HashSet<>());
    }

    public void adduser(User user) {
        if (!users.contains(user)) {  //users as set - better
            users.add(user);
        }
    }

}