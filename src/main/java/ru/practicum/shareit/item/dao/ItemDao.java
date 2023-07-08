package ru.practicum.shareit.item.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemDao extends JpaRepository<Item, Long> {
    List<Item> findItemsByOwnerId(Long ownerId, Pageable page);

    List<Item> findItemsByOwnerId(Long ownerId);

    List<Item> findByAvailableTrueAndDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(String text,
                                                                                               String texts,
                                                                                               Pageable page);

    List<Item> findByRequestId(Long requestId);
}
