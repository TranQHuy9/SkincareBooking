package com.example.skincare.res;

import com.example.skincare.models.Blog;
import com.example.skincare.models.Center;
import com.example.skincare.models.Service;
import com.example.skincare.models.SkinTherapist;
import com.example.skincare.repositories.BlogRepository;
import com.example.skincare.repositories.CenterRepository;
import com.example.skincare.repositories.ServiceRepository;
import com.example.skincare.repositories.SkinTherapistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class HomeController {

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SkinTherapistRepository skinTherapistRepository;

    @Autowired
    private BlogRepository blogRepository;

    @GetMapping("/home")
    public Map<String, Object> getHomeData() {
        Map<String, Object> homeData = new HashMap<>();

        List<Center> centers = centerRepository.findAll();
        List<Service> services = serviceRepository.findAll();
        List<SkinTherapist> therapists = skinTherapistRepository.findAll();
        List<Blog> blogs = blogRepository.findAll();

        homeData.put("centers", centers);
        homeData.put("services", services);
        homeData.put("therapists", therapists);
        homeData.put("blogs", blogs);

        return homeData;
    }
}