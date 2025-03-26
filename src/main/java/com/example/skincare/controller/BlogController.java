package com.example.skincare.controller;
import com.example.skincare.models.Blog;
import com.example.skincare.repositories.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogRepository blogRepository;

    // Lấy danh sách tất cả bài blog (công khai)
    @GetMapping
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    // Lấy thông tin một bài blog theo ID (công khai)
    @GetMapping("/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable Long id) {
        Optional<Blog> blog = blogRepository.findById(id);
        return blog.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo bài blog mới (chỉ MANAGER hoặc STAFF)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public Blog createBlog(@RequestBody Blog blog) {
        return blogRepository.save(blog);
    }

    // Cập nhật bài blog (chỉ MANAGER hoặc STAFF)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<Blog> updateBlog(@PathVariable Long id, @RequestBody Blog updatedBlog) {
        Optional<Blog> blog = blogRepository.findById(id);
        if (blog.isPresent()) {
            Blog existingBlog = blog.get();
            existingBlog.setTitle(updatedBlog.getTitle());
            existingBlog.setContent(updatedBlog.getContent());
            existingBlog.setAuthor(updatedBlog.getAuthor());
            existingBlog.setCreatedAt(updatedBlog.getCreatedAt());
            return ResponseEntity.ok(blogRepository.save(existingBlog));
        }
        return ResponseEntity.notFound().build();
    }

    // Xóa bài blog (chỉ MANAGER hoặc STAFF)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        if (blogRepository.existsById(id)) {
            blogRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}