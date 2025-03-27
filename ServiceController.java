package com.example.skincare.controller;
import com.example.skincare.models.Service;
import com.example.skincare.repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    // Lấy danh sách tất cả dịch vụ (công khai)
    @GetMapping
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    // Lấy thông tin một dịch vụ theo ID (công khai)
    @GetMapping("/{id}")
    public ResponseEntity<Service> getServiceById(@PathVariable Long id) {
        Optional<Service> service = serviceRepository.findById(id);
        return service.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo dịch vụ mới (chỉ MANAGER)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public Service createService(@RequestBody Service service) {
        return serviceRepository.save(service);
    }

    // Cập nhật dịch vụ (chỉ MANAGER)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Service> updateService(@PathVariable Long id, @RequestBody Service updatedService) {
        Optional<Service> service = serviceRepository.findById(id);
        if (service.isPresent()) {
            Service existingService = service.get();
            existingService.setName(updatedService.getName());
            existingService.setDescription(updatedService.getDescription());
            existingService.setPrice(updatedService.getPrice());
            existingService.setDuration(updatedService.getDuration());
            return ResponseEntity.ok(serviceRepository.save(existingService));
        }
        return ResponseEntity.notFound().build();
    }

    // Xóa dịch vụ (chỉ MANAGER)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        if (serviceRepository.existsById(id)) {
            serviceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
