package com.example.skincare.controller;

import com.example.skincare.models.Schedule;
import com.example.skincare.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    // Lấy danh sách tất cả lịch làm việc (chỉ MANAGER hoặc STAFF)
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    // Lấy thông tin một lịch làm việc theo ID (chỉ MANAGER, STAFF, hoặc THERAPIST sở hữu)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfSchedule(#id, authentication)")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        Optional<Schedule> schedule = scheduleRepository.findById(id);
        return schedule.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo lịch làm việc mới (chỉ MANAGER, STAFF, hoặc THERAPIST)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or hasRole('THERAPIST')")
    public Schedule createSchedule(@RequestBody Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    // Cập nhật lịch làm việc (chỉ MANAGER, STAFF, hoặc THERAPIST sở hữu)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfSchedule(#id, authentication)")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule updatedSchedule) {
        Optional<Schedule> schedule = scheduleRepository.findById(id);
        if (schedule.isPresent()) {
            Schedule existingSchedule = schedule.get();
            existingSchedule.setTherapist(updatedSchedule.getTherapist());
            existingSchedule.setStartTime(updatedSchedule.getStartTime());
            existingSchedule.setEndTime(updatedSchedule.getEndTime());
            existingSchedule.setIsAvailable(updatedSchedule.getIsAvailable());
            return ResponseEntity.ok(scheduleRepository.save(existingSchedule));
        }
        return ResponseEntity.notFound().build();
    }

    // Xóa lịch làm việc (chỉ MANAGER, STAFF, hoặc THERAPIST sở hữu)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfSchedule(#id, authentication)")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        if (scheduleRepository.existsById(id)) {
            scheduleRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}