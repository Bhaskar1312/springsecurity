package com.example.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

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
	
	final Logger log = LogManager.getLogger();
	
	private final UserRepository userRepository;
	private final AuthorityRepository authorityRepository;
	private final MessageRepository messageRepository;
	
	public Runner(UserRepository userRepository, AuthorityRepository authorityRepository, MessageRepository messageRepository) {
		this.userRepository = userRepository;
		this.authorityRepository = authorityRepository;
		this.messageRepository = messageRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// install some data
		
		Authority admin = this.authorityRepository.save(new Authority("ADMIN"));
		Authority user = this.authorityRepository.save(new Authority("USER"));
		
		User bhaskar =  this.userRepository.save(new User("bhaskar", "password", admin, user));
		Message messageForBhaskar = this.messageRepository.save(new Message("Hi Bhaskar!", bhaskar));
//		admin.getUsers().add(bhaskar);
		admin.adduser(bhaskar);
		
		User jlong =  this.userRepository.save(new User("jlong", "password", admin, user));
		Message messageFoJosh = this.messageRepository.save(new Message("Hey Josh!",  jlong));
		admin.adduser(jlong);
		
		log.info("Bhaskar: ", bhaskar.toString());
		log.info("josh: ", jlong);
		
		log.info("bhaskar: "+bhaskar.email);
	}
	
}

interface MessageRepository extends JpaRepository<Message, Long> {
	
}

interface UserRepository extends JpaRepository<User, Long> {
	
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
@EqualsAndHashCode(exclude="authorities") // so that toString doesn't recursively try to print
@ToString
@Getter
@Data
class User {
	@Id
	@GeneratedValue //(strategy = GenerationType.IDENTITY)
	private Long id;
	
	String email;
	private String password;
	
	@ManyToMany(mappedBy = "users", 
			cascade = { CascadeType.PERSIST, CascadeType.MERGE}) // user has many authorities, user owns authorities
	@Getter
	private List<Authority> authorities = new ArrayList<>();

	public User(String email, String password, Set<Authority> authorities) {
		this.email = email;
		this.password = password;
		this.authorities.addAll(authorities);
	}
	

	public User(String email, String password) {
		this(email, password, new HashSet<>());
	}
	
	public User(String email, String password, Authority...authorities) {
		this(email, password, new HashSet<>(Arrays.asList(authorities)));
		
	}
	
}

@Entity
@AllArgsConstructor
//@NoArgsConstructor
@ToString(exclude = "users")
@Data
@Getter
class Authority {
	@Id
	@GeneratedValue //(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String authority;
	
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
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
		if(!users.contains(user)) {  //users as set - better
			users.add(user);
		}
	}
	
}