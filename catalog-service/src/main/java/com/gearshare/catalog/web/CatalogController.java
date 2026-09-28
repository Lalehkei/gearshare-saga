package com.gearshare.catalog.web;

import com.gearshare.catalog.domain.GearItem;
import com.gearshare.catalog.domain.GearItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only endpoints so you can watch item availability flip live while a saga runs --
 * hit GET /api/items before and after a POST /api/rentals on booking-service and you'll
 * see the item you rented go from available to unavailable (and back, if it gets
 * compensated).
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class CatalogController {

    private final GearItemRepository itemRepository;

    @GetMapping
    public List<GearItem> listItems() {
        return itemRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GearItem> getItem(@PathVariable String id) {
        return itemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
