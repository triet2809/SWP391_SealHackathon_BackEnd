package Group1.Topic4.Service;

import Group1.Topic4.config.JwtTokenProvider;
import Group1.Topic4.entity.Users;
import Group1.Topic4.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    public String login(String email, String password){
        Users user = userRepository.findByEmail(email);
            if(user == null){
                return null;
            }
        if(passwordEncoder.matches(password, user.getPasswordHash())){
            return jwtTokenProvider.generateToken(user);
        }
        return null;

    }
    public boolean register(String email, String password, String fullName, String studentID, String universityName){
        if(userRepository.findByEmail(email) != null){
            return false; // Email đã tồn tại
        }
        Users newUser = new Users();
        newUser.setEmail(email);
        newUser.setStudentId(studentID);
        newUser.setUniversityName(universityName);
        String hashPass = passwordEncoder.encode(password);
        newUser.setPasswordHash(hashPass); // Lưu mật khẩu dưới dạng hash trong thực tế
        newUser.setFullName(fullName);
        newUser.setSystemRole("STUDENT");
        newUser.setIsApproved(false);
        newUser.setCreatedAt(Instant.now());
        userRepository.save(newUser);
        return true;
    }
    public Users findByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
