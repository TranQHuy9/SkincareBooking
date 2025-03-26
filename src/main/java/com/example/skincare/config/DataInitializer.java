package com.example.skincare.config;

import com.example.skincare.models.Center;
import com.example.skincare.models.Role;
import com.example.skincare.models.User;
import com.example.skincare.repositories.CenterRepository;
import com.example.skincare.repositories.RoleRepository;
import com.example.skincare.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Tạo các role nếu chưa tồn tại
        Role customerRole = roleRepository.findByName("CUSTOMER");
        if (customerRole == null) {
            customerRole = new Role("CUSTOMER");
            roleRepository.save(customerRole);
        }

        Role therapistRole = roleRepository.findByName("THERAPIST");
        if (therapistRole == null) {
            therapistRole = new Role("THERAPIST");
            roleRepository.save(therapistRole);
        }

        Role staffRole = roleRepository.findByName("STAFF");
        if (staffRole == null) {
            staffRole = new Role("STAFF");
            roleRepository.save(staffRole);
        }

        Role managerRole = roleRepository.findByName("MANAGER");
        if (managerRole == null) {
            managerRole = new Role("MANAGER");
            roleRepository.save(managerRole);
        }

        // Tạo user mặc định nếu chưa tồn tại
        if (userRepository.findByEmail("customer1@example.com").isEmpty()) {
            User customer = new User();
            customer.setUsername("customer1");
            customer.setPassword(passwordEncoder.encode("password"));
            customer.setEmail("customer1@example.com");
            customer.setFullName("Customer One");
            Set<Role> customerRoles = new HashSet<>();
            customerRoles.add(customerRole);
            customer.setRoles(customerRoles);
            userRepository.save(customer);
        }

        if (userRepository.findByEmail("therapist1@example.com").isEmpty()) {
            User therapist = new User();
            therapist.setUsername("therapist1");
            therapist.setPassword(passwordEncoder.encode("password"));
            therapist.setEmail("therapist1@example.com");
            therapist.setFullName("Therapist One");
            Set<Role> therapistRoles = new HashSet<>();
            therapistRoles.add(therapistRole);
            therapist.setRoles(therapistRoles);
            userRepository.save(therapist);
        }

        if (userRepository.findByEmail("staff1@example.com").isEmpty()) {
            User staff = new User();
            staff.setUsername("staff1");
            staff.setPassword(passwordEncoder.encode("password"));
            staff.setEmail("staff1@example.com");
            staff.setFullName("Staff One");
            Set<Role> staffRoles = new HashSet<>();
            staffRoles.add(staffRole);
            staff.setRoles(staffRoles);
            userRepository.save(staff);
        }

        if (userRepository.findByEmail("manager1@example.com").isEmpty()) {
            User manager = new User();
            manager.setUsername("manager1");
            manager.setPassword(passwordEncoder.encode("password"));
            manager.setEmail("manager1@example.com");
            manager.setFullName("Manager One");
            Set<Role> managerRoles = new HashSet<>();
            managerRoles.add(managerRole);
            manager.setRoles(managerRoles);
            userRepository.save(manager);
        }

        // Tạo dữ liệu mẫu cho Center nếu chưa tồn tại
        if (centerRepository.findByName("Skincare Center 1").isEmpty()) {
            Center center1 = new Center();
            center1.setName("Skincare Center 1");
            center1.setAddress("123 Main St, City A");
            center1.setPhone("123-456-7890");
            center1.setOpenTime(LocalTime.of(9, 0));  // 9:00 AM
            center1.setCloseTime(LocalTime.of(18, 0)); // 6:00 PM
            center1.setEmail("center1@example.com"); // Thêm email
            center1.setDescription("A premium skincare center in City A"); // Thêm description (có thể để null nếu không bắt buộc)
            centerRepository.save(center1);
        }

        if (centerRepository.findByName("Skincare Center 2").isEmpty()) {
            Center center2 = new Center();
            center2.setName("Skincare Center 2");
            center2.setAddress("456 Oak St, City B");
            center2.setPhone("987-654-3210");
            center2.setOpenTime(LocalTime.of(10, 0));  // 10:00 AM
            center2.setCloseTime(LocalTime.of(19, 0)); // 7:00 PM
            center2.setEmail("center2@example.com"); // Thêm email
            center2.setDescription("A premium skincare center in City B"); // Thêm description (có thể để null nếu không bắt buộc)
            centerRepository.save(center2);
        }
    }
}