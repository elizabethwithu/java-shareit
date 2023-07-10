package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestDao;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoByOwner;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.service.UserServiceImpl.checkUserAvailability;

@RequiredArgsConstructor
@Slf4j
@Transactional
@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserDao userDao;
    private final ItemRequestDao requestDao;
    private final ItemDao itemDao;

    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        User user = userDao.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        requestDto.setRequester(user);

        ItemRequest request = ItemRequestMapper.toItemRequest(requestDto);
        ItemRequest savedRequest = requestDao.save(request);
        log.info("Добавлен запрос {}", savedRequest);

        return ItemRequestMapper.doItemRequestDto(savedRequest);
    }

    public List<ItemRequestDtoByOwner> findAllUsersRequestsWithReplies(Long userId) {
        checkUserAvailability(userDao, userId);
        List<ItemRequest> requests = requestDao.findAllByRequesterId(userId);

        return findAndMap(requests);
    }

    public List<ItemRequestDtoByOwner> findAll(Long userId, int from, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        List<ItemRequest> requests = requestDao.findAllByRequesterIdNot(userId, pageRequest);

        return findAndMap(requests);
    }

    public ItemRequestDtoByOwner findByIdWithReplies(Long userId, Long requestId) {
        checkUserAvailability(userDao, userId);
        ItemRequest request = requestDao.findById(requestId).orElseThrow(() -> new NotFoundException("Запрос не найден."));

        List<Item> items = itemDao.findByRequestId(requestId);
        List<ItemDto> reply = new ArrayList<>();
        for (Item item : items) {
            reply.add(ItemMapper.doItemDto(item));
        }
        return ItemRequestMapper.doItemRequestDtoByOwner(request, reply);
    }

    private List<ItemRequestDtoByOwner> findAndMap(List<ItemRequest> requests) {
        List<ItemDto> itemDtoList = new ArrayList<>();
        List<ItemRequestDtoByOwner> result = new ArrayList<>();
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemDao.findByRequestIdIn(requestIds);

        for (ItemRequest itemRequest : requests) {
            for (Item item : items) {
                if (Objects.equals(itemRequest.getId(), item.getRequest().getId())) {
                    itemDtoList.add(ItemMapper.doItemDto(item));
                }
            }
            result.add(ItemRequestMapper.doItemRequestDtoByOwner(itemRequest, itemDtoList));
        }
        return result;
    }
}
