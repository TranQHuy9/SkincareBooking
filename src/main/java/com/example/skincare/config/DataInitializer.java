/*package com.example.skincare.config;

import com.example.skincare.models.*;
import com.example.skincare.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
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
    private ServiceRepository serviceRepository;

    @Autowired
    private SkinTherapistRepository therapistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("Starting DataInitializer...");

        // Tạo các role nếu chưa tồn tại
        Role customerRole = roleRepository.findByName("CUSTOMER");
        if (customerRole == null) {
            customerRole = new Role("CUSTOMER");
            roleRepository.save(customerRole);
            System.out.println("Created CUSTOMER role");
        }

        Role therapistRole = roleRepository.findByName("THERAPIST");
        if (therapistRole == null) {
            therapistRole = new Role("THERAPIST");
            roleRepository.save(therapistRole);
            System.out.println("Created THERAPIST role");
        }

        Role staffRole = roleRepository.findByName("STAFF");
        if (staffRole == null) {
            staffRole = new Role("STAFF");
            roleRepository.save(staffRole);
            System.out.println("Created STAFF role");
        }

        Role managerRole = roleRepository.findByName("MANAGER");
        if (managerRole == null) {
            managerRole = new Role("MANAGER");
            roleRepository.save(managerRole);
            System.out.println("Created MANAGER role");
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
            System.out.println("Created customer1 user");
        }

        // Tạo 10 therapist users
        User[] therapists = new User[10];
        for (int i = 1; i <= 10; i++) {
            String email = "therapist" + i + "@example.com";
            String username = "therapist" + i;
            String fullName = "Therapist " + numberToWord(i);
            if (userRepository.findByEmail(email).isEmpty()) {
                User therapist = new User();
                therapist.setUsername(username);
                therapist.setPassword(passwordEncoder.encode("password"));
                therapist.setEmail(email);
                therapist.setFullName(fullName);
                Set<Role> therapistRoles = new HashSet<>();
                therapistRoles.add(therapistRole);
                therapist.setRoles(therapistRoles);
                userRepository.save(therapist);
                System.out.println("Created " + username + " user");
                therapists[i - 1] = therapist;
            } else {
                therapists[i - 1] = userRepository.findByEmail(email).orElseThrow();
                System.out.println("Found existing " + username + " user");
            }
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
            System.out.println("Created staff1 user");
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
            System.out.println("Created manager1 user");
        }

        // Tạo dữ liệu mẫu cho Center nếu chưa tồn tại
        if (centerRepository.findByName("Skincare Center 1").isEmpty()) {
            Center center1 = new Center();
            center1.setName("Skincare Center 1");
            center1.setAddress("123 Main St, City A");
            center1.setPhone("123-456-7890");
            center1.setOpenTime(LocalTime.of(9, 0));
            center1.setCloseTime(LocalTime.of(18, 0));
            center1.setEmail("center1@example.com");
            center1.setDescription("A premium skincare center in City A");
            centerRepository.save(center1);
            System.out.println("Created Skincare Center 1");
        }

        if (centerRepository.findByName("Skincare Center 2").isEmpty()) {
            Center center2 = new Center();
            center2.setName("Skincare Center 2");
            center2.setAddress("456 Oak St, City B");
            center2.setPhone("987-654-3210");
            center2.setOpenTime(LocalTime.of(10, 0));
            center2.setCloseTime(LocalTime.of(19, 0));
            center2.setEmail("center2@example.com");
            center2.setDescription("A premium skincare center in City B");
            centerRepository.save(center2);
            System.out.println("Created Skincare Center 2");
        }

        // Tạo dữ liệu mẫu cho Service nếu chưa tồn tại
        Service[] services = new Service[10];
        String[] serviceNames = {
                "Soothing Acne Treatment", "Acne Treatment Facial", "Intensive Hydration Therapy",
                "Anti-Aging Facial", "Collagen Boosting Treatment", "Balancing Hydration Facial",
                "Gentle Anti-Aging Facial", "Microcurrent Anti-Aging Treatment", "Classic Hydrating Facial",
                "Vitamin C Infusion"
        };
        String[] descriptions = {
                "A gentle acne treatment with LED light therapy",
                "Deep cleansing facial for acne-prone skin",
                "Deep hydration with Hyaluronic Acid",
                "Anti-aging treatment to reduce wrinkles",
                "Boost collagen production for firmer skin",
                "Balance moisture for combination skin",
                "Mild anti-aging treatment for sensitive skin",
                "Lift and firm skin with microcurrent therapy",
                "Basic hydrating facial for normal skin",
                "Brighten skin with Vitamin C"
        };
        double[] prices = {50.0, 60.0, 70.0, 80.0, 90.0, 65.0, 75.0, 100.0, 55.0, 60.0};
        int[] durations = {60, 75, 60, 90, 90, 75, 75, 90, 60, 45};

        for (int i = 0; i < 10; i++) {
            String name = serviceNames[i];
            if (serviceRepository.findByName(name).isEmpty()) {
                Service service = new Service();
                service.setName(name);
                service.setDescription(descriptions[i]);
                service.setPrice(prices[i]);
                service.setDuration(durations[i]);
                serviceRepository.save(service);
                System.out.println("Created " + name + " service");
            }
            services[i] = serviceRepository.findByName(name).orElseThrow();
        }

        // Tạo các SkinTherapist và gán dịch vụ
        System.out.println("Creating SkinTherapists...");
        SkinTherapist[] skinTherapists = new SkinTherapist[10];
        String[] expertises = {
                "Acne Treatment", "Acne Treatment", "Hydration Therapy",
                "Anti-Aging", "Collagen Boosting", "Hydration Therapy",
                "Anti-Aging", "Microcurrent Therapy", "Hydration Therapy",
                "Vitamin C Infusion"
        };

        for (int i = 0; i < 10; i++) {
            SkinTherapist skinTherapist = new SkinTherapist();
            skinTherapist.setUser(therapists[i]);
            skinTherapist.setFullName("Therapist " + numberToWord(i + 1));
            skinTherapist.setExpertise(expertises[i]);
            skinTherapist.setExperience("5 years");

            // Gán mỗi therapist cho một dịch vụ duy nhất
            skinTherapist.setServices(new HashSet<>(Collections.singletonList(services[i])));
            skinTherapists[i] = skinTherapist;
        }

        // Lưu các SkinTherapist
        System.out.println("Saving SkinTherapists...");
        therapistRepository.saveAll(Arrays.asList(skinTherapists));
        System.out.println("DataInitializer completed successfully!");
    }

    // Helper method để chuyển số thành chữ (1 -> One, 2 -> Two, ...)
    private String numberToWord(int number) {
        String[] words = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"};
        return words[number - 1];
    }
}




 */



