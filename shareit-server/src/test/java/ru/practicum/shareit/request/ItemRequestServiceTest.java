package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestDao;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Mock
    private ItemRequestDao repository;

    @Mock
    private UserDao userRepository;

    @Mock
    private ItemDao itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl service;

    private User requester;
    private Item item;
    private ItemRequest request;

    @BeforeEach
    void started() {
        User owner = User.builder()
                .id(1L)
                .name("nick")
                .email("nick@mail.ru")
                .build();

        requester = User.builder()
                .id(2L)
                .name("fred")
                .email("fred@mail.ru")
                .build();

        request = ItemRequest.builder()
                .id(1L)
                .requester(requester)
                .created(LocalDateTime.now())
                .build();

        item = Item.builder()
                .id(4L)
                .name("table")
                .description("red")
                .available(true)
                .owner(owner)
                .request(request)
                .build();
    }


    @Test
    void succeedCreateRequest() {
        when(repository.save(any())).thenReturn(request);
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));

        ItemRequestDto requestDto = service.createRequest(requester.getId(),
                ItemRequestDto.builder().description("red").build());

        assertNotNull(requestDto);
        assertEquals(request.getId(), requestDto.getId());
        verify(repository, times(1)).save(any());
    }

    @Test
    void createRequestFailByUserNotFound() {
        long userNotFoundId = 0L;
        String error = "Пользователь не найден.";
        when(userRepository.findById(userNotFoundId)).thenThrow(new NotFoundException(error));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.createRequest(userNotFoundId, ItemRequestDto.builder().description("red").build())
        );

        assertEquals(error, exception.getMessage());
        verify(repository, times(0)).save(any());
    }

    @Test
    void succeedFindAllByRequesterId() {
        long userId = requester.getId();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(repository.findAllByRequesterId(userId)).thenReturn(List.of(request));

        List<ItemRequestDtoByOwner> requests = service.findAllUsersRequestsWithReplies(userId);

        assertNotNull(requests);
        assertEquals(1, requests.size());
        verify(repository, times(1)).findAllByRequesterId(userId);
    }

    @Test
    void succeedFindAll() {
        long userId = requester.getId();
        int from = 0;
        int size = 1;
        PageRequest pageRequest = PageRequest.of(from / size, size, SORT);
        when(repository.findAllByRequesterIdNot(userId, pageRequest)).thenReturn(List.of(request));

        List<ItemRequestDtoByOwner> requestDto = service.findAll(userId, from, size);

        assertNotNull(requestDto);
        assertEquals(1, requestDto.size());
    }

    @Test
    void findAllReturnEmptyList() {
        long userId = requester.getId();
        int from = 0;
        int size = 1;
        PageRequest pageRequest = PageRequest.of(from / size, size, SORT);
        when(repository.findAllByRequesterIdNot(userId, pageRequest)).thenReturn(Collections.emptyList());

        List<ItemRequestDtoByOwner> requestDto = service.findAll(userId, from, size);

        assertNotNull(requestDto);
        assertEquals(0, requestDto.size());
    }

    @Test
    void findByIdWithReplies() {
        long userId = requester.getId();
        when(userRepository.existsById(userId)).thenReturn(true);
        long requestId = request.getId();
        when(repository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of(item));

        ItemRequestDtoByOwner requestDto = service.findByIdWithReplies(userId, requestId);

        assertNotNull(requestDto);
        assertEquals(requestId, requestDto.getId());
        assertEquals(1, requestDto.getItems().size());
        assertEquals(item.getId(), requestDto.getItems().get(0).getId());
    }
}
